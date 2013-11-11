package org.photon.jackson.flatjson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class FlatJsonTest {

    private ObjectMapper mapper;

    @Before
    public void initMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new FlatJsonModule());
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
