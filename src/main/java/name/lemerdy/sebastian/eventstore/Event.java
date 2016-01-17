package name.lemerdy.sebastian.eventstore;

import lombok.Value;

import java.time.Instant;

@Value
public class Event {
    public Instant date;
    public String type;
    public String data;
}
