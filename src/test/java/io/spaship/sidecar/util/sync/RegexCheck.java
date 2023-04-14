package io.spaship.sidecar.util.sync;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegexCheck {


    private static final String REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String QMARKREGEX = "\\?";

    @Test
    void urlSplitByRegex() {
        String url = "https://example.com/services/chrome/head?legacy=false";
        String[] urlPart = url.split(QMARKREGEX);
        System.out.println(Arrays.toString(urlPart));
        Assertions.assertTrue(urlPart.length > 1);
    }

    @Test
    void isUrlTrueCase1() {
        var url = "http://google.com";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(url);
        Assertions.assertTrue(m.matches());
    }

    @Test
    void isUrlTrueCase2() {
        var url = "https://google.com";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(url);
        Assertions.assertTrue(m.matches());
    }

    @Test
    void isUrlTrueCase3() {
        var url = "https://raw.githubusercontent.com/arkaprovob/remote-config-for-testing/main/spaship-sync-config.json";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(url);
        Assertions.assertTrue(m.matches());
    }


    @Test
    void isUrlFalseCase1() {
        var url = "raw.githubusercontent.com/arkaprovob/remote-config-for-testing/main/spaship-sync-config.json";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(url);
        Assertions.assertFalse(m.matches());

    }

}
