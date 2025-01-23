/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/service")
public class ChatResource {

    @Inject
    private SimpleAIService aiService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    public String chatWithAssistant(@QueryParam("question") String question) {
        String answer;
        try {
            answer = aiService.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }
        return answer;
    }
}
