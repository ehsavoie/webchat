/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
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

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ServerEndpoint(value = "/websocket/chatbot",
        configurator = org.wildfly.ai.websocket.CustomConfigurator.class)
public class RagChatBot {


    @Inject
    @Named(value = "mychat")
    ChatLanguageModel chatModel;
    @Inject
    @Named(value = "myembedding")
    EmbeddingModel embeddingModel;
    @Inject
    @Named(value = "mystore")
    EmbeddingStore embeddingStore;
    @Inject
    @Named(value = "myretriever")
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
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
//                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
//                .build();
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
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .retrievalAugmentor(augmentor)
                .build();
        String result = chain.execute(question).replace("\n", "<br/>");
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
