import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;
import ratpack.http.Response;
import ratpack.http.client.ReceivedResponse;
import ratpack.http.internal.HttpHeaderConstants;
import ratpack.sse.ServerSentEvents;
import ratpack.sse.internal.ServerSentEventEncoder;
import ratpack.stream.Streams;
import ratpack.test.embed.EmbeddedApp;
import ratpack.websocket.WebSockets;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ratpack.http.ResponseChunks.stringChunks;
import static ratpack.sse.ServerSentEvents.serverSentEvents;
import static ratpack.stream.Streams.periodically;

public class MainTest {

    /*@Test
    public void render(Context context) throws Exception {
        ByteBufAllocator bufferAllocator = context.get(ByteBufAllocator.class);

        Response response = context.getResponse();
        response.getHeaders().add(HttpHeaderConstants.CONTENT_TYPE, HttpHeaderConstants.TEXT_EVENT_STREAM_CHARSET_UTF_8);
        response.getHeaders().add(HttpHeaderConstants.TRANSFER_ENCODING, HttpHeaderConstants.CHUNKED);
        response.getHeaders().add(HttpHeaderConstants.CACHE_CONTROL, HttpHeaderConstants.NO_CACHE_FULL);
        response.getHeaders().add(HttpHeaderConstants.PRAGMA, HttpHeaderConstants.NO_CACHE);

        response.sendStream(Streams.map(publisher, i -> ServerSentEventEncoder.INSTANCE.encode(i, bufferAllocator)));
    }*/

    @Test
    public void webSocketsTest() throws Exception {
        EmbeddedApp.fromHandler( context -> {
            Publisher<String> stream = periodically(context, Duration.ofSeconds(1), i ->
                    i < 5 ? i.toString() : null
            );
            WebSockets.websocketBroadcast(context, stream);
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
            /*System.out.println(response.getHeaders().get("Content-Type"));
            System.out.println("ESPERADO: " + expectedOutput);
            System.out.println("RESPONSE: " + response.getBody().getText());*/

            assertEquals(expectedOutput, response.getBody().getText());
        });
    }

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
}
