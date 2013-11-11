package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class FlatJsonModule extends Module {
    public static final Version VERSION = new Version(0, 1, 0, "", "org.photon.jackson.flatjson", "flatjson");

    @Override
    public String getModuleName() {
        return "flatjson";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.appendAnnotationIntrospector(new FlatJsonIntrospector());
    }
}
