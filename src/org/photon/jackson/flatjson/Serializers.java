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

    public static Object first(Iterable<Object> values) {
        return values.iterator().next();
    }

    public static boolean isEmpty(Iterable<Object> values) {
        return !values.iterator().hasNext();
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

    public static class OneToMany extends JsonSerializer<Iterable<Object>> {

        @Override
        public void serialize(Iterable<Object> values, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            if (!Serializers.isEmpty(values)) {
                BeanDescription bd = getBeanDescription(Serializers.first(values).getClass(), provider);
                AnnotatedMember member = Utils.getObjectIdMember(bd);

                if (member == null) throw new IllegalStateException(String.format(
                        "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getClassInfo()));

                for (Object value : values) {
                    Object id = member.getValue(value);
                    provider.defaultSerializeValue(id, jgen);
                }
            }

            jgen.writeEndArray();
        }
    }
}
