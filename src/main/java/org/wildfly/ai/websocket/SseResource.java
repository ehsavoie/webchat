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

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import java.util.List;
import org.jboss.resteasy.annotations.Stream;
import org.reactivestreams.Publisher;

@RequestScoped
@Path("/sse")
public class SseResource {

    @Inject
    @Named(value = "streaming-ollama")
    StreamingChatLanguageModel streamingChatModel;

//
//    @GET
//    @Produces(MediaType.SERVER_SENT_EVENTS)
//    @Path("/chat")
//    public void streamingChatWithAssistant(@Context Sse sse, @Context SseEventSink sseEventSink,
//            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) @DefaultValue("-1") int lastReceivedId,
//            @QueryParam("question") String question) throws InterruptedException {
//        final int lastEventId;
//        if (lastReceivedId != -1) {
//            lastEventId = lastReceivedId + 1;
//        } else {
//            lastEventId = 1;
//        }
//        System.out.println("Receiving a request on SSE endpoint");
//        List<ChatMessage> messages = List.of(SystemMessage.from("""
//                   You are an AI named Bob answering general question.
//                   Your response must be polite, use the same language as the question, and be relevant to the question."""),
//                UserMessage.from(question));
//        SseBroadcasterStreamingResponseHandler handler = new SseBroadcasterStreamingResponseHandler(sseEventSink, sse, lastEventId);
//        streamingChatModel.generate(messages, handler);
//        while (handler.isRunning()) {
//            Thread.sleep(300);
//        }
//    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/chat")
    @Stream
    public Publisher<String> streamingChatWithAssistant(@Context Sse sse,
            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) @DefaultValue("-1") int lastReceivedId,
            @QueryParam("question") String question) throws InterruptedException {
        final int lastEventId;
        if (lastReceivedId != -1) {
            lastEventId = lastReceivedId + 1;
        } else {
            lastEventId = 1;
        }
        System.out.println("Receiving a request on SSE endpoint");
        List<ChatMessage> messages = List.of(SystemMessage.from("""
                   You are an AI named Bob answering general question.
                   Your response must be polite, use the same language as the question, and be relevant to the question."""),
                UserMessage.from(question));
        PublisherStreamingResponseHandler handler = new PublisherStreamingResponseHandler();
        streamingChatModel.generate(messages, handler);
        return handler;
    }
}
