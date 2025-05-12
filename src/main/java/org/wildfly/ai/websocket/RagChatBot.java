/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ServerEndpoint(value = "/websocket/chatbot",
        configurator = org.wildfly.ai.websocket.CustomConfigurator.class)
public class RagChatBot {


    @Inject
    @Named(value = "ollama")
    ChatModel chatModel;
    @Inject
    @Named(value = "embedding-store-retriever")
    ContentRetriever retriever;
    
    RetrievalAugmentor augmentor;

    private static final String PROMPT_TEMPLATE = "You are a WildFly expert who understands well how to administrate the WildFly server and its components\n"
            + "Objective: answer the user question delimited by  ---\n"
            + "\n"
            + "---\n"
            + "{{userMessage}}\n"
            + "---"
            + "\n Here is a few data to help you:\n"
            + "{{contents}}";

    @PostConstruct
    public void createBasicRag() {
        augmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE))
//                        .metadataKeysToInclude(asList("file_name", "url", "title", "subtitle"))
                        .build())
                .queryRouter(new DefaultQueryRouter(retriever))
                .build();
    }

    @OnMessage
    public String sayHello(String question, Session session) throws IOException {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().id(session.getUserProperties().get("httpSessionId")).maxMessages(3).build();
        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .retrievalAugmentor(augmentor)
                .build();
        String result = chain.execute(question);
        return result;
    }


    @OnOpen
    public void helloOnOpen(Session session, EndpointConfig config) {
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnClose
    public void helloOnClose(CloseReason reason, Session session) {
        System.out.println("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
    }
}
