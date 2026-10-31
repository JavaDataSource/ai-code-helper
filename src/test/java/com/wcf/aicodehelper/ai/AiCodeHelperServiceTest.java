package com.wcf.aicodehelper.ai;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeHelperServiceTest {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Test
    void chat() {
        String result = aiCodeHelperService.chat("你好，我是程序员wcf");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result = aiCodeHelperService.chat("你好，我是程序员wcf");
        System.out.println(result);
        result = aiCodeHelperService.chat("你好，我是谁来着？");
        System.out.println(result);
    }

    @Test
    void chatForReport() {
        String userMessage = "你好，我是程序员wcf，学编程5年，请帮我制定学习报告";
        AiCodeHelperService.Report report = aiCodeHelperService.chatForReport(userMessage);
        System.out.println(report);

        // 如果发现AI有时无法生成准确的 JSON，那么可以采用 JSON Schema 模式，直接在请求中约束 LLM 的输出格式。这是目前最可靠、精确度最高的结构化输出实现。
       /* ResponseFormat responseFormat = ResponseFormat.builder()
                .type(JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("Person")
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("name")
                                .addIntegerProperty("age")
                                .addNumberProperty("height")
                                .addBooleanProperty("married")
                                .required("name", "age", "height", "married")
                                .build())
                        .build())
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(List.of(UserMessage.from(userMessage)))
                .build();*/
    }

    @Test
    void chatWithRag() {
        /* String result = aiCodeHelperService.chat("怎么学习Java，有哪些常见的面试题？");
        System.out.println(result); */
        Result<String> res = aiCodeHelperService.chatWithRag("怎么学习Java，有哪些常见的面试题？");
        System.out.println(res.sources());
        System.out.println(res.content());
    }

    @Test
    void chatWithTools() {
        String result = aiCodeHelperService.chat("有哪些常见的计算机网络面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelperService.chat("什么是程序员鱼皮的编程导航？");
        System.out.println(result);
    }

    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat("save the game");
        System.out.println(result);
    }

}