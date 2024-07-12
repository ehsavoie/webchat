/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.wildfly.ai.websocket;

import org.wildfly.ai.annotations.RegisterAIService;

@RegisterAIService(chatLanguageModelName = "mychat")
public interface SimpleAIService {
    String chat(String question);
}
