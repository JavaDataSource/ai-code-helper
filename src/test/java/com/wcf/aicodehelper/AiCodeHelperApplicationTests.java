package com.wcf.aicodehelper;

import com.wcf.aicodehelper.ai.AiCodeHelper;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeHelperApplicationTests {

    // 打上@SpringBootTest就能通过@Resource：自动注入Bean
    @Resource
    private AiCodeHelper aiCodeHelper;

    // 单元测试
    @Test
    void chat() {
        aiCodeHelper.chat("你好，我是Java程序员wcf");
    }

    // 多模态-Multimodality：多模态是指能够同时处理、理解和生成多种不同类型数据的能力，比如文本、图像、音频、视频、PDF 等等。
    @Test
    void chatWithMessage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("描述图片"),
                ImageContent.from("https://www.codefather.cn/logo.png")
        );
        aiCodeHelper.chatWithMessage(String.valueOf(userMessage));

    }

}
