package io.spaship.sidecar.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Destination {

    @JsonProperty("path")
    private String path;
    @JsonProperty("filename")
    private String filename;

    @Override
    public String toString() {
        return "{"
                + "\"path\":\"" + path + "\""
                + ", \"filename\":\"" + filename + "\""
                + "}";
    }
}
