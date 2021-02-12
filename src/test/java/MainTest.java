
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import ratpack.http.client.ReceivedResponse;
import ratpack.sse.ServerSentEvents;
import ratpack.test.embed.EmbeddedApp;
import ratpack.websocket.WebSockets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ratpack.http.ResponseChunks.stringChunks;
import static ratpack.sse.ServerSentEvents.serverSentEvents;
import static ratpack.stream.Streams.periodically;

public class MainTest {

    @Test
    public void responseChunksTest() throws Exception {
        EmbeddedApp.fromHandler(ctx -> {
            Publisher<String> strings = periodically(ctx, Duration.ofMillis(10),
                    i -> i < 5 ? i.toString() : null
            );

            ctx.render(stringChunks(strings));
        }).test(httpClient -> {
            System.out.println(httpClient.getText());
            assertEquals("01234", httpClient.getText());
        });
    }

    @Test
    public void sendEventsTest() throws Exception {
        EmbeddedApp.fromHandler(context -> {
            Publisher<String> stream = periodically(context, Duration.ofMillis(5), i ->
                    i < 5 ? i.toString() : null
            );

            ServerSentEvents events = serverSentEvents(stream, e ->
                    e.id(Objects::toString).event("counter").data(i -> "event " + i)
            );

            context.render(events);
        }).test(httpClient -> {
            ReceivedResponse response = httpClient.get();
            assertEquals("text/event-stream;charset=UTF-8", response.getHeaders().get("Content-Type"));

            String expectedOutput = Arrays.asList(0, 1, 2, 3, 4)
                    .stream()
                    .map(i -> "id: " + i + "\nevent: counter\ndata: event " + i + "\n")
                    .collect(joining("\n"))
                    + "\n";

            System.out.println(httpClient.get().getBody().getText());
            assertEquals(expectedOutput, response.getBody().getText());
        });
    }
}
