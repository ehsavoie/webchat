/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.websocket;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
@MultipartConfig
@WebServlet("/uploadServlet")
public class ImageAnalyzerServlet extends HttpServlet {

    @Inject
    @Named(value = "ollama")
    ChatModel chatModel;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part file = request.getPart("file");
        String filename = getFilename(file);
        Image image = Image.builder().mimeType(file.getContentType())
                .base64Data(encodeBase64(file.getInputStream())).build();
        UserMessage userMessage = UserMessage.from(
                TextContent.from("What do you see?"),
                ImageContent.from(encodeBase64(file.getInputStream()), file.getContentType()));
        ChatResponse answer = chatModel.chat(userMessage);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(answer.aiMessage().text());
    }

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }

    private static String encodeBase64(InputStream in) throws IOException {
        try (
                ByteArrayOutputStream tempBuffer = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                tempBuffer.write(buffer, 0, length);
            }
            return Base64.getEncoder().encodeToString(tempBuffer.toByteArray());
        }
    }
}
