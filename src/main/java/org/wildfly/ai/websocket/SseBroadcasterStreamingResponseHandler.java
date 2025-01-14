/*
 * Copyright 2025 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.ai.websocket;

import java.util.concurrent.atomic.AtomicInteger;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

public class SseBroadcasterStreamingResponseHandler implements StreamingResponseHandler<AiMessage> {

    private final AtomicInteger lastEventId;
    private final SseEventSink sseEventSink;
    private final OutboundSseEvent.Builder eventBuilder;

    public SseBroadcasterStreamingResponseHandler(SseEventSink sseEventSink, Sse sse, int lastEventId) {
        this.lastEventId = new AtomicInteger(lastEventId);
        this.sseEventSink = sseEventSink;
        this.eventBuilder = sse.newEventBuilder();
    }

    @Override
    public void onNext(String token) {
        OutboundSseEvent sseEvent = eventBuilder
                .name("token")
                .id(String.valueOf(lastEventId.getAndIncrement()))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data(token.replace("\n", "<br/>"))
                .reconnectDelay(3000)
                .comment("This is a token from the llm")
                .build();
        sseEventSink.send(sseEvent);
    }

    @Override
    public void onComplete(Response<AiMessage> response) {
        OutboundSseEvent sseEvent = eventBuilder
                .name("token")
                .id(String.valueOf(lastEventId.getAndIncrement()))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data("end-data-token")
                .reconnectDelay(3000)
                .comment("This is a token from the llm")
                .build();
        sseEventSink.send(sseEvent)
                .whenComplete((event, throwable) -> {
                    sseEventSink.close();
                });
    }

    @Override
    public void onError(Throwable error) {
        error.printStackTrace();
    }
}
