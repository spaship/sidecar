package io.spaship.sidecar.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class TargetEntry {

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
        return Arrays.asList(source.getSubPaths());
    }

    public String getSourceUrl(){
        return this.source.getUrl();
    }

    public String getDestPath(){
        return this.dest.getPath();
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
