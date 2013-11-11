package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import java.io.IOException;

@JsonSerialize(using = Ref.RefSerializer.class)
public class Ref<T> {
    private T value;

    public Ref() { }

    public Ref(T value) {
        this.value = value;
    }

    public T get() {
        if (value == null) throw new NullPointerException();
        return value;
    }

    public T nullable() {
        return value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isPresent() {
        return !isNull();
    }

    public void set(T value) {
        this.value = value;
    }

    public static class RefConverter<T> implements Converter<T, Ref<T>> {
        private final Class<T> inputClass;

        public RefConverter(Class<T> inputClass) {
            this.inputClass = inputClass;
        }

        @Override
        public Ref<T> convert(T value) {
            return new Ref<>(value);
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(inputClass);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructSimpleType(Ref.class, new JavaType[] {getInputType(typeFactory)});
        }
    }

    public static class RefSerializer extends JsonSerializer<Ref<?>> {
        @Override
        public void serialize(Ref<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value.isNull()) {
                provider.defaultSerializeNull(jgen);
            } else {
                provider.defaultSerializeValue(value.get(), jgen);
            }
        }
    }
}
