package io.spaship.sidecar.type;

import java.nio.file.Path;
import java.util.UUID;

public class Environment {

    private String name;
    private String websiteName;
    private UUID traceID;
    private String nameSpace;
    private boolean updateRestriction;
    private Path zipFileLocation;
    private String websiteVersion;
    private String spaName;
    private String spaContextPath;
    private String branch;
    private boolean excludeFromEnvironment; //to create or not to create :P
    private boolean operationPerformed = false; // for flagging purpose, to know whether any k8s operation is performed
    private String identification;

    Environment(String name, String websiteName, UUID traceID, String nameSpace, boolean updateRestriction, Path zipFileLocation, String websiteVersion, String spaName, String spaContextPath, String branch, boolean excludeFromEnvironment, boolean operationPerformed, String identification) {
        this.name = name;
        this.websiteName = websiteName;
        this.traceID = traceID;
        this.nameSpace = nameSpace;
        this.updateRestriction = updateRestriction;
        this.zipFileLocation = zipFileLocation;
        this.websiteVersion = websiteVersion;
        this.spaName = spaName;
        this.spaContextPath = spaContextPath;
        this.branch = branch;
        this.excludeFromEnvironment = excludeFromEnvironment;
        this.operationPerformed = operationPerformed;
        this.identification = identification;
    }

    public static EnvironmentBuilder builder() {
        return new EnvironmentBuilder();
    }

    @Override
    public String toString() {
        return "{"
                + "\"name\":\"" + name + "\""
                + ", \"websiteName\":\"" + websiteName + "\""
                + ", \"traceID\":" + traceID
                + ", \"nameSpace\":\"" + nameSpace + "\""
                + ", \"updateRestriction\":\"" + updateRestriction + "\""
                + ", \"zipFileLocation\":" + zipFileLocation
                + ", \"websiteVersion\":\"" + websiteVersion + "\""
                + ", \"spaName\":\"" + spaName + "\""
                + ", \"spaContextPath\":\"" + spaContextPath + "\""
                + ", \"branch\":\"" + branch + "\""
                + ", \"excludeFromEnvironment\":\"" + excludeFromEnvironment + "\""
                + ", \"operationPerformed\":\"" + operationPerformed + "\""
                + ", \"identification\":\"" + identification + "\""
                + "}";
    }

    public static class EnvironmentBuilder {
        private String name;
        private String websiteName;
        private UUID traceID;
        private String nameSpace;
        private boolean updateRestriction;
        private Path zipFileLocation;
        private String websiteVersion;
        private String spaName;
        private String spaContextPath;
        private String branch;
        private boolean excludeFromEnvironment;
        private boolean operationPerformed;
        private String identification;

        EnvironmentBuilder() {
        }

        public EnvironmentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EnvironmentBuilder websiteName(String website) {
            this.websiteName = refactorWebsiteName(website);
            return this;
        }

        private String refactorWebsiteName(String website) {
            var modifiedWebsiteName = website.replace(".", "-");
            modifiedWebsiteName = modifiedWebsiteName.replace("_", "-");
            return modifiedWebsiteName;
        }

        public EnvironmentBuilder traceID(UUID traceID) {
            this.traceID = traceID;
            return this;
        }

        public EnvironmentBuilder nameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
            return this;
        }

        public EnvironmentBuilder updateRestriction(boolean updateRestriction) {
            this.updateRestriction = updateRestriction;
            return this;
        }

        public EnvironmentBuilder zipFileLocation(Path zipFileLocation) {
            this.zipFileLocation = zipFileLocation;
            return this;
        }

        public EnvironmentBuilder websiteVersion(String websiteVersion) {
            this.websiteVersion = websiteVersion;
            return this;
        }

        public EnvironmentBuilder spaName(String spaName) {
            this.spaName = spaName;
            return this;
        }

        public EnvironmentBuilder spaContextPath(String spaContextPath) {
            this.spaContextPath = spaContextPath;
            return this;
        }

        public EnvironmentBuilder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public EnvironmentBuilder excludeFromEnvironment(boolean excludeFromEnvironment) {
            this.excludeFromEnvironment = excludeFromEnvironment;
            return this;
        }

        public EnvironmentBuilder operationPerformed(boolean operationPerformed) {
            this.operationPerformed = operationPerformed;
            return this;
        }


        public Environment build() {
            identification = websiteName.concat("-").concat(name);
            return new Environment(name, websiteName, traceID, nameSpace, updateRestriction, zipFileLocation, websiteVersion, spaName, spaContextPath, branch, excludeFromEnvironment, operationPerformed, identification);
        }

        @Override
        public String toString() {
            return "{"
                    + "\"name\":\"" + name + "\""
                    + ", \"websiteName\":\"" + websiteName + "\""
                    + ", \"traceID\":" + traceID
                    + ", \"nameSpace\":\"" + nameSpace + "\""
                    + ", \"updateRestriction\":\"" + updateRestriction + "\""
                    + ", \"zipFileLocation\":" + zipFileLocation
                    + ", \"websiteVersion\":\"" + websiteVersion + "\""
                    + ", \"spaName\":\"" + spaName + "\""
                    + ", \"spaContextPath\":\"" + spaContextPath + "\""
                    + ", \"branch\":\"" + branch + "\""
                    + ", \"excludeFromEnvironment\":\"" + excludeFromEnvironment + "\""
                    + ", \"operationPerformed\":\"" + operationPerformed + "\""
                    + ", \"identification\":\"" + identification + "\""
                    + "}";
        }
    }
}
