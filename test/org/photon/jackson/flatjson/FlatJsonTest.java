package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.Converter;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class FlatJsonTest {

    private ObjectMapper mapper;

    @Before
    public void initMapper() {
        mapper = new ObjectMapper();

        mapper.registerModule(new FlatJsonModule());

        mapper.setHandlerInstantiator(new HandlerInstantiatorAdapter() {
            @Override
            public Converter<?, ?> converterInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                if (implClass == Ref.class) {
                    ParameterizedType type = (ParameterizedType) ((AnnotatedMethod) annotated).getGenericParameterType(0);
                    //noinspection unchecked
                    return new Ref.RefConverter<>((Class<Object>) type.getActualTypeArguments()[0]);
                }

                return null;
            }
        });
    }

    @After
    public void releaseMapper() {
        mapper = null; // not necessary
    }

    public Registry createRegistry() {
        Parent daddy = new Parent();
        daddy.setId(1);
        daddy.setName("daddy");

        Child junior = new Child();
        junior.setId(1);
        junior.setName("junior");

        Child sister = new Child();
        sister.setId(2);
        sister.setName("junior's sister");

        daddy.addChild(junior).addChild(sister);

        Registry registry = new Registry();
        return registry.addParent(daddy).addChild(junior).addChild(sister);
    }

    public Registry2 createRegistry2() {
        Parent2 daddy = new Parent2();
        daddy.setId(1);
        daddy.setName("daddy");

        Child2 junior = new Child2();
        junior.setId(1);
        junior.setName("junior");

        Child2 sister = new Child2();
        sister.setId(2);
        sister.setName("junior's sister");

        daddy.addChild(junior).addChild(sister);

        Registry2 registry = new Registry2();
        registry.addParent(daddy).addChild(junior).addChild(sister);
        return registry;
    }

    @Test
    public void serialize() throws JsonProcessingException {
        Registry registry = createRegistry();

        String json = mapper.writeValueAsString(registry);

        assertThat(json, equalTo(
                "{" +
                    "\"parents\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"daddy\"," +
                            "\"children\":[1,2]" +
                        "}" +
                    "]," +
                    "\"children\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"junior\"," +
                            "\"parent\":1" +
                        "},{" +
                            "\"id\":2," +
                            "\"name\":\"junior's sister\"," +
                            "\"parent\":1" +
                        "}" +
                    "]" +
                "}"));
    }

    @Test
    public void serializeWithRef() throws JsonProcessingException {
        Registry2 registry = createRegistry2();

        String json = mapper.writeValueAsString(registry);

        assertThat(json, equalTo(
                "{" +
                        "\"parents\":[" +
                        "{" +
                        "\"id\":1," +
                        "\"name\":\"daddy\"," +
                        "\"children\":[1,2]" +
                        "}" +
                        "]," +
                        "\"children\":[" +
                        "{" +
                        "\"id\":1," +
                        "\"name\":\"junior\"," +
                        "\"parentRef\":1" +
                        "},{" +
                        "\"id\":2," +
                        "\"name\":\"junior's sister\"," +
                        "\"parentRef\":1" +
                        "}" +
                        "]" +
                        "}"));
    }

    @Test
    public void deserialize() throws IOException {
        String json =
                "{" +
                    "\"parents\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"daddy\"," +
                            "\"children\":[1,2]" +
                        "}" +
                    "]," +
                    "\"children\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"junior\"," +
                            "\"parent\":1" +
                        "},{" +
                            "\"id\":2," +
                            "\"name\":\"junior's sister\"," +
                            "\"parent\":1" +
                        "}" +
                    "]" +
                "}";

        Registry registry = mapper.readValue(json, Registry.class);

        assertThat(registry.getParents(), hasSize(1));
        assertThat(registry.getChildren(), hasSize(2));

        Parent daddy = registry.getParents().get(0);
        assertThat(daddy.getId(), equalTo(1L));
        assertThat(daddy.getName(), equalTo("daddy"));
        assertThat(daddy.getChildren(), hasSize(2));

        Child junior = registry.getChildren().get(0);
        assertThat(junior.getId(), equalTo(1L));
        assertThat(junior.getName(), equalTo("junior"));
        assertThat(junior.getParent(), equalTo(daddy));

        Child sister = registry.getChildren().get(1);
        assertThat(sister.getId(), equalTo(2L));
        assertThat(sister.getName(), equalTo("junior's sister"));
        assertThat(sister.getParent(), equalTo(daddy));
    }

    @Test
    public void deserializeWithRef() throws IOException {
        String json =
                "{" +
                    "\"parents\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"daddy\"," +
                            "\"children\":[1,2]" +
                            "}" +
                        "]," +
                    "\"children\":[" +
                        "{" +
                            "\"id\":1," +
                            "\"name\":\"junior\"," +
                            "\"parentRef\":1" +
                        "},{" +
                            "\"id\":2," +
                            "\"name\":\"junior's sister\"," +
                            "\"parentRef\":1" +
                        "},{" +
                            "\"id\":3," +
                            "\"name\":\"orphan\"," +
                            "\"parentRef\":null" +
                        "}" +
                    "]" +
                "}";

        Registry2 registry = mapper.readValue(json, Registry2.class);

        Parent2 daddy = registry.getParents().get(0);
        assertThat(daddy.getName(), equalTo("daddy"));

        Child2 junior = registry.getChildren().get(0);
        assertThat(junior.getName(), equalTo("junior"));

        assertThat(junior.getParentRef().nullable(), equalTo(daddy));

        Child2 orphan = registry.getChildren().get(2);
        assertThat(orphan.getName(), equalTo("orphan"));
        assertThat(orphan.getParentRef(), nullValue());
    }


    public static class SizeMatcher extends TypeSafeMatcher<Collection<?>> {

        private final int expectedSize;

        public SizeMatcher(int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        protected boolean matchesSafely(Collection<?> item) {
            return item.size() == expectedSize;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("collection of size ");
            description.appendValue(expectedSize);
        }
    }

    public static SizeMatcher hasSize(int expectedSize) {
        return new SizeMatcher(expectedSize);
    }

}
