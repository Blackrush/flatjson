package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

public class FlatJsonIntrospector extends AnnotationIntrospector {
    @Override
    public Version version() {
        return FlatJsonModule.VERSION;
    }

    @Override
    public Object findSerializer(Annotated am) {
        Object serializer = super.findSerializer(am);

        if (serializer == null) {
            if (am.hasAnnotation(ManyToOne.class)) {
                serializer = Serializers.ManyToOne.class;
            } else if (am.hasAnnotation(OneToMany.class)) {
                serializer = Serializers.OneToMany.class;
            }
        }

        return serializer;
    }

    @Override
    public Object findDeserializer(Annotated am) {
        Object deserializer = super.findSerializer(am);

        if (deserializer == null) {
            if (am.hasAnnotation(ManyToOne.class)) {
                deserializer = new Deserializers.ManyToOne((AnnotatedMember) am);
            } else if (am.hasAnnotation(OneToMany.class)) {
                deserializer = new Deserializers.OneToMany((AnnotatedMember) am);
            }
        }

        return deserializer;
    }
}
