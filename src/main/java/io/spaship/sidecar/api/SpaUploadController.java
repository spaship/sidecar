package io.spaship.sidecar.api;

import io.smallrye.mutiny.Uni;
import io.spaship.sidecar.services.FileHandler;
import io.spaship.sidecar.type.FormData;
import io.spaship.sidecar.type.OperationResponse;
import org.jboss.resteasy.reactive.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

@Path("upload")
public class SpaUploadController {

    private static final Logger LOG = LoggerFactory.getLogger(SpaUploadController.class);
    private final FileHandler fileHandlerService;

    public SpaUploadController(FileHandler fileHandlerService) {
        this.fileHandlerService = fileHandlerService;
    }


    @Produces("text/plain")
    @GET
    public String upload() {
        return "please post a spa zip in the same url to make it work";
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<OperationResponse> uploadSPA(@MultipartForm FormData formData) {

        validate(formData);
        return fileHandlerService.handleFileUpload(formData);
    }

    private void validate(FormData formData) {
        String fileName = formData.fileName();
        Long fileSize = formData.fileSize();
        java.nio.file.Path path = formData.getfilePath();

        Objects.requireNonNull(fileName, "validation failed, file name not found");
        Objects.requireNonNull(fileSize, "validation failed, file size cannot be null");
        Objects.requireNonNull(path, "validation failed, unable to store the file");

        LOG.debug("file received  name is {} , size {}, location {} \n",
                fileName, fileSize, path);

    }

}
