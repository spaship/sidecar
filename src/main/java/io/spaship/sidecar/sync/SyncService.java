package io.spaship.sidecar.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import io.spaship.sidecar.type.OperationException;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.event.Observes;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SyncService {

    private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);
    private static final List<Timer> timers = new ArrayList<>();
    Predicate<String> hasSubPath = subPath ->Objects.nonNull(subPath) &&
            !(subPath.isEmpty() || subPath.isBlank()) && !subPath.equals("/");
    BiPredicate<String, String> isForwardSlashMissing = (subPath, sourceUrl) -> {
        if(!hasSubPath.test(subPath))
            return false;
        return !(subPath.startsWith("/") || sourceUrl.endsWith("/"));
    };
    AtomicBoolean errorFlag = new AtomicBoolean(false);


    void startup(@Observes StartupEvent startupEvent) throws JsonProcessingException {
        LOG.info("reading sync from config");
        String syncConfig = ConfigProvider.getConfig().getValue("sidecar.sync.config", String.class);
        if (syncConfig.equalsIgnoreCase("na")) {
            LOG.info("sync config doesn't exists");
            return;
        }
        LOG.info("mapping sync config into ConfigJsonWrapper");
        ObjectMapper mapper = new ObjectMapper();
        var config = mapper.readValue(syncConfig, ConfigJsonWrapper.class);
        LOG.info("applying sync config");
        init(config);
    }

    public int getTasksLength() {
        return timers.size();
    }


    public boolean init(String url) throws IOException {
        String syncConfig = readRemoteSyncConfig(url);
        ObjectMapper mapper = new ObjectMapper();
        var config = mapper.readValue(syncConfig, ConfigJsonWrapper.class);
        return init(config);
    }

    public boolean init(ConfigJsonWrapper config) {
        Objects.requireNonNull(config, "configObject not fond!");
        cancelAllTasks();
        schedule(config.getTargetEntries());

        boolean hasError = errorFlag.get();
        errorFlag.set(false);
        return !hasError;
    }


    private String readRemoteSyncConfig(String url) throws IOException {
        var connection = new URL(url).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        var syncConfig = scanner.next();
        scanner.close();
        return syncConfig;
    }

    private void schedule(List<TargetEntry> targetEntries) {
        targetEntries.forEach(this::trigger);
        LOG.info("all target entries are scheduled to know more check resource /sync");
    }

    private void trigger(TargetEntry te) {

        Timer timer = new Timer(te.getName());
        TimerTask task = new TimerTask() {
            public void run() {
                updateResource(te);
                LOG.debug("fetching target  {} and interval is set to {} ", te.getName(), te.getInterval());
            }
        };

        int interval = te.getIntInterval();

        if (interval == 0) {
            timer.schedule(task, 500L);
            return;
        }


        timer.schedule(task, 0, (interval * 1000L));
        timers.add(timer);
        LOG.debug("added imer in the list the list length is {}", timers.size());

    }


    public void cancelAllTasks() {
        LOG.info("the length of timer is {}", timers.size());
        timers.forEach(timer -> {
            timer.cancel();
            timer.purge();
            LOG.info("removed timer {}", timer);
        });
        timers.clear();
    }


    public void updateResource(TargetEntry targetEntry) {


        if (targetEntry.getSourceSubPaths().isEmpty())
            targetEntry.setSourceSubPaths(new String[]{"/"});

        //inner logic of schedule
        targetEntry.getSourceSubPaths().forEach(subPath -> {
            var targetUrlParts = targetEntry.getSourceUrl().split("\\?");
            var targetUrl = targetEntry.getSourceUrl().concat(subPath);

            if (hasSubPath.test(subPath) && isForwardSlashMissing.test(subPath, targetEntry.getSourceUrl()))
                targetUrl = targetEntry.getSourceUrl().concat("/").concat(subPath);

            var urlPartLength = targetUrlParts.length;
            if (urlPartLength > 2)
                LOG.warn("target url part length must not exceed 2, " +
                        "something went wrong, targetEntry.getSourceUrl() is {}", targetEntry.getSourceUrl());

            if (urlPartLength > 1) {
                if (isForwardSlashMissing.test(subPath, targetUrlParts[0])) {
                    targetUrl = targetUrlParts[0].concat("/").concat(subPath).concat("?").concat(targetUrlParts[1]);
                } else {
                    if(!hasSubPath.test(subPath))
                        subPath="";
                    targetUrl = targetUrlParts[0].concat(subPath).concat("?").concat(targetUrlParts[1]);
                }
            }


            LOG.debug("targetUrl is {} and targetUrl length is {}", targetUrl, urlPartLength);

            var fullyQualifiedDestPath = targetEntry.getDestPath()
                    .concat(subPath);
            try {
                copyAndReplace(targetUrl, fullyQualifiedDestPath, targetEntry.getDestFileName());
            } catch (OperationException e) {
                LOG.error(e.getMessage());
                errorFlag.set(true);
            }
        });
    }


    //TODO break into smaller functions
    private void copyAndReplace(String targetUrl, String fullyQualifiedDestPath, String fileName) throws OperationException {

        // format target url
        String transformedTargetUrl = replaceTrailingSlash(targetUrl);

        // compute simple absolute html file path based on target directory and file name
        String absHtmlFilePath = fullyQualifiedDestPath.concat("/").concat(fileName);

        // create destination folder if not exists
        File file = new File(fullyQualifiedDestPath);

        // create target directories if not exists
        if (!file.isDirectory()) {
            try {
                Files.createDirectories(Paths.get(fullyQualifiedDestPath));
            } catch (IOException e) {
                throw new OperationException(
                        String.format("failed to create directories recursively due to %s", e.getMessage())
                        , e);
            }
        }


        // compute  absolute html file path if target directory ends with a /
        if (fullyQualifiedDestPath.substring(fullyQualifiedDestPath.length() - 1).equals("/"))
            absHtmlFilePath = fullyQualifiedDestPath.concat(fileName);

        try {

/*            File htmlFile = new File(absHtmlFilePath);

            // delete the html file if exists
            if(htmlFile.exists())
                htmlFile.delete();

            // create a new html file into the target directory
            var isFileCreated = htmlFile.createNewFile();

            if(!isFileCreated)
                return;*/

            var noCacheParam = ConfigProvider.getConfig()
                    .getValue("curl.nocache.param", String.class).equalsIgnoreCase("na") ?
                    null : ConfigProvider.getConfig().getValue("curl.nocache.param", String.class);
            var proxyParam = ConfigProvider.getConfig()
                    .getValue("curl.proxy.param", String.class).equalsIgnoreCase("na") ?
                    null : ConfigProvider.getConfig().getValue("curl.proxy.param", String.class);
            var debugCurl = ConfigProvider.getConfig()
                    .getValue("curl.command.debug", String.class).equalsIgnoreCase("true");

            //execute curl command
            var statusCodeOutcome = CurlUtility.with(fullyQualifiedDestPath, debugCurl)
                    .apply(transformedTargetUrl, fileName, noCacheParam, proxyParam);
            LOG.debug("curl return code is {}", statusCodeOutcome);

        } catch (Exception e) {
            throw new OperationException(
                    String.format("failed to create html file due to %s", e.getMessage())
                    , e);
        }


    }


    private String replaceTrailingSlash(String targetUrl) {
        //TODO replace this condition with targetUrl.endsWith("/")
        boolean isTrailingSlashPresent = targetUrl.substring(targetUrl.length() - 1).equals("/");
        if (isTrailingSlashPresent) {
            targetUrl = targetUrl.substring(0, targetUrl.length() - 1);
            LOG.info("trailing slash found in the url and removed final targetUrl is {}", targetUrl);
        }
        return targetUrl;
    }

}
