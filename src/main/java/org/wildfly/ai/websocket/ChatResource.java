/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.wildfly.ai.websocket;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
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
        Span span = tracer.spanBuilder("service-chat")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("com.acme.string-key", "value")
                .startSpan();
        Scope scope = span.makeCurrent();
        try {
            String answer;
            try {
                span.addEvent(question);
                System.out.println("Weld Proxy " + aiService);
                answer = aiService.chat(question);
            } catch (Exception e) {
                e.printStackTrace();
                span.recordException(e);
                answer = "My failure reason is:\n\n" + e.getMessage();
            }
            span.addEvent(answer);
            return answer;
        } finally {
            scope.close();
            span.end();
        }
    }
}
