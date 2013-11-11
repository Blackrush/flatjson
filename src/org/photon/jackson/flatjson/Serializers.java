package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;

public final class Serializers {

    public static BeanDescription getBeanDescription(Class<?> value, SerializerProvider provider) {
        return provider.getConfig().getClassIntrospector()
                .forSerialization(provider.getConfig(), SimpleType.construct(value), provider.getConfig());
    }

    public static class ManyToOne extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            BeanDescription bd = getBeanDescription(value.getClass(), provider);
            AnnotatedMember member = Utils.getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                    "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getClassInfo()));

            provider.defaultSerializeValue(member.getValue(value), jgen);
        }
    }

    public static class OneToMany extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            BeanDescription bd = getBeanDescription(value.getClass(), provider);
            AnnotatedMember member = Utils.getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                    "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getClassInfo()));

            provider.defaultSerializeValue(member.getValue(value), jgen);
        }
    }
}
