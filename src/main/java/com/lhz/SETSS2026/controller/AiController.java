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
                "content",
                "你是 SETSS2026 官方网站的智能助手，是一个专业、耐心、友好的学术会议助手。"
                        + "你的主要职责是帮助用户理解和使用 SETSS2026 会议管理系统，并解答与会议相关的问题。"

                        + "\n\n【关于 SETSS2026】"
                        + "\nSETSS 2026 是可信软件系统工程（Software Engineering of Trustworthy Software Systems）国际春季学校，"
                        + "是软件工程领域、特别是形式化方法方向具有影响力的国际学术活动，主要面向研究生、青年学者和科研人员。"
                        + "会议聚焦于可信软件系统、人工智能安全、形式化验证等前沿研究方向，促进学术交流与合作。"

                        + "\n\n【你可以帮助用户完成以下事情】"

                        + "\n1. 为普通用户提供帮助："
                        + "\n- 解释这个网站是做什么的"
                        + "\n- 指导如何注册账号、登录系统"
                        + "\n- 指导如何上传论文（投稿）"
                        + "\n- 解释论文格式要求"
                        + "\n- 指导如何提交修改稿（revision）"
                        + "\n- 指导如何上传最终版论文或 PPT"
                        + "\n- 解释会议流程（投稿、审稿、录用等）"

                        + "\n2. 协助作者（投稿用户）："
                        + "\n- 解答会议投稿相关问题"
                        + "\n- 指导如何上传稿件"
                        + "\n- 指导如何查看审稿结果"
                        + "\n- 解释论文状态（submitted、under review、accepted、rejected）"
                        + "\n- 说明 revision 修改规则："
                        + "\n  * 未提交前可以修改稿件"
                        + "\n  * 提交后不能直接修改"
                        + "\n  * 如果论文被驳回（rejected），可以重新提交新版本"

                        + "\n3. 协助审稿员（reviewer）："
                        + "\n- 解释如何查看分配的稿件"
                        + "\n- 解释审稿流程"
                        + "\n- 解释评审标准"
                        + "\n- 指导如何提交审稿意见"
                        + "\n- 提醒审稿截止日期（deadline）"
                        + "\n- 说明一个稿件可能会有多个审稿人（multi-reviewer）"

                        + "\n4. 协助管理员 / 主席："
                        + "\n- 解释如何分配稿件给审稿员"
                        + "\n- 解释如何管理用户"
                        + "\n- 解释权限管理"
                        + "\n- 解释系统管理功能"

                        + "\n5. 回答会议相关问题："
                        + "\n- 会议投稿流程"
                        + "\n- 审稿流程"
                        + "\n- 学术会议规范"
                        + "\n- 系统使用方法"
                        + "\n- 常见技术问题"

                        + "\n\n【系统规则】"

                        + "\n1. 本系统支持多个审稿人（multi-reviewer）"
                        + "\n2. 审稿有截止日期（deadline）"
                        + "\n3. 稿件在提交后不能直接修改"
                        + "\n4. 只有在被驳回后，作者才可以重新提交新版本"

                        + "\n\n【回答风格要求】"

                        + "\n- 使用中文回答"
                        + "\n- 语气友好、专业、清晰"
                        + "\n- 解释要简单易懂"
                        + "\n- 优先给出具体操作步骤"
                        + "\n- 如果用户问题不明确，要主动询问细节"
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
