package io.spaship.sidecar.services;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.sidecar.type.Environment;
import io.spaship.sidecar.type.FormData;
import io.spaship.sidecar.type.OperationResponse;
import io.spaship.sidecar.type.SpashipMapping;
import io.spaship.sidecar.util.CheckedException;
import io.spaship.sidecar.util.CommonOps;
import io.spaship.sidecar.util.CustomException;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


@ApplicationScoped
public class RequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestProcessor.class);

    private final Executor executor = Infrastructure.getDefaultExecutor();

    private final String rootDirIdentifier = ConfigProvider.getConfig()
            .getValue("sidecar.root.dir.identifier", String.class);

    private final String spashipMappingFleName = ConfigProvider.getConfig().getValue("spaship.mapping.file",
            String.class);

    private final String website = ConfigProvider.getConfig().getValue("sidecar.websitename", String.class);

    private final String environmentName = ConfigProvider.getConfig().getValue("sidecar.environmentname",
            String.class);

    private final String websiteVersion = ConfigProvider.getConfig().getValue("sidecar.website.version",
            String.class);

    private final String parentDeploymentDirectory = ConfigProvider.getConfig().getValue("sidecar.spadir",
            String.class);

    private final Environment environment = Environment.builder()
            .name(environmentName)
            .websiteName(website)
            .websiteVersion(websiteVersion)
            .build();


    void onStart(@Observes StartupEvent ev) {
        validatePropertyFields();
    }

    private void validatePropertyFields() {
        LOG.debug("field validation start");
        Objects.requireNonNull(website, "website is missing in the configuration");
        Objects.requireNonNull(environmentName, "website is missing in the configuration");
        Objects.requireNonNull(websiteVersion, "websiteVersion is missing in the configuration");
        Objects.requireNonNull(spashipMappingFleName,"spaship mapping file identifier missing");
        Objects.requireNonNull(parentDeploymentDirectory,"deployment directory info missing");
        LOG.debug("field validation end");
    }

    public Uni<OperationResponse> handleFileUpload(FormData formData) {
        return Uni.createFrom()
                .item(() -> processFile(formData))
                .runSubscriptionOn(executor);
    }


    private OperationResponse processFile(FormData formData) {

        OperationResponse.OperationResponseBuilder opsBuilderCommon = OperationResponse.builder()
                .environment(environment)
                .originatedFrom(this.getClass().toGenericString())
                .sideCarServiceUrl(null);

        // Collect information
        var zipFilePath = formData.getfilePath().toString();
        String unZippedPath = null;
        try {
            unZippedPath = CommonOps.unzip(zipFilePath, null);
        } catch (IOException e) {
            LOG.error("error in method CommonOps.unzip ",e);
            var errMsg = e.getMessage();
            if(Objects.isNull(errMsg) || errMsg.contains("null") ||  errMsg.isEmpty())
                errMsg = "something went wrong, null pointer exception, check log for more details";
            return opsBuilderCommon.status(0).errorMessage(errMsg).build();
        }

        //extract context path
        String contextPath = null;
        try {
            contextPath = extractSpaContextPath(unZippedPath);
        } catch (Exception e) {

            LOG.error("error {}",e.getMessage());
            var errMsg = e.getMessage();
            if(Objects.isNull(errMsg) || errMsg.contains("null") ||  errMsg.isEmpty())
                errMsg = "something went wrong, may be a null pointer exception, check log for more details";
            return opsBuilderCommon.status(0).errorMessage(errMsg).build();
        }


        //Determine absolute path, if contextPath is set to root folder then set absolute path as parent dir
        String absoluteSpaPath = computeAbsoluteDeploymentPath(contextPath);

        //Prepare target directory
        int status = handleDir(absoluteSpaPath,parentDeploymentDirectory,contextPath);

        var sourcePath = Paths.get(unZippedPath);
        var destinationPath = Paths.get(absoluteSpaPath);

        // Copy files from temporary place to the target directory
        try {
            copySpa(status, sourcePath, destinationPath);
        } catch (CustomException e) {
            return opsBuilderCommon.status(0).errorMessage(e.getMessage()).build();
        }

        // set response status based on target dir preparation status
        LOG.debug("status is {}", status);
        if (status == 1 || status == -1) {
            LOG.debug("It was an existing SPA");
            opsBuilderCommon.status(2);
        }
        if (status == 0) {
            LOG.debug("It's a new SPA!");
            opsBuilderCommon.status(1);
        }
        var opsResponse = opsBuilderCommon.build();
        LOG.info("ops response is {}", opsResponse);

        return opsResponse;
    }

    private void copySpa(int status, Path sourcePath, Path destinationPath) throws CustomException {
        try (var pathStream = Files.walk(sourcePath)) {
            pathStream.forEach(source -> {
                try {
                    copyOps(parentDeploymentDirectory, status,
                            sourcePath, destinationPath, source);
                } catch (Exception e) {
                    throw new CheckedException(e.getMessage());
                }
            });
        } catch (Exception e) {
            LOG.error("error in copySpa ",e);
            throw new CustomException(e.getMessage().concat(" in sidecar"));
        }
    }

    private String computeAbsoluteDeploymentPath(String contextPath) {
        var absoluteSpaPath =  contextPath.equals(rootDirIdentifier)?
                parentDeploymentDirectory : parentDeploymentDirectory.concat(File.separator).concat(contextPath);
        LOG.debug("computed absoluteSpaPath is {}",absoluteSpaPath);
        return absoluteSpaPath;
    }

    private String extractSpaContextPath(String unZippedPath) throws CustomException {
        // Extract and load SpashipMapping
        var path = unZippedPath.concat(File.separator).concat(spashipMappingFleName);
        LOG.debug("fully qualified spaship mapping is {}", path);
        SpashipMapping spaMapping = null;
        try (var content = Files.lines(Paths.get(path))) {
            var spashipMappingString = content.collect(Collectors.joining(System.lineSeparator()));
            spaMapping = new SpashipMapping(spashipMappingString);
        } catch (Exception e) {
            LOG.error("error in method extractSpaContextPath ",e);
            throw new CustomException(e.getMessage().concat(" in sidecar"));
        }

        // Collect deployment dir related information
        var contextPath = spaMapping.getContextPath();
        LOG.debug("extracted context path is {}",contextPath);

        //Validation of context path for avoiding errors
        if(contextPath.contains(rootDirIdentifier) && !contextPath.equals(rootDirIdentifier))
            throw new CustomException("invalid context path detected");

        return contextPath;
    }


    private void copyOps(String parentDeploymentDirectory, int status, Path sourcePath, Path destinationPath,
                         Path source) throws IOException {
        // To avoid DirectoryNotEmptyException
        if(status == -1 && destinationPath.resolve(sourcePath.relativize(source)).toAbsolutePath().toString()
                .equalsIgnoreCase(parentDeploymentDirectory)){
            LOG.debug("copying into root dir and skipping the first iteration");
            return;
        }
        copy(source, destinationPath.resolve(sourcePath.relativize(source)));
    }

    private void copy(Path source, Path dest) throws IOException {
        //LOG.debug("copying {} to {}",source.toAbsolutePath(),dest.toAbsolutePath());
        if(new File(dest.toString()).isDirectory())
            FileUtils.deleteDirectory(new File(dest.toString()));
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        //LOG.debug("Copy DOne!");
    }

    private int handleDir(String dirName,String parentDirectory,String contextPath) {
        var isNestedContextPath = contextPath.contains(File.separator);
        LOG.debug("nested context path detection status {}",isNestedContextPath);

        // new status for identification of deployment in root directory
        if(dirName.equalsIgnoreCase(parentDirectory)){
            LOG.debug("deploying spa in root directory");
            return -1;
        }

        if (isSpaDirExists(dirName)) {
            deleteDirectory(dirName);
            return 1;
        }
        LOG.debug("directory does not exists");
        if(isNestedContextPath){
            var recursiveDirectoryCreateStatus = new File(dirName).mkdirs();
            LOG.debug("recursive dir create status is {}",recursiveDirectoryCreateStatus);
        }else{
            var directoryCreateStatus = new File(dirName).mkdir();
            LOG.debug("dir create status is {}",directoryCreateStatus);
        }
        return 0;
    }

    private boolean isSpaDirExists(String dirName) {
        var dir = new File(dirName);
        var exists = dir.exists();
        LOG.debug("dir {} exists status is {}", dirName, exists);
        return exists;
    }

    private void deleteDirectory(String dirName){
        var dir = new File(dirName);
        var deleted = false;
        try {
            FileUtils.cleanDirectory(dir);
            LOG.debug("Directory cleaned");
            FileUtils.deleteDirectory(dir);
            LOG.debug("Directory deleted");
            deleted = true;
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Error file delete {} , on {}",e.getMessage(),e.getLocalizedMessage());
        }

        LOG.debug("dir {} delete status is {}", dirName, deleted);
    }
}
