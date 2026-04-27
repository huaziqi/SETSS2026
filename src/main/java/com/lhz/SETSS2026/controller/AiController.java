package com.LHZ.SETSS2026.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${spring.zhipu.api.key}")
    private String apiKey;

    @Value("${spring.zhipu.api.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(
            value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chat(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(60000L);
        System.out.println("========== AI聊天请求开始 ==========");
        System.out.println("请求参数: " + request);

        Object messageObj = request.get("query");
        if (messageObj == null) {
            messageObj = request.get("message");
        }

        String userMessage = null;

        if (messageObj instanceof String) {
            userMessage = (String) messageObj;
        } else if (messageObj instanceof java.util.List<?> list && !list.isEmpty()) {
            userMessage = list.get(0).toString();
        }

        System.out.println("用户消息: " + userMessage);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("error")
                                .data("消息不能为空")
                );
                emitter.complete();
            } catch (Exception ignored) {
            }
            return emitter;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4-flash");
        requestBody.put("stream", true);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是 智谱，一个聪明的AI助手。请用中文友好地回答用户的问题。"
        ));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        messages.add(Map.of(
                "role", "user",
                "content", userMessage
        ));

        requestBody.put("messages", messages);

        System.out.println("API URL: " + baseUrl + "/chat/completions");
        CompletableFuture.runAsync(() -> {
            HttpURLConnection connection = null;
            try {
                String jsonBody = objectMapper.writeValueAsString(requestBody);
                System.out.println("请求体: " + jsonBody);

                URL url = new URL(baseUrl + "/chat/completions");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setDoOutput(true);
                connection.setReadTimeout(60000);
                connection.setConnectTimeout(10000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                System.out.println("API 响应码: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    try (BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        String errorMsg = "API失败: " + responseCode + " - " + errorResponse;
                        System.err.println(errorMsg);
                        emitter.send(
                                SseEmitter.event()
                                        .name("error")
                                        .data(errorMsg)
                        );
                        emitter.complete();
                    }
                    return;
                }

                System.out.println("开始接收流式响应...");
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {

                        if (!line.startsWith("data: "))
                            continue;

                        String json = line.substring(6).trim();

                        if (json.isEmpty())
                            continue;

                        if (json.contains("[DONE]")) {
                            System.out.println("收到结束标记 [DONE]");
                            sendDone(emitter);
                            return;
                        }

                        try {
                            JsonNode node = objectMapper.readTree(json);
                            JsonNode choices = node.path("choices");

                            if (choices.isArray() && !choices.isEmpty()) {
                                JsonNode delta = choices.get(0).path("delta");
                                String content = delta.path("content").asText();

                                if (content != null && !content.isEmpty()) {
                                    System.out.println("AI回复片段: [" + content + "]");

                                    Map<String, Object> chunk = new HashMap<>();
                                    chunk.put("code", 0);
                                    chunk.put("result", content);
                                    chunk.put("is_end", false);

                                    String jsonData = objectMapper.writeValueAsString(chunk);

                                    emitter.send(
                                            SseEmitter.event()
                                                    .data(jsonData)
                                    );
                                }
                            }
                        } catch (Exception parseEx) {
                            System.err.println("解析JSON失败: " + parseEx.getMessage());
                        }
                    }
                }

                System.out.println("流式响应结束");
                sendDone(emitter);

            } catch (Exception e) {
                System.err.println("请求异常: " + e.getMessage());
                e.printStackTrace();
                try {
                    emitter.send(
                            SseEmitter.event()
                                    .name("error")
                                    .data("系统错误: " + e.getMessage())
                    );
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });

        System.out.println("返回 SseEmitter，等待异步处理...");
        return emitter;
    }

    private void sendDone(SseEmitter emitter) {
        try {
            Map<String, Object> end = new HashMap<>();
            end.put("code", 0);
            end.put("result", "");
            end.put("is_end", true);

            String jsonData = objectMapper.writeValueAsString(end);
            System.out.println("发送结束标记: " + jsonData);

            emitter.send(
                    SseEmitter.event()
                            .data(jsonData)
            );

            emitter.complete();
            System.out.println("========== AI聊天请求完成 ==========");

        } catch (Exception e) {
            System.err.println("发送完成标记失败: " + e.getMessage());
        }
    }
}
