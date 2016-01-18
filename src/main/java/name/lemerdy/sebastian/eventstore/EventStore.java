package name.lemerdy.sebastian.eventstore;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class EventStore {

    private final Clock clock;
    private final SortedMap<Instant, List<Event>> storedEvents;
    private final Path eventsPath = Paths.get(".eventstore");

    public EventStore(Clock clock) {
        this.clock = clock;
        this.storedEvents = new TreeMap<>();
        this.load();
    }

    public List<Event> events() {
        return FLATTEN.apply(eventsAsStream());
    }

    public List<Event> events(String type) {
        return FILTER.andThen(FLATTEN).apply(eventsAsStream(), event -> type.equals(event.type));
    }

    public List<Event> events(Instant fromThisInstant) {
        return FILTER.andThen(FLATTEN).apply(eventsAsStream(), event -> fromThisInstant.equals(event.date) || fromThisInstant.isBefore(event.date));
    }

    public EventStore store(String type, String data) {
        return store(new Event(clock.instant(), type, data));
    }

    private EventStore store(Event event) {
        storedEvents.merge(event.date, singletonList(event), CONCAT);
        return this;
    }

    private void load() {
        try {
            Files.readAllLines(eventsPath).stream()
                    .map(currentLine -> currentLine.split(",", 3))
                    .map(elements -> new Event(Instant.parse(elements[0]), elements[1], elements[2].replaceAll("\\\\n", "\n")))
                    .forEach(this::store);
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void persist() {
        try (PrintWriter in = new PrintWriter(Files.newBufferedWriter(eventsPath))) {
            eventsAsStream().forEach(events ->
                    events.forEach(event ->
                            in.printf("%s,%s,%s%n", event.date, event.type, event.data.replaceAll("\n", "\\\\n"))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<List<Event>> eventsAsStream() {
        return storedEvents.values().stream();
    }

    private static final BinaryOperator<List<Event>> CONCAT = (events1, events2) -> {
        List<Event> events = new ArrayList<>();
        events.addAll(events1);
        events.addAll(events2);
        return events;
    };

    private static final Function<Stream<List<Event>>, List<Event>> FLATTEN = events ->
            events.reduce(emptyList(), CONCAT);

    private static final BiFunction<Stream<List<Event>>, Predicate<Event>, Stream<List<Event>>> FILTER = (events, filter) ->
            events.map(eventsWithSameDate -> eventsWithSameDate.stream().filter(filter).collect(toList()));

}
