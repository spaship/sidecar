package io.spaship.sidecar.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.sidecar.type.FormData;
import io.spaship.sidecar.type.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;


@ApplicationScoped
public class FileHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();

    public Uni<OperationResponse> handleFileUpload(FormData formData) {
        return Uni.createFrom()
                .item(() -> processFile(formData))
                .runSubscriptionOn(executor);
    }

    private OperationResponse processFile(FormData formData) {

        var absoluteFilePath = formData.getfilePath();
        File spaDistribution = new File(absoluteFilePath.toUri());
        assert spaDistribution.exists();


        return null;
    }


}
