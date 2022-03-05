package io.spaship.sidecar.sync;

import io.spaship.sidecar.services.RequestProcessor;
import io.spaship.sidecar.type.OperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiPredicate;

public class SyncService {

    private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);


    private void schedule(List<TargetEntry> targetEntries){
        targetEntries.forEach(this::scheduleSync);
    }

    public BiPredicate<String,String> isForwardSlashMissing = (subPath, sourceUrl) -> !(subPath.startsWith("/") || sourceUrl.endsWith("/"));




    public void scheduleSync(TargetEntry targetEntry) {

        //TODO.. spin one new vertx periodic schedule and then invoke internal logic

        //inner logic of schedule
        targetEntry.getSourceSubPaths().forEach(subPath ->{
            var targetUrlParts = targetEntry.getSourceUrl().split("\\\\?(?!\\\\?)",2);
            var targetUrl = targetEntry.getSourceUrl().concat(subPath);


            if(isForwardSlashMissing.test(subPath,targetEntry.getSourceUrl()))
                targetUrl = targetEntry.getSourceUrl().concat("/").concat(subPath);



            var urlPartLength = targetUrlParts.length;
            if(urlPartLength >2)
                LOG.info("target url part length must not exceed 2, " +
                        "something went wrong, targetEntry.getSourceUrl() is {}",targetEntry.getSourceUrl());
            if (urlPartLength >1)
                targetUrl = targetUrlParts[0].concat(subPath).concat("?").concat(targetUrlParts[1]);

            LOG.info("targetUrl is {} and targetUrl length is {}",targetUrl,urlPartLength);

            var fullyQualifiedDestPath = targetEntry.getDestPath()
                    .concat(subPath);
            try {
                copyAndReplace(targetUrl,fullyQualifiedDestPath,targetEntry.getDestFileName());
            } catch (OperationException e) {
                LOG.error(e.getMessage());
            }
        });
    }

    private void copyAndReplace(String targetUrl, String fullyQualifiedDestPath, String fileName) throws OperationException {

        // curl the html content from remote
        String htmlContent = readFromRemote(targetUrl);

        // compute simple absolute html file path based on target directory and file name
        String absHtmlFilePath = fullyQualifiedDestPath.concat("/").concat(fileName);

        // create destination folder if not exists
        File file = new File(fullyQualifiedDestPath);

        // create target directories if not exists
        if(!file.isDirectory()){
            try {
                Files.createDirectories(Paths.get(fullyQualifiedDestPath));
            } catch (IOException e) {
                throw new OperationException(
                        String.format("failed to create directories recursively due to %s",e.getMessage())
                        ,e);
            }
        }


        // compute  absolute html file path if target directory ends with a /
        if(fullyQualifiedDestPath.substring(fullyQualifiedDestPath.length() - 1).equals("/"))
            absHtmlFilePath = fullyQualifiedDestPath.concat(fileName);

        try {

            File htmlFile = new File(absHtmlFilePath);

            // delete the html file if exists
            if(htmlFile.exists())
                htmlFile.delete();

            // create a new html file into the target directory
            var isFileCreated = htmlFile.createNewFile();

            if(!isFileCreated)
                return;

            //write content into html file
            Path path = Paths.get(absHtmlFilePath);
            byte[] strToBytes = htmlContent.getBytes();
            Files.write(path, strToBytes);

            LOG.info("{} successfully created",absHtmlFilePath);

        } catch (IOException e) {
            throw new OperationException(
                    String.format("failed to create html file due to %s",e.getMessage())
                    ,e);
        }


    }

    private String readFromRemote(String targetUrl) throws OperationException {
        String content = null;
        URLConnection connection = null;
        try {
            connection = new URL(targetUrl).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        } catch (IOException e) {
            throw new OperationException(
                    String.format("unable to fetch html content from remote due to %s",e.getMessage())
                    ,e);

        }

        return content;
    }



}
