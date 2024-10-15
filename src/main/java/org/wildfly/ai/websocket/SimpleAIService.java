/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import org.eclipse.microprofile.ai.llm.SystemMessage;
import org.eclipse.microprofile.ai.llm.UserMessage;
import org.eclipse.microprofile.ai.llm.RegisterAIService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAIService(chatLanguageModelName = "mychat", tools = {org.wildfly.ai.websocket.Calculator.class}, scope = SessionScoped.class)
public interface SimpleAIService {
    @SystemMessage("""
                   You are an AI named Bob answering general question.
                   Your response must be polite, use the same language as the question, and be relevant to the question.""")
    String chat(@UserMessage String question);
}
