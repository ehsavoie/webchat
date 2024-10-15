/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.wildfly.ai.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
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
            System.out.println("Weld Proxy " + aiService);
            answer = aiService.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }
        return answer;
    }
}
