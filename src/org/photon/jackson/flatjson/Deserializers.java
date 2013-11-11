
package org.photon.jackson.flatjson;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Hack;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import java.io.IOException;
import java.util.Collection;

public final class Deserializers {

    public static JavaType getJavaType(AnnotatedMember member, DeserializationContext ctx) {
        if (member instanceof AnnotatedMethod) {
            return ctx.constructType(((AnnotatedMethod) member).getGenericParameterType(0));
        }

        return ctx.constructType(member.getGenericType());
    }

    public static BeanDescription getBeanDescription(DeserializationContext ctx, JavaType jt) {
        return ctx.getConfig().getClassIntrospector()
                .forDeserializationWithBuilder(ctx.getConfig(), jt, ctx.getConfig());
    }

    public static AnnotatedMember getObjectIdMember(BeanDescription bd) {
        String property = bd.getObjectIdInfo().getPropertyName();

        for (BeanPropertyDefinition bpd : bd.findProperties()) {
            if (bpd.getName().equals(property)) {
                return bpd.getAccessor();
            }
        }
        return null;
    }

    public static ValueInstantiator getValueInstantiator(DeserializationContext ctx, JavaType jt) throws JsonMappingException {
        if (jt.isInterface()) {
            Class<? extends Collection> fallback = Hack.findCollectionCallback(jt.getRawClass().getName());
            if (fallback == null) throw new IllegalStateException(String.format(
                    "can't create `%s'", jt));

            return ctx.getFactory().findValueInstantiator(ctx, getBeanDescription(ctx, jt.narrowBy(fallback)));
        }

        return ctx.getFactory().findValueInstantiator(ctx, getBeanDescription(ctx, jt));
    }

    public static class ManyToOne extends JsonDeserializer<Object> {

        private final AnnotatedMember member;

        public ManyToOne(AnnotatedMember member) {
            this.member = member;
        }

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            JavaType jt = getJavaType(member, ctx);
            BeanDescription bd = getBeanDescription(ctx, jt);
            AnnotatedMember member = getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                        "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getType()));

            Object id = jp.readValueAs(member.getRawType());

            final ReadableObjectId roi = ctx.findObjectId(id, new FakeObjectIdGenerator(bd.getBeanClass(), member));
            if (roi.item != null) {
                return roi.item;
            } else {
                return Enhancer.create(this.member.getRawType(), new LazyLoader() {
                    @Override
                    public Object loadObject() throws Exception {
                        return roi.item;
                    }
                });
            }
        }
    }

    public static class OneToMany extends JsonDeserializer<Collection<Object>> {

        private final AnnotatedMember member;

        public OneToMany(AnnotatedMember member) {
            this.member = member;
        }

        @Override
        public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            JavaType jt = getJavaType(member, ctx);
            BeanDescription bd = getBeanDescription(ctx, jt.getContentType());
            AnnotatedMember member = getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                    "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getType()));

            @SuppressWarnings("unchecked")
            Collection<Object> result = (Collection<Object>) getValueInstantiator(ctx, jt)
                    .createUsingDefault(ctx);

            JsonDeserializer<Object> des = ctx.findRootValueDeserializer(CollectionType.construct(
                    jt.getRawClass(), ctx.constructType(member.getRawType())));

            for (Object id : (Iterable) des.deserialize(jp, ctx)) {
                final ReadableObjectId roi = ctx.findObjectId(id,
                        new FakeObjectIdGenerator(bd.getBeanClass(), member));

                if (roi.item != null) {
                    result.add(roi.item);
                } else {
                    result.add(Enhancer.create(bd.getBeanClass(), new LazyLoader() {
                        @Override
                        public Object loadObject() throws Exception {
                            return roi.item;
                        }
                    }));
                }
            }

            return result;
        }
    }

    public static class FakeObjectIdGenerator extends ObjectIdGenerator<Object> {
        private final Class<?> scope;
        private final AnnotatedMember member;

        public FakeObjectIdGenerator(Class<?> scope, AnnotatedMember member) {
            this.scope = scope;
            this.member = member;
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen.getScope() == this.getScope();
        }

        @Override
        public ObjectIdGenerator<Object> forScope(Class<?> scope) {
            return new FakeObjectIdGenerator(scope, member);
        }

        @Override
        public ObjectIdGenerator<Object> newForSerialization(Object context) {
            return this;
        }

        @Override
        public IdKey key(Object key) {
            return new IdKey(PropertyBasedObjectIdGenerator.class, scope, key);
        }

        @Override
        public Object generateId(Object forPojo) {
            return member.getValue(forPojo);
        }
    }
}
