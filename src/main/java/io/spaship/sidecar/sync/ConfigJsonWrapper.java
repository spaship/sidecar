package io.spaship.sidecar.sync;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ConfigJsonWrapper {


    @JsonProperty("autosync")
    private AutoSync autosync;


    public List<TargetEntry> getTargetEntries() {
        if (Objects.isNull(autosync) || Objects.isNull(autosync.getTargetEntries()))
            return Collections.emptyList();
        return Arrays.asList(autosync.getTargetEntries());
    }

    @Override
    public String toString() {
        return "{"
                + "\"autosync\":" + autosync
                + "}";
    }
}
