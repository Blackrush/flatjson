package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;
import java.util.List;

public final class Serializers {

    public static class ManyToOne extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            BeanDescription bd = provider.getConfig().getClassIntrospector()
                    .forSerialization(provider.getConfig(), SimpleType.construct(value.getClass()), provider.getConfig());
            String property = bd.getObjectIdInfo().getPropertyName();

            AnnotatedMember member = null;

            for (BeanPropertyDefinition bpd : bd.findProperties()) {
                if (bpd.getName().equals(property)) {
                    member = bpd.getAccessor();
                    break;
                }
            }

            if (member == null)
                throw new IllegalStateException(String.format("unknown property `%s' on `%s'",
                        property, value.getClass().getName()));

            provider.defaultSerializeValue(member.getValue(value), jgen);
        }
    }

    public static class OneToMany extends JsonSerializer<List<Object>> {

        @Override
        public void serialize(List<Object> values, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();

            if (!values.isEmpty()) {
                Class<?> clazz = values.get(0).getClass();
                BeanDescription bd = provider.getConfig().getClassIntrospector()
                        .forSerialization(provider.getConfig(), SimpleType.construct(clazz), provider.getConfig());
                String property = bd.getObjectIdInfo().getPropertyName();

                AnnotatedMember member = null;

                for (BeanPropertyDefinition bpd : bd.findProperties()) {
                    if (bpd.getName().equals(property)) {
                        member = bpd.getAccessor();
                        break;
                    }
                }

                if (member == null) throw new IllegalStateException(String.format("unknown property `%s' on `%s'", property, clazz.getName()));

                for (Object value : values) {
                    Object id = member.getValue(value);
                    provider.defaultSerializeValue(id, jgen);
                }
            }

            jgen.writeEndArray();
        }
    }
}
