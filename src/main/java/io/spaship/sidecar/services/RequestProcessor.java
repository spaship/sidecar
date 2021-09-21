package io.spaship.sidecar.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.sidecar.type.Environment;
import io.spaship.sidecar.type.FormData;
import io.spaship.sidecar.type.OperationResponse;
import io.spaship.sidecar.type.SpashipMapping;
import io.spaship.sidecar.util.CommonOps;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ApplicationScoped
public class RequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestProcessor.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();


    public Uni<OperationResponse> handleFileUpload(FormData formData) {
        return Uni.createFrom()
                .item(() -> processFile(formData))
                .runSubscriptionOn(executor);
    }

    @SneakyThrows
    private OperationResponse processFile(FormData formData) {

        var zipFilePath = formData.getfilePath().toString();
        var unZippedPath = CommonOps.unzip(zipFilePath, null);

        var spashipMappingFleName = ConfigProvider.getConfig().getValue("spaship.mapping.file", String.class);

        var website = ConfigProvider.getConfig().getValue("sidecar.websitename", String.class);
        var environmentName = ConfigProvider.getConfig().getValue("sidecar.environmentname", String.class);
        var websiteVersion = ConfigProvider.getConfig().getValue("sidecar.website.version", String.class);
        var environment = Environment.builder()
                .name(environmentName)
                .websiteName(website)
                .websiteVersion(websiteVersion)
                .build();

        Objects.requireNonNull(website,"website is missing in the configuration");
        Objects.requireNonNull(environmentName,"website is missing in the configuration");
        Objects.requireNonNull(websiteVersion,"websiteVersion is missing in the configuration");

        var opsBuilderCommon = OperationResponse.builder()
                .environment(environment)
                .originatedFrom(this.getClass())
                .sideCarServiceUrl(null);

        if(Objects.isNull(spashipMappingFleName))
            return opsBuilderCommon.status(0).errorMessage("spaship-mapping not found").build();

        var path = unZippedPath.concat(File.separator).concat(spashipMappingFleName);
        LOG.debug("fully qualified spaship mapping is {}",path);
        SpashipMapping spaMapping = null;
        try(var content = Files.lines(Paths.get(path))){
            var spashipMappingString = content.collect(Collectors.joining(System.lineSeparator()));
            spaMapping = new SpashipMapping(spashipMappingString);
        }catch (Exception e){
            return opsBuilderCommon.status(0).errorMessage(e.getMessage()).build();
        }

        var contextPath = spaMapping.getContextPath();
        var parentDeploymentDirectory = ConfigProvider.getConfig().getValue("sidecar.spadir", String.class);
        var absoluteSpaPath = parentDeploymentDirectory.concat(File.separator).concat(contextPath);
        LOG.debug("computed absolute spa path is {}",absoluteSpaPath);

        int status = deleteIfExists(absoluteSpaPath);

        var sourcePath = Paths.get(unZippedPath);
        var destinationPath = Paths.get(absoluteSpaPath);

        try(var pathStream = Files.walk(sourcePath)){
            pathStream.forEach(source -> copy(source, destinationPath.resolve(sourcePath.relativize(source))));
        }catch (Exception e) {
            return opsBuilderCommon.status(0).errorMessage(e.getMessage()).build();
        }

        LOG.debug("status is {}",status);
        if(status==1){
            LOG.debug("It was an existing SPA");
            opsBuilderCommon.status(2);
        }

        if(status==0){
            LOG.debug("It's a new SPA!");
            opsBuilderCommon.status(1);
        }

        var opsResponse = opsBuilderCommon.build();
        LOG.debug("ops response is {}",opsResponse);
        return opsResponse;
    }


    @SneakyThrows
    private void copy(Path source, Path dest){
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }


    private int deleteIfExists(String dirName){
        if(isSpaDirExists(dirName)){
            deleteDirectory(dirName);
            return 1;
        }
        return 0;
    }

    private boolean isSpaDirExists(String dirName){
        var dir = new File(dirName);
        var exists = dir.exists();
        LOG.debug("dir {} exists status is {}",dirName,exists);
        return exists;
    }

    private void deleteDirectory(String dirName){
        var dir = new File(dirName);
        var deleted = delRecursive(dir);
        LOG.debug("dir {} delete status is {}",dirName,deleted);
    }

    static boolean delRecursive(File fileOrDir) {
        return fileOrDir.isDirectory() ? Arrays.stream(Objects.requireNonNull(fileOrDir.listFiles()))
                .allMatch(RequestProcessor::delRecursive) && fileOrDir.delete() : fileOrDir.delete();
    }
}
