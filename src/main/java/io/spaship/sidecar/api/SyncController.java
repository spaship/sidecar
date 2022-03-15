package io.spaship.sidecar.api;

import io.spaship.sidecar.sync.SyncService;
import io.spaship.sidecar.type.OperationException;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("sync")
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);
    private final SyncService syncService;


    public SyncController() {
        this.syncService = new SyncService();
    }


    @Produces("text/plain")
    @GET
    public String scheduleSync() {
        return "sync service is active";
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public String scheduleSync(String url) {

        LOG.info("url is {}",url);

        try {
            boolean res = syncService.init(url);
            if (!res)
                throw new OperationException("operation failed");

        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject().put("status",e.getMessage()).encodePrettily();
        }
        return new JsonObject().put("status","scheduled").encodePrettily();
    }

    @Produces("text/plain")
    @GET
    @Path("cancelAll")
    public String cancelSync() {
        syncService.cancelAllTasks();
        return new JsonObject().put("status","cancelled all tasks").encodePrettily();
    }
}
