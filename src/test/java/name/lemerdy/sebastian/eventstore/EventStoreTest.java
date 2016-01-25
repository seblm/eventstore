package name.lemerdy.sebastian.eventstore;

import name.lemerdy.sebastian.eventstore.testtools.DataFileRemover;
import name.lemerdy.sebastian.eventstore.testtools.IncrementingClock;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

public class EventStoreTest {

    @Rule
    public DataFileRemover dataFileRemover = new DataFileRemover();

    @Test
    public void should_store_event() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now));

        eventStore.store("name.lemerdy.sebastian.type", "data");

        assertThat(eventStore.events()).containsExactly(
                new Event(now, "name.lemerdy.sebastian.type", "data"));
    }

    @Test
    public void should_retrieve_all_events_in_chronological_order() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.type", "data_1")
                .store("name.lemerdy.sebastian.type", "data_2");

        List<Event> events = eventStore.events();

        assertThat(events).containsExactly(
                new Event(now.plusSeconds(0), "name.lemerdy.sebastian.type", "data_1"),
                new Event(now.plusSeconds(1), "name.lemerdy.sebastian.type", "data_2"));
    }


    @Test
    public void should_retrieve_all_events_for_a_given_type() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.typeA", "data_0")
                .store("name.lemerdy.sebastian.typeB", "data_1")
                .store("name.lemerdy.sebastian.typeA", "data_2");

        List<Event> events = eventStore.events("name.lemerdy.sebastian.typeA");

        assertThat(events).extracting(Event::getType)
                .allMatch("name.lemerdy.sebastian.typeA"::equals)
                .hasSize(2);
    }

    @Test
    public void should_retrieve_all_events_starting_from_some_instant() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.type", "data_0")
                .store("name.lemerdy.sebastian.type", "data_1")
                .store("name.lemerdy.sebastian.type", "data_2");

        List<Event> events = eventStore.events(now.plusSeconds(1));

        assertThat(events).containsExactly(
                new Event(now.plusSeconds(1), "name.lemerdy.sebastian.type", "data_1"),
                new Event(now.plusSeconds(2), "name.lemerdy.sebastian.type", "data_2"));
    }

    @Test
    public void should_persist_all_events_to_disk() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.type", "data_0")
                .store("name.lemerdy.sebastian.type", "data_1")
                .store("name.lemerdy.sebastian.type", "data_2");

        eventStore.persist();

        assertThat(new EventStore(Clock.systemUTC()).events()).containsExactly(
                new Event(now.plusSeconds(0), "name.lemerdy.sebastian.type", "data_0"),
                new Event(now.plusSeconds(1), "name.lemerdy.sebastian.type", "data_1"),
                new Event(now.plusSeconds(2), "name.lemerdy.sebastian.type", "data_2"));
    }

    @Test
    public void should_persist_event_with_carriage_return() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.type", "data_0\nnew line");

        eventStore.persist();

        assertThat(new EventStore(Clock.systemUTC()).events()).containsExactly(
                new Event(now.plusSeconds(0), "name.lemerdy.sebastian.type", "data_0\nnew line"));
    }


    @Test
    public void should_modify_persisted_data_in_case_of_double_backslash_and_n() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now))
                .store("name.lemerdy.sebastian.type", "data_0\\nnew line");

        eventStore.persist();

        assertThat(new EventStore(Clock.systemUTC()).events()).containsExactly(
                new Event(now.plusSeconds(0), "name.lemerdy.sebastian.type", "data_0\nnew line"));
    }

}
