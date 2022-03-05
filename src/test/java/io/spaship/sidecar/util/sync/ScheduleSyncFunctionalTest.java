/*
package io.spaship.sidecar.util.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.spaship.sidecar.sync.ConfigJsonWrapper;
import io.spaship.sidecar.sync.SyncService;
import io.spaship.sidecar.sync.TargetEntry;
import io.spaship.sidecar.type.OperationException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Scanner;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
class ScheduleSyncFunctionalTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleSyncFunctionalTest.class);

    SyncService syncService;
    String syncConfig=null;
    ConfigJsonWrapper config =null;
    static final String targetUrl = "https://raw.githubusercontent.com/arkaprovob/remote-config-for-testing/main/spaship-sync-config.json";



    private String readFromRemote() throws OperationException {
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



    @SneakyThrows
    @BeforeAll
    void setup() {
        syncService = new SyncService();

        syncConfig = readFromRemote();
        //System.out.println(syncConfig);

        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(syncConfig,ConfigJsonWrapper.class);
        LOG.debug(config.toString());


    }

    @Test
    void check_sync_config_is_not_null(){
        Assertions.assertTrue(Objects.nonNull(syncConfig));
    }


    @Test
    void test_sync_functionality(){
        var tes = config.getTargetEntries();

        tes.forEach(arg-> syncService.scheduleSync(arg));
    }





}
*/
