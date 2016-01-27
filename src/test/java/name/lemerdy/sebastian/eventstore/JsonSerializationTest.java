package name.lemerdy.sebastian.eventstore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSerializationTest {

    private Serializer serializer;
    private Deserializer deserializer;

    @Before
    public void createSerializers() {
        serializer = new Serializer();
        deserializer = new Deserializer();
    }

    @Test
    public void should_serialize_quotation_mark() {
        String serialized = serializer.apply("\"");

        assertThat(serialized).isEqualTo("\\\"");
    }

    @Test
    public void should_deserialize_quotation_mark() {
        String deserialized = deserializer.apply("\\\"");

        assertThat(deserialized).isEqualTo("\"");
    }

    @Test
    public void should_serialize_and_deserialize_quotation_mark() {
        String identity = serializer.andThen(deserializer).apply("\"");

        assertThat(identity).isEqualTo("\"");
    }

    @Test
    public void should_serialize_reverse_solidus() {
        String serialized = serializer.apply("\\");

        assertThat(serialized).isEqualTo("\\\\");
    }

    @Test
    public void should_deserialize_reverse_solidus() {
        String deserialized = deserializer.apply("\\\\");

        assertThat(deserialized).isEqualTo("\\");
    }

    @Test
    public void should_serialize_and_deserialize_reverse_solidus() {
        String identity = serializer.andThen(deserializer).apply("\\");

        assertThat(identity).isEqualTo("\\");
    }

    @Test
    public void should_serialize_solidus() {
        String serialized = serializer.apply("/");

        assertThat(serialized).isEqualTo("\\/");
    }

    @Test
    public void should_deserialize_solidus() {
        String deserialized = deserializer.apply("\\/");

        assertThat(deserialized).isEqualTo("/");
    }

    @Test
    public void should_serialize_and_deserialize_solidus() {
        String identity = serializer.andThen(deserializer).apply("/");

        assertThat(identity).isEqualTo("/");
    }

    @Test
    public void should_serialize_backspace() {
        String serialized = serializer.apply("\b");

        assertThat(serialized).isEqualTo("\\b");
    }

    @Test
    public void should_deserialize_backspace() {
        String deserialized = deserializer.apply("\\b");

        assertThat(deserialized).isEqualTo("\b");
    }

    @Test
    public void should_serialize_and_deserialize_backspace() {
        String identity = serializer.andThen(deserializer).apply("\b");

        assertThat(identity).isEqualTo("\b");
    }

    @Test
    public void should_serialize_formfeed() {
        String serialized = serializer.apply("\f");

        assertThat(serialized).isEqualTo("\\f");
    }

    @Test
    public void should_deserialize_formfeed() {
        String deserialized = deserializer.apply("\\f");

        assertThat(deserialized).isEqualTo("\f");
    }

    @Test
    public void should_serialize_and_deserialize_formfeed() {
        String identity = serializer.andThen(deserializer).apply("\f");

        assertThat(identity).isEqualTo("\f");
    }

    @Test
    public void should_serialize_carriage_return() {
        String serialized = serializer.apply("\n");

        assertThat(serialized).isEqualTo("\\n");
    }

    @Test
    public void should_deserialize_carriage_return() {
        String deserialized = deserializer.apply("\\n");

        assertThat(deserialized).isEqualTo("\n");
    }

    @Test
    public void should_serialize_and_deserialize_carriage_return() {
        String identity = serializer.andThen(deserializer).apply("\n");

        assertThat(identity).isEqualTo("\n");
    }

    @Test
    public void should_serialize_horizontal_tab() {
        String serialized = serializer.apply("\t");

        assertThat(serialized).isEqualTo("\\t");
    }

    @Test
    public void should_deserialize_horizontal_tab() {
        String deserialized = deserializer.apply("\\t");

        assertThat(deserialized).isEqualTo("\t");
    }

    @Test
    public void should_serialize_and_deserialize_horizontal_tab() {
        String identity = serializer.andThen(deserializer).apply("\t");

        assertThat(identity).isEqualTo("\t");
    }

    @Test
    public void should_not_serialize_unicode_character() {
        String deserialized = deserializer.apply("\\u0065");

        assertThat(deserialized).isEqualTo("\\u0065");
    }

    @Test
    public void should_not_deserialize_unicode_character() {
        String deserialized = deserializer.apply("\\u0065");

        assertThat(deserialized).isEqualTo("\\u0065");
    }

    @Test
    public void should_not_serialize_and_deserialize_unicode_character() {
        String identity = serializer.andThen(deserializer).apply("\\u0065");

        assertThat(identity).isEqualTo("\\u0065");
    }

    @Test
    @Ignore
    public void should_not_transform_double_backslash_and_n() {
        String deserialized = deserializer.apply("\\n");

        assertThat(deserialized).isEqualTo("\\n");
    }

    @Test
    @Ignore
    public void should_not_serialize_and_deserialize_transform_double_backslash_and_n() {
        String deserialized = serializer.andThen(deserializer).apply("\\n");

        assertThat(deserialized).isEqualTo("\\n");
    }

}