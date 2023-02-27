package com.tshell.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
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

        String paramJson = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + question.strip() + "\", \"temperature\": 0, \"max_tokens\": 1000}";


        // 创建HttpClient实例
        HttpClient httpClient = HttpClient.newHttpClient();

        // 创建HttpPost请求对象
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(openAiKey))
                .POST(HttpRequest.BodyPublishers.ofString(paramJson))
                .build();

        // 发送HttpPost请求并获取响应
        java.net.http.HttpResponse<String> response = null;
        try {
            response = httpClient.<String>send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.HTTP_OK) {
                String body = response.body();
                AIAnswer aiAnswer = objectMapper.readValue(body, AIAnswer.class);
                return aiAnswer.getChoices().stream().map(Choices::getText)
                        .collect(Collectors.joining(StrUtil.EMPTY));
            } else {
                throw new RuntimeException("Ai 异常 code:" + response.statusCode());
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

        private List<Choices> choices;
    }


    @Data
    @RegisterForReflection
    public static class Choices {
        private String text;

        private int index;

        private String logprobs;

        private String finish_reason;
    }


}
