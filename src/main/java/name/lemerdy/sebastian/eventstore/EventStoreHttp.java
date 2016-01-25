package name.lemerdy.sebastian.eventstore;

import org.simpleframework.http.Method;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class EventStoreHttp implements Container {

    private final EventStore eventStore;
    private final Serializer serializer;

    public EventStoreHttp(EventStore eventStore) {
        this.eventStore = eventStore;
        this.serializer = new Serializer();
    }

    @Override
    public void handle(Request req, Response resp) {
        try {
            if (!req.getPath().getPath().startsWith("/events")) {
                notFound(resp);
                return;
            }

            String[] segments = req.getPath().getSegments();

            if (segments.length == 2 && Method.POST.equals(req.getMethod())) {
                store(req, resp, segments[1]);
                return;
            }

            events(req, resp);
        } finally {
            try {
                resp.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void notFound(Response resp) {
        resp.setStatus(Status.NOT_FOUND);
    }

    private void events(Request req, Response resp) {

        Optional<String> maybeFilter;
        try {
            maybeFilter = Optional.of(req.getPath().getSegments()[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            maybeFilter = Optional.empty();
        }

        resp.setContentType("application/json");

        PrintStream printStream;
        try {
            printStream = resp.getPrintStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        printStream.print(maybeFilter.map(startingFromOrType -> {
            try {
                return eventStore.events(Instant.parse(startingFromOrType));
            } catch (DateTimeParseException e) {
                return eventStore.events(startingFromOrType);
            }
        })
                .orElseGet(eventStore::events)
                .stream()
                .map(event -> "" +
                        "  {\n" +
                        "    \"date\": \"" + event.date + "\",\n" +
                        "    \"type\": \"" + event.type + "\",\n" +
                        "    \"data\": \"" + serializer.apply(event.data) + "\"\n" +
                        "  }")
                .collect(joining(",\n", "[\n", "\n]")));
    }

    private void store(Request req, Response resp, String type) {
        String content;
        try {
            content = req.getContent();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        eventStore.store(type, content);
        eventStore.persist();
        resp.setStatus(Status.CREATED);
    }

    private void start() {
        try {
            ContainerSocketProcessor containerSocketProcessor = new ContainerSocketProcessor(new EventStoreHttp(eventStore));
            SocketConnection socketConnection = new SocketConnection(containerSocketProcessor);
            socketConnection.connect(new InetSocketAddress(8080));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        new EventStoreHttp(new EventStore(Clock.systemUTC())).start();
    }
}
