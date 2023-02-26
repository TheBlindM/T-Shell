package com.tshell.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    final  String openAiKey="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";


    public String doChat(String question) {

        HttpRequest request = HttpUtil
                .createPost("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer %s".formatted(openAiKey))
                .header("Content-Type","application/json")
                .timeout(1000*60);

        String paramJson = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + question.strip() + "\", \"temperature\": 0, \"max_tokens\": 500}";
        request.body(paramJson);

        try (HttpResponse httpResponse = request.execute()) {
            if (httpResponse.getStatus() == HttpStatus.HTTP_OK) {
                String body = httpResponse.body();
                try {
                    AIAnswer aiAnswer = objectMapper.readValue(body, AIAnswer.class);
                    return   aiAnswer.getChoices().stream().map(Choices::getText)
                            .collect(Collectors.joining(StrUtil.EMPTY));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }else {
                throw new RuntimeException("Ai 异常 code:" + httpResponse.getStatus());
            }
        }
    }


    @Data
    public static class AIAnswer {
        private String id;

        private String object;

        private int created;

        private String model;

        private List<Choices> choices;
    }


    @Data
    public static class Choices {
        private String text;

        private int index;

        private String logprobs;

        private String finish_reason;
    }


}
