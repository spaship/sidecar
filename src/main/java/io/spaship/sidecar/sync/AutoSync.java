package io.spaship.sidecar.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class AutoSync {

    @JsonProperty("enabled")
    boolean enabled;

    @JsonProperty("targets")
    TargetEntry[] targetEntries;


    @Override
    public String toString() {
        return "{"
                + "\"enabled\":\"" + enabled + "\""
                + ", \"targetEntries\":" + Arrays.toString(targetEntries)
                + "}";
    }
}
