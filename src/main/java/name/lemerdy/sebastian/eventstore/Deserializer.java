package name.lemerdy.sebastian.eventstore;

import java.util.function.Function;

public class Deserializer implements Function<String, String> {

    @Override
    public String apply(String s) {
        return s
                .replaceAll("\\\\\\\\", "\\\\")
                .replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\/", "/")
                .replaceAll("\\\\b", "\b")
                .replaceAll("\\\\f", "\f")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t");
    }

}
