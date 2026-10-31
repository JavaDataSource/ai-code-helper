package com.wcf.aicodehelper.ai;

import com.wcf.aicodehelper.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;

// @AiService
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    // 如果有多个用户，希望每个用户之间的消息隔离，可以通过给对话方法增加入参：@MemoryId int memoryId，在调用会话时传入memoryId即可（类似聊天室的房间号）
    String chat(String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);

    /* record:Java 14 引入（Java 16 正式转正）的一个全新语法特性是一种专门用来充当不可变数据载体（Data Carrier）”的轻量级类。
    在这里，它的作用是让 AI 大模型返回的 JSON 结果，能自动、优雅地映射成一个 Java 对象。 */
    record Report(String name, List<String> suggestionList){};

    // 返回封装后的结果
    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    // 流式输出
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String message);

}
