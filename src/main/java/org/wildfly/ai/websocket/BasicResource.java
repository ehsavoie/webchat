/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
@RequestScoped
@Path("/basic")
public class BasicResource {

    @Inject
    @Named(value = "ollama")
    ChatModel chatModel;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    public String chatWithAssistant(@QueryParam("question") String question, @Context HttpServletRequest request) {
        request.getSession(true);
        String answer;
        try {
            answer = chatModel.chat(SystemMessage.from("""
                   You are an AI named Bob answering general question.
                   Your response must be polite, use the same language as the question, and be relevant to the question."""),
                    UserMessage.from(question)).aiMessage().text();
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }
        return answer;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/implicit")
    public String testImplicitOpentelemetry(@QueryParam("question") String question, @Context HttpServletRequest request) {
        request.getSession(true);
        String answer;
        try {
            answer = question + " received";
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }
        return answer;
    }
}
