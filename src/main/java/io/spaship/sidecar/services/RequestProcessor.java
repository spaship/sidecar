package io.spaship.sidecar.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.spaship.sidecar.type.FormData;
import io.spaship.sidecar.type.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.concurrent.Executor;


@ApplicationScoped
public class RequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestProcessor.class);
    private final Executor executor = Infrastructure.getDefaultExecutor();

    public Uni<OperationResponse> handleFileUpload(FormData formData) {
        return Uni.createFrom()
                .item(() -> processFile(formData))
                .runSubscriptionOn(executor);
    }

    private OperationResponse processFile(FormData formData) {

        var zipFilePath = formData.getfilePath().toString();





        return null;
    }

    private void isSpaDirExists(String dirName){
    }
    private void createSpaDirectory(String dirName){
    }
    private void updateSpaDirectory(String dirName, File spa){
    }

}
