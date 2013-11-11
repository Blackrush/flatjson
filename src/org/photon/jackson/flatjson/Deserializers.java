package org.photon.jackson.flatjson;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import java.io.IOException;

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

    public static class ManyToOne extends JsonDeserializer<Object> {

        private final AnnotatedMember member;

        public ManyToOne(AnnotatedMember member) {
            this.member = member;
        }

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            JavaType jt = getJavaType(member, ctx);
            BeanDescription bd = getBeanDescription(ctx, jt);
            AnnotatedMember member = Utils.getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                        "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getType()));

            Object id = jp.readValueAs(member.getRawType());

            final ReadableObjectId roi = ctx.findObjectId(id, new FakeObjectIdGenerator(bd.getBeanClass(), member));
            if (roi.item != null) {
                return roi.item;
            } else {
                return Enhancer.create(jt.getRawClass(), new LazyLoader() {
                    @Override
                    public Object loadObject() throws Exception {
                        return roi.item;
                    }
                });
            }
        }
    }

    public static class OneToMany extends JsonDeserializer<Object> {

        private final AnnotatedMember member;

        public OneToMany(AnnotatedMember member) {
            this.member = member;
        }

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            JavaType jt = getJavaType(member, ctx).getContentType();
            BeanDescription bd = getBeanDescription(ctx, jt);
            AnnotatedMember member = Utils.getObjectIdMember(bd);

            if (member == null) throw new IllegalStateException(String.format(
                    "unknown property `%s' on `%s'", bd.getObjectIdInfo().getPropertyName(), bd.getType()));

            Object id = jp.readValueAs(member.getRawType());

            final ReadableObjectId roi = ctx.findObjectId(id, new FakeObjectIdGenerator(bd.getBeanClass(), member));
            if (roi.item != null) {
                return roi.item;
            } else {
                return Enhancer.create(jt.getRawClass(), new LazyLoader() {
                    @Override
                    public Object loadObject() throws Exception {
                        return roi.item;
                    }
                });
            }
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
