/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.smallrye.llm.spi.RegisterAIService;
import jakarta.enterprise.context.SessionScoped;


@RegisterAIService(chatModelName = "ollama", tools = {org.wildfly.ai.websocket.Weather.class,org.wildfly.ai.websocket.Calculator.class}, scope = SessionScoped.class, chatMemoryProviderName = "chat-memory")
public interface SimpleAIService {
    @SystemMessage("""
                   You are an AI named Bob answering general question.
                   Your response must be polite, use the same language as the question, and be relevant to the question.""")
    String chat(@UserMessage String question, @MemoryId int memoryId);
}
