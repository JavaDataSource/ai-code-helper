package com.wcf.aicodehelper.controller;

import com.wcf.aicodehelper.ai.AiCodeHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    /**
     * SSE 流式接口
     * 我们平时开发的大多数接口都是同步接口，也就是等后端处理完再返回
     * 但是对于 AI 应用，特别是响应时间较长的对话类应用，可能会让用户失去耐心等待
     * 因此推荐使用 SSE（Server-Sent Events）技术实现实时流式输出，类似打字机效果，大幅提升用户体验
     * @param memoryId
     * @param message
     * @return Flux<ServerSentEvent<String>>
     */
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
        return aiCodeHelperService.chatStream(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

}
