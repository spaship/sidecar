package io.spaship.sidecar.api;

import io.spaship.sidecar.sync.ConfigJsonWrapper;
import io.spaship.sidecar.sync.SyncService;
import io.spaship.sidecar.type.OperationException;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("sync")
public class SyncController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncController.class);
    private static final String RESPONSE_KEY = "status";
    private final SyncService syncService;


    public SyncController() {
        this.syncService = new SyncService();
    }


    @Produces("text/plain")
    @GET
    public String scheduleSync() {

        if (syncService.getTasksLength() < 1)
            return "no sync record found";

        return "sync service is active";
    }

    boolean isValidUrl(String url) {
        var regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        return m.matches();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public String scheduleSync(String url) {

        LOG.info("url is {}", url);
        if (!isValidUrl(url))
            return new JsonObject().put(RESPONSE_KEY, "request body not accepted").encodePrettily();

        try {
            boolean res = syncService.init(url);
            if (!res)
                throw new OperationException("operation failed");

        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject()
                    .put(RESPONSE_KEY, "failed due to : ".concat(e.getMessage()))
                    .encodePrettily();
        }
        return new JsonObject().put(RESPONSE_KEY, "scheduled").encodePrettily();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String scheduleSync(ConfigJsonWrapper configObject) {

        LOG.info("request payload is  {}", configObject);
        boolean res = syncService.init(configObject);
        if (!res)
            return new JsonObject()
                    .put(RESPONSE_KEY, "something went wrong, please check the console for more details")
                    .encodePrettily();
        return new JsonObject().put(RESPONSE_KEY, "scheduled").encodePrettily();
    }

    @Produces("text/plain")
    @GET
    @Path("cancelAll")
    public String cancelSync() {
        syncService.cancelAllTasks();
        return new JsonObject()
                .put(RESPONSE_KEY, "unscheduled all tasks")
                .encodePrettily();
    }
}
