package com.wcf.aicodehelper.ai;

import com.wcf.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration：SpringBoot配置类注解。若AiCodeHelperService使用了@AiService，则需要暂时注释掉，不然会创建两个同名的Bean
@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    /**
     * 工厂设计模式思想
      * 调用 AiServices.create 方法就可以创建出 AI Service 的实现类了，背后的原理是利用 Java 反射机制创建了一个实现接口的代理对象，
     * 代理对象负责输入和输出的转换，比如把 String 类型的用户消息参数转为 UserMessage 类型并调用 ChatModel，再将 AI 返回的 AiMessage
     * 类型转换为 String 类型作为返回值。
     */
    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 会话记忆 最多保存10条消息
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造AI Service
        AiCodeHelperService aiCodeHelperService = AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel) // 流式输出
                .chatMemory(chatMemory) // 会话记忆
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever) // RAG检索增强生成
                .tools(new InterviewQuestionTool()) // 工具调用
                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return aiCodeHelperService;

        // return AiServices.create(AiCodeHelperService.class, qwenChatModel);

        /*ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(10)
                .chatMemoryStore(new PersistentChatMemoryStore())  // 一个拥有持久化能力的会话记忆（存在数据库里）
                .build();*/

       /* 极简版RAG：适合快速查看效果，首先需要引入额外的依赖（<artifactId>langchain4j-easy-rag</artifactId>），里面包含了内置的离线 Embedding 模型，开箱即用
        示例代码如下，使用内置的文档加载器读取文档，然后利用内置的 Embedding 模型将文档转换成向量，并存储在内置的 Embedding 内存存储中，最后给 AI Service 绑定默认的内容检索器。
        // 1. 加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
        // 2. 使用内置的 EmbeddingModel 转换文本为向量，然后存储到自动注入的内存 embeddingStore 中
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        // 构造 AI Service
        AiCodeHelperService aiCodeHelperService = AiServices.builder(AiCodeHelperService.class)
                .chatModel(qwenChatModel)
                .chatMemory(chatMemory)
                // RAG：从内存 embeddingStore 中检索匹配的文本片段
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build(); */

    }

}
