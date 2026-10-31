package com.wcf.aicodehelper.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话记忆默认是存储在内存里的，重启后会丢失，可以通过自定义ChatMemoryStore接口的实现类，将消息保存到MySQL等其他数据源中
 */
public class PersistentChatMemoryStore implements ChatMemoryStore {
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: Implement getting all messages from the persistent store by memory ID.
        // ChatMessageDeserializer.messageFromJson(String) and
        // ChatMessageDeserializer.messagesFromJson(String) helper methods can be used to
        // easily deserialize chat messages from JSON.
        return new ArrayList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: Implement updating all messages in the persistent store by memory ID.
        // ChatMessageSerializer.messageToJson(ChatMessage) and
        // ChatMessageSerializer.messagesToJson(List<ChatMessage>) helper methods can be used to
        // easily serialize chat messages into JSON.
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: Implement deleting all messages in the persistent store by memory ID.
    }
}
