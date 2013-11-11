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

        if (serializer == null && am.hasAnnotation(ManyToOne.class)) {
            serializer = Serializers.ManyToOne.class;
        }

        return serializer;
    }

    @Override
    public Object findContentSerializer(Annotated am) {
        Object serializer = super.findContentSerializer(am);


        if (serializer == null && am.hasAnnotation(OneToMany.class)) {
            serializer = Serializers.OneToMany.class;
        }

        return serializer;
    }

    @Override
    public Object findDeserializer(Annotated am) {
        Object deserializer = super.findSerializer(am);

        if (deserializer == null && am.hasAnnotation(ManyToOne.class)) {
            deserializer = new Deserializers.ManyToOne((AnnotatedMember) am);
        }

        return deserializer;
    }

    @Override
    public Object findContentDeserializer(Annotated am) {

        Object deserializer = super.findSerializer(am);

        if (deserializer == null && am.hasAnnotation(OneToMany.class)) {
            deserializer = new Deserializers.OneToMany((AnnotatedMember) am);
        }

        return deserializer;
    }
}
