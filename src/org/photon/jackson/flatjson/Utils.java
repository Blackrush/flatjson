package org.photon.jackson.flatjson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public final class Utils {
    private Utils() {}

    public static AnnotatedMember getObjectIdMember(BeanDescription bd) {
        String property = bd.getObjectIdInfo().getPropertyName();

        for (BeanPropertyDefinition bpd : bd.findProperties()) {
            if (bpd.getName().equals(property)) {
                return bpd.getAccessor();
            }
        }
        return null;
    }
}
