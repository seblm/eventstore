package name.lemerdy.sebastian.eventstore;

import name.lemerdy.sebastian.eventstore.testtools.DataFileRemover;
import name.lemerdy.sebastian.eventstore.testtools.IncrementingClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.simpleframework.http.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EventStoreHttpTest {

    @Rule
    public DataFileRemover dataFileRemover = new DataFileRemover();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Request req;

    @Mock
    private Response resp;

    @Mock
    private Path path;

    private Instant now;

    private EventStore eventStore;

    private EventStoreHttp eventStoreHttp;

    private Optional<String> previousPassword;

    @Before
    public void initializeEventStore() {
        now = Instant.now();
        eventStore = new EventStore(new IncrementingClock(now));
        eventStoreHttp = new EventStoreHttp(eventStore);
    }

    @Before
    public void mockRequest() {
        when(req.getValue(Protocol.AUTHORIZATION)).thenReturn("Basic " + Base64.getMimeEncoder().encodeToString("user:password".getBytes()));
        when(req.getPath()).thenReturn(path);
    }

    @Before
    public void setPassword() {
        previousPassword = Optional.ofNullable(System.setProperty("password", "password"));
    }

    @After
    public void resetPassword() {
        if (previousPassword.isPresent()) {
            System.setProperty("password", previousPassword.get());
        } else {
            System.clearProperty("password");
        }
    }

    @Test
    public void should_store_event() {
        when(path.getPath()).thenReturn("/events/name.lemerdy.sebastian.typeA");
        when(path.getSegments()).thenReturn(new String[]{"events", "name.lemerdy.sebastian.typeA"});
        when(req.getMethod()).thenReturn(Method.POST);
        try {
            when(req.getContent()).thenReturn("this is my data");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        eventStoreHttp.handle(req, resp);

        verify(resp).setStatus(Status.CREATED);
        assertThat(eventStore.events()).containsExactly(new Event(now, "name.lemerdy.sebastian.typeA", "this is my data"));
        verifyRespIsClosed();
    }

    @Test
    public void should_retrieve_all_events() {
        eventStore
                .store("name.lemerdy.sebastian.typeA", "data0")
                .store("name.lemerdy.sebastian.typeA", "data1")
                .store("name.lemerdy.sebastian.typeB", "data2");
        when(path.getPath()).thenReturn("/events");
        when(path.getSegments()).thenReturn(new String[]{"events"});
        PrintStream respContent = mockRespContent();

        eventStoreHttp.handle(req, resp);

        ArgumentCaptor<String> responseContent = ArgumentCaptor.forClass(String.class);
        verify(respContent).print(responseContent.capture());
        assertThat(responseContent.getValue()).isEqualTo("" +
                "[\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(0) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.typeA\",\n" +
                "    \"data\": \"data0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(1) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.typeA\",\n" +
                "    \"data\": \"data1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(2) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.typeB\",\n" +
                "    \"data\": \"data2\"\n" +
                "  }\n" +
                "]");
        verifyRespIsClosed();
    }

    @Test
    public void should_retrieve_all_events_for_a_given_type() {
        eventStore
                .store("name.lemerdy.sebastian.typeA", "data0")
                .store("name.lemerdy.sebastian.typeB", "data1")
                .store("name.lemerdy.sebastian.typeA", "data2");
        when(path.getPath()).thenReturn("/events/name.lemerdy.sebastian.typeA");
        when(path.getSegments()).thenReturn(new String[]{"events", "name.lemerdy.sebastian.typeA"});
        PrintStream respContent = mockRespContent();

        eventStoreHttp.handle(req, resp);

        ArgumentCaptor<String> responseContent = ArgumentCaptor.forClass(String.class);
        verify(respContent).print(responseContent.capture());
        assertThat(responseContent.getValue()).isEqualTo("" +
                "[\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(0) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.typeA\",\n" +
                "    \"data\": \"data0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(2) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.typeA\",\n" +
                "    \"data\": \"data2\"\n" +
                "  }\n" +
                "]");
        verifyRespIsClosed();
    }

    @Test
    public void should_retrieve_all_events_starting_from_some_instant() {
        eventStore
                .store("name.lemerdy.sebastian.type", "data0")
                .store("name.lemerdy.sebastian.type", "data1")
                .store("name.lemerdy.sebastian.type", "data2");
        when(path.getPath()).thenReturn("/events/" + now.plusSeconds(1));
        when(path.getSegments()).thenReturn(new String[]{"events", now.plusSeconds(1).toString()});
        PrintStream respContent = mockRespContent();

        eventStoreHttp.handle(req, resp);

        ArgumentCaptor<String> responseContent = ArgumentCaptor.forClass(String.class);
        verify(respContent).print(responseContent.capture());
        assertThat(responseContent.getValue()).isEqualTo("" +
                "[\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(1) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.type\",\n" +
                "    \"data\": \"data1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"date\": \"" + now.plusSeconds(2) + "\",\n" +
                "    \"type\": \"name.lemerdy.sebastian.type\",\n" +
                "    \"data\": \"data2\"\n" +
                "  }\n" +
                "]");
        verifyRespIsClosed();
    }

    @Test
    public void should_not_authorize_unauthenticated_request() {
        when(req.getValue(Protocol.AUTHORIZATION)).thenReturn(null);

        eventStoreHttp.handle(req, resp);

        verify(resp).setStatus(Status.UNAUTHORIZED);
        verifyRespIsClosed();
    }

    @Test
    public void should_not_authorize_bad_password_request() {
        when(req.getValue(Protocol.AUTHORIZATION)).thenReturn("Basic " + Base64.getMimeEncoder().encodeToString("user:badpassword".getBytes()));

        eventStoreHttp.handle(req, resp);

        verify(resp).setStatus(Status.UNAUTHORIZED);
        verifyRespIsClosed();
    }

    @Test
    public void should_not_found_if_query_is_unknown() {
        when(path.getPath()).thenReturn("/not-found");

        eventStoreHttp.handle(req, resp);

        verify(resp).setStatus(Status.NOT_FOUND);
        verifyRespIsClosed();
    }

    private void verifyRespIsClosed() {
        try {
            verify(resp).close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PrintStream mockRespContent() {
        PrintStream respContent = mock(PrintStream.class);
        try {
            when(resp.getPrintStream()).thenReturn(respContent);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return respContent;
    }

}
