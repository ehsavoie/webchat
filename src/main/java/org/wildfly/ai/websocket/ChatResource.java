/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.wildfly.ai.websocket;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@RequestScoped
@Path("/service")
public class ChatResource {

    @Inject
    private SimpleAIService aiService;
    @Inject
    private Tracer tracer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    public String chatWithAssistant(@QueryParam("question") String question) {
        Span span = tracer.spanBuilder("ChatResource")
                .setAttribute("com.acme.string-key", "value")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            String answer;
            try {
                span.setAttribute("com.acme.question", "question");
                span.addEvent(question);
                System.out.println("Weld Proxy " + aiService + " with context " + span.getSpanContext() + " and tracer " + tracer + " in thread " + Thread.currentThread() + " with scope " + scope);
                answer = aiService.chat(question);
                System.out.println("Answer received " + answer + " with context " + span.getSpanContext() + " and tracer " + tracer + " in thread " + Thread.currentThread() + " with scope " + scope);
                span.setStatus(StatusCode.OK);
            } catch (Exception e) {
                e.printStackTrace();
                span.recordException(e);
                span.setStatus(StatusCode.ERROR);
                answer = "My failure reason is:\n\n" + e.getMessage();
            }
            span.addEvent(answer);
            System.out.println("Ending span " + span + " with context " + span.getSpanContext() + " and tracer " + tracer + " in thread " + Thread.currentThread() + " with scope " + scope);
            return answer;
        } finally {
            System.out.println("Span " + span + " with context " + span.getSpanContext() + " and tracer " + tracer + " in thread " + Thread.currentThread() + " is ended");
            span.end();
        }
    }
}
