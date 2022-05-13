package io.spaship.sidecar.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class TargetEntry {

    private static final Logger LOG = LoggerFactory.getLogger(TargetEntry.class);

    @JsonProperty("name")
    private String name;
    @JsonProperty("interval")
    private String interval;
    @JsonProperty("source")
    private Source source;
    @JsonProperty("dest")
    private Destination dest;


    public int getIntInterval(){
        var a = this.interval
                .replace("s","");
        try{
            return Integer.parseInt(a);
        }catch(NumberFormatException e){
            return 0;
        }
    }

    public List<String> getSourceSubPaths(){
        if(Objects.isNull(source.getSubPaths()))
            return List.of();
        return Arrays.asList(source.getSubPaths());
    }

    public void setSourceSubPaths(String[] subPaths){
        source.setSubPaths(subPaths);
    }

    public String getSourceUrl(){
        return this.source.getUrl();
    }

    public String getDestPath(){
        var transformedDestPath = dest.getPath().replace("/var/www/html",
                ConfigProvider.getConfig().getValue("sidecar.spadir", String.class));
        LOG.debug("transformed destination path is {}",transformedDestPath);
        return transformedDestPath;
    }

    public String getDestFileName(){
        return this.dest.getFilename();
    }


    @Override
    public String toString() {
        return "{"
                + "\"name\":\"" + name + "\""
                + ", \"interval\":\"" + interval + "\""
                + ", \"source\":" + source
                + ", \"dest\":" + dest
                + "}";
    }
}
