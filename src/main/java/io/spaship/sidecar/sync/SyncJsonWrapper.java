package io.spaship.sidecar.sync;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncJsonWrapper {


    @JsonProperty("autosync")
    private AutoSync autosync;


    @Override
    public String toString() {
        return "{"
                + "\"autosync\":" + autosync
                + "}";
    }
}
