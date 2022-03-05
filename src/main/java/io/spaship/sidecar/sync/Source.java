package io.spaship.sidecar.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Source {

    @JsonProperty("url")
    private String url;
    @JsonProperty("sub_paths")
    private String[] subPaths;


    @Override
    public String toString() {
        return "{"
                + "\"url\":\"" + url + "\""
                + ", \"subPaths\":" + Arrays.toString(subPaths)
                + "}";
    }
}
