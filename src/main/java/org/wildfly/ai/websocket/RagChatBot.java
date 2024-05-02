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
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.Collections;
import org.wildfly.ai.websocket.embeddings.EmbeddingStoreFactory;

@ServerEndpoint(value = "/websocket/chatbot",
        configurator = org.wildfly.ai.websocket.CustomConfigurator.class)
public class RagChatBot {
    // To register and get a free API key for Cohere, please visit the following link:
    // https://dashboard.cohere.com/welcome/register

    private static final String COHERE_API_KEY = "JBfpKpgYfp5PoeGmDjOdF5mvCfwdrJILot3VxowT";

//    private static final ContentRetriever contentRetriever
//            = EmbeddingStoreFactory.createEmbeddingStoreContentRetriever(
//                    "/home/ehugonne/dev/AI/crawler/crawler/docs-wildfly-embedding.json");
    private static final ContentRetriever contentRetriever
            = EmbeddingStoreFactory.createWeaviateEmbeddingStoreContentRetriever("localhost", 8090, Collections.emptyList()
                /*List.of("url", "language", "parent_url", "file_name", "file_path")*/);
    private static final ChatLanguageModel model = OpenAiChatModel
            .builder()
            .apiKey("demo")
            .maxRetries(5)
            .modelName(OpenAiChatModelName.GPT_3_5_TURBO)
            .logRequests(Boolean.TRUE)
            .logResponses(Boolean.TRUE)
            .maxTokens(1000)
            .build();

    private static final String PROMPT_TEMPLATE = "You are a wildfly expert who understands well how to administrate the wildfly server and its components\n"
            + "Objective: answer the user question delimited by  ---\n"
            + "\n"
            + "---\n"
            + "{{userMessage}}\n"
            + "---"
            + "\n Here is a few data to help you:\n"
            + "{{contents}}";

    @OnMessage
    public String sayHello(String question, Session session) throws IOException {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().id(session.getUserProperties().get("httpSessionId")).maxMessages(3).build();
        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .retrievalAugmentor(createBasicRag())
                .build();
        return chain.execute(question).replace("\n", "<br/>");
    }

    private RetrievalAugmentor createRerankingRag() {
        // To register and get a free API key for Cohere, please visit the following link:
        // https://dashboard.cohere.com/welcome/register
        ScoringModel scoringModel = CohereScoringModel.withApiKey(COHERE_API_KEY);

        ContentAggregator contentAggregator = ReRankingContentAggregator.builder()
                .scoringModel(scoringModel)
                .minScore(0.8) // we want to present the LLM with only the truly relevant segments for the user's query
                .build();

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentAggregator(contentAggregator)
                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE))
                        .metadataKeysToInclude(asList("file_name", "url", "title"))
                        .build())
                .queryRouter(new DefaultQueryRouter(contentRetriever))
                .build();
    }

    private RetrievalAugmentor createBasicRag() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from(PROMPT_TEMPLATE))
                        .metadataKeysToInclude(asList("file_name", "url", "title"))
                        .build())
                .queryRouter(new DefaultQueryRouter(contentRetriever))
                .build();
    }

    @OnOpen
    public void helloOnOpen(Session session, EndpointConfig config) {
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnClose
    public void helloOnClose(CloseReason reason) {
        System.out.println("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
    }
}
