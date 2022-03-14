package io.spaship.sidecar.util.sync;

import io.spaship.sidecar.sync.SyncService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.function.BiPredicate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PredicateTest {


    SyncService syncService;
    BiPredicate<String,String> isForwardSlashMissing = (subPath, sourceUrl) -> !(subPath.startsWith("/") || sourceUrl.endsWith("/"));

    @BeforeAll
    void setup() {
        syncService = new SyncService();
    }


    @Test
    void predicate_when_fslash_in_subPath(){

        String url = "https://access.redhat.com/services/chrome/header";
        String subPath = "/k";

        var expectedOutcome = false;
        var outcome = isForwardSlashMissing.test(subPath, url);

        System.out.println(outcome);

        Assertions.assertEquals(expectedOutcome, outcome, "fslash present in sub-path");

    }

    @Test
    void predicate_when_fslash_in_url(){

        String url = "https://access.redhat.com/services/chrome/header/";
        String subPath = "k";

        var expectedOutcome = false;
        var outcome = isForwardSlashMissing.test(subPath, url);

        System.out.println(outcome);

        Assertions.assertEquals(expectedOutcome, outcome, "fslash is present i url");

    }


    @Test
    void predicate_when_fslash_absent(){

        String url = "https://access.redhat.com/services/chrome/header";
        String subPath = "k";

        var expectedOutcome = true;
        var outcome = isForwardSlashMissing.test(subPath, url);

        System.out.println(outcome);

        Assertions.assertEquals(expectedOutcome, outcome, "fslash absent");

    }

}
