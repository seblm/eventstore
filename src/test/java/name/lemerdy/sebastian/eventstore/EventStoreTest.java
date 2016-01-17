package name.lemerdy.sebastian.eventstore;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class EventStoreTest {

    @Test
    public void should_store_event() {
        Instant now = now();
        EventStore eventStore = new EventStore(fixed(now, ZoneId.systemDefault()));

        eventStore.store("name.lemerdy.sebastian.type", "data");

        List<Event> events = eventStore.events();
        assertThat(events).containsExactly(new Event(now, "name.lemerdy.sebastian.type", "data"));
    }

    @Test
    public void should_retrieve_all_events_in_chronological_order() {
        EventStore eventStore = new EventStore(new IncrementingClock(now()));
        eventStore.store("name.lemerdy.sebastian.type", "data_1");
        eventStore.store("name.lemerdy.sebastian.type", "data_2");

        List<Event> events = eventStore.events();

        assertThat(events).extracting(Event::getType, Event::getData).containsExactly(
                tuple("name.lemerdy.sebastian.type", "data_1"),
                tuple("name.lemerdy.sebastian.type", "data_2"));
    }


    @Test
    public void should_retrieve_all_events_for_a_given_type() {
        EventStore eventStore = new EventStore(new IncrementingClock(now()));
        eventStore.store("name.lemerdy.sebastian.type1", "data_1");
        eventStore.store("name.lemerdy.sebastian.type2", "data_2");
        eventStore.store("name.lemerdy.sebastian.type1", "data_3");

        List<Event> events = eventStore.events("name.lemerdy.sebastian.type1");

        assertThat(events).extracting(Event::getType, Event::getData).containsExactly(
                tuple("name.lemerdy.sebastian.type1", "data_1"),
                tuple("name.lemerdy.sebastian.type1", "data_3")
        );
    }

    @Test
    public void should_retrieve_all_events_starting_from_some_instant() {
        Instant now = now();
        EventStore eventStore = new EventStore(new IncrementingClock(now));
        eventStore.store("name.lemerdy.sebastian.type", "data_1");
        eventStore.store("name.lemerdy.sebastian.type", "data_2");
        eventStore.store("name.lemerdy.sebastian.type", "data_3");

        List<Event> events = eventStore.events(now.plusSeconds(1));

        assertThat(events).extracting(Event::getType, Event::getData).containsExactly(
                tuple("name.lemerdy.sebastian.type", "data_2"),
                tuple("name.lemerdy.sebastian.type", "data_3")
        );
    }

}
