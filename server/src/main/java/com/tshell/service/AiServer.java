package com.tshell.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheBlind
 * @version 1.0
 */
@ApplicationScoped
public class AiServer {

    @Inject
    ObjectMapper objectMapper;

    final String openAiKey = "密钥";


    public String doChat(String question) {

        try {
            List<Param.Message> messages = List.of(
                    Param.Message.builder().role("system").content("T-Shell 是一款智能命令提示终端，你是叫小T。").build(),
                    Param.Message.builder().role("user").content(question.trim()).build()
            );

            Param param = Param.builder().model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            // 创建HttpClient实例
            HttpClient httpClient = HttpClient.newHttpClient();

            // 创建HttpPost请求对象
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer %s".formatted(openAiKey))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(param)))
                    .build();

            // 发送HttpPost请求并获取响应
            java.net.http.HttpResponse<String> response = null;

            response = httpClient.<String>send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.HTTP_OK) {
                String body = response.body();
                AIAnswer aiAnswer = objectMapper.readValue(body, AIAnswer.class);
                return aiAnswer.getChoices().stream().map((choices3_5) -> choices3_5.message.content)
                        .collect(Collectors.joining(StrUtil.EMPTY));
            } else {
                return String.format("Ai 异常 code:%s,body:%s", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @RegisterForReflection
    public static class AIAnswer {
        private String id;

        private String object;

        private int created;

        private String model;

        private List<Choices3_5> choices;
    }


    @Data
    @RegisterForReflection
    public static class Choices {
        private String text;

        private int index;

        private String logprobs;

        private String finish_reason;
    }

    @Data
    @Builder
    @RegisterForReflection
    public static class Param {
        String model;
        List<Message> messages;
        int temperature = 0;
        double top_p = 0.8;

        @Data
        @Builder
        @RegisterForReflection
        public static class Message {
            String role;
            String content;
        }
    }

    @Data
    @RegisterForReflection
    public static class Choices3_5 {
        private int index;
        private Param.Message message;
        private String finish_reason;
    }


}
