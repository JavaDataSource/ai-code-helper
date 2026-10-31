# AI 编程小助手（ai-code-helper）

> 基于 **Spring Boot 4 + LangChain4j + Vue 3** 构建的智能编程问答应用。支持 **SSE 流式输出**、**多轮会话记忆**、**RAG 知识库检索**、**工具调用（Tool Calling）**与 **MCP 扩展**，助力编程学习与求职面试。

---

## 📖 项目简介

AI 编程小助手是一个全栈 AI 对话应用，前端提供类似聊天室的交互界面，后端接入阿里云 **通义千问（Qwen）** 大模型。它专注于 4 个方向：

1. **规划清晰的编程学习路线**
2. **提供项目学习建议**
3. **给出程序员求职全流程指南**（简历优化、投递技巧等）
4. **分享高频面试题和面试技巧**

除了基础的智能问答，项目还完整实践了当前主流的 AI 应用开发技术：

| 能力 | 说明 |
| --- | --- |
| ⚡ SSE 流式输出 | 后端以 `Flux<ServerSentEvent>` 推流，前端打字机式实时渲染，告别漫长等待 |
| 🧠 多轮会话记忆 | 通过 `@MemoryId` 隔离不同用户会话，每会话独立保存最近 10 条消息 |
| 📚 RAG 检索增强 | 加载本地知识库文档，按段落切分后向量化入库，问答时检索最相关片段注入上下文 |
| 🔧 Tool Calling | 内置「面试鸭题目搜索」工具，AI 可自主联网检索相关面试题 |
| 🔌 MCP 协议 | 通过 MCP 客户端接入智谱 `web_search` 工具，扩展 AI 联网搜索能力 |
| 🛡️ 输入护栏 Guardrail | 对用户输入进行敏感词校验，拦截不安全请求 |
| 📊 结构化输出 | 支持将大模型返回的 JSON 自动映射为 Java 对象（`record Report`） |
| 🖥️ Markdown 渲染 + 代码高亮 | 前端用 marked 渲染标题/列表/表格/代码块，highlight.js 高亮，DOMPurify 消毒防 XSS |
| 📡 模型监听器 | `ChatModelListener` 记录每次请求 / 响应 / 异常日志，便于调试 |

## 🏗️ 技术栈

### 后端
- **Java 21** + **Spring Boot 4.1.2**（Web / WebMVC / Lombok）
- **LangChain4j 1.1.0**：AI Service、会话记忆、RAG、Guardrail、Reactor 流式支持
- **langchain4j-community-dashscope**：通义千问 `qwen-max` 对话模型 + `text-embedding-v4` 向量模型
- **langchain4j-mcp**：MCP 客户端与工具提供器
- **Jsoup**：网页抓取（面试鸭搜索页解析）

### 前端
- **Vue 3.5**（Composition API + `<script setup>`）
- **Axios**：基于 `onDownloadProgress` 手动解析 SSE 协议实现流式接收
- **marked + marked-highlight + highlight.js**：AI 回复的 Markdown 渲染与代码高亮
- **DOMPurify**：对 AI 输出的 HTML 消毒，防止 XSS
- **Vite 5**：开发服务器 + 构建工具

## 📁 项目结构

```
ai-code-helper
├── src/main/java/com/wcf/aicodehelper
│   ├── AiCodeHelperApplication.java        # Spring Boot 启动类
│   ├── controller
│   │   └── AiController.java               # /ai/chat SSE 流式对话接口
│   ├── config
│   │   └── CorsConfig.java                 # 全局跨域配置
│   └── ai
│       ├── AiCodeHelper.java               # 基于 ChatModel 的简单对话示例
│       ├── AiCodeHelperService.java        # AI Service 接口（核心对话能力）
│       ├── AiCodeHelperServiceFactory.java # 工厂模式组装 AI Service（记忆/RAG/工具/MCP）
│       ├── PersistentChatMemoryStore.java  # 会话记忆持久化扩展点（TODO 实现入库）
│       ├── config
│       │   └── McpConfig.java              # MCP 客户端配置（智谱 web_search SSE）
│       ├── model
│       │   └── QwenChatModelConfig.java    # 通义千问 ChatModel 配置
│       ├── listener
│       │   └── ChatModelListenerConfig.java# 模型请求/响应/异常日志监听
│       ├── guardrail
│       │   └── SafeInputGuardrail.java     # 输入敏感词护栏
│       ├── tools
│       │   └── InterviewQuestionTool.java  # 面试鸭面试题搜索工具（Jsoup 抓取）
│       └── rag
│           └── RagConfig.java              # RAG 文档加载/切分/向量化/检索配置
├── src/main/resources
│   ├── system-prompt.txt                   # 系统提示词
│   ├── application.yml / application-local.yml  # 配置文件（local 存放真实密钥）
│   └── docs                                # RAG 知识库文档
│       ├── Java 编程学习路线.md
│       ├── 鱼皮的项目学习建议.md
│       ├── 鱼皮的求职指南.md
│       └── 程序员常见面试题.md
├── ai-code-helper-frontend                 # 前端工程
│   ├── index.html
│   ├── vite.config.js                      # 端口 5173 + /api 代理到 8081
│   └── src
│       ├── main.js / App.vue               # 聊天界面（多轮对话/流式渲染/重开会话）
│       ├── api/chat.js                     # SSE 流式请求封装
│       ├── utils/markdown.js               # Markdown 渲染 + 代码高亮 + XSS 消毒
│       └── style.css
├── pom.xml
└── mvnw / mvnw.cmd                         # Maven 包装器
```

## 🚀 快速开始

### 环境要求
- **JDK 21+**
- **Maven 3.9+**（或直接使用项目自带的 `mvnw`）
- **Node.js 18+**
- **阿里云 DashScope API Key**（通义千问，用于对话与向量模型）—— [申请地址](https://dashscope.console.aliyun.com/)
- **智谱 API Key**（可选，用于 MCP 联网搜索工具）—— [申请地址](https://open.bigmodel.cn/)

### 1. 配置密钥

在 `src/main/resources/application-local.yml` 中填入你的 API Key（该文件已在 `.gitignore` 中忽略，**切勿提交真实密钥**）：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: <你的 DashScope API Key>
      embedding-model:
        model-name: text-embedding-v4
        api-key: <你的 DashScope API Key>
      streaming-chat-model:
        model-name: qwen-max
        api-key: <你的 DashScope API Key>
bigmodel:
  api-key: <你的智谱 API Key>
```

### 2. 启动后端（端口 8081）

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

启动成功后，后端接口基地址为 `http://localhost:8081/api`。

> 💡 首次启动时，`RagConfig` 会自动加载 `src/main/resources/docs` 下的知识库文档，完成切分、向量化并写入向量存储。

### 3. 启动前端（端口 5173）

```bash
cd ai-code-helper-frontend
npm install
npm run dev
```

### 4. 开始对话

浏览器访问 [http://localhost:5173](http://localhost:5173)，即可与 AI 编程小助手对话：

- 输入问题后按 **Enter** 或点击「发送」，AI 回复以打字机效果流式呈现，并自动渲染为 **Markdown**（支持标题、列表、表格、代码块及 **highlight.js 语法高亮**）
- 页面自动生成会话 ID，支持**多轮连续对话**；点击「重开会话」可开启新会话
- 首次进入时提供示例问题快捷按钮，一键填入试试看

## 🔌 API 接口

### SSE 流式对话

```
GET /api/ai/chat?memoryId={会话ID}&message={消息内容}
```

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `memoryId` | int | 会话（聊天室）ID，用于隔离不同用户的多轮上下文 |
| `message` | String | 用户发送的消息 |

- **响应类型**：`text/event-stream`（SSE），每个分片格式为 `data:<chunk>\n\n`
- **后端实现**：`AiController` 返回 `Flux<ServerSentEvent<String>>`，由 `AiCodeHelperService#chatStream` 流式生成

**示例（curl）：**

```bash
curl -N "http://localhost:8081/api/ai/chat?memoryId=1&message=Java%20HashMap%20的底层原理"
```

```
data:HashMap
data: 的
data: 底层...
```

## ⚙️ 核心实现细节

### AI Service 组装（`AiCodeHelperServiceFactory`）

采用**工厂模式**通过 `AiServices.builder()` 手动组装 AI Service 代理对象，将各项能力挂载到同一个接口上：

```java
AiServices.builder(AiCodeHelperService.class)
        .chatModel(myQwenChatModel)                    // 同步对话模型
        .streamingChatModel(qwenStreamingChatModel)    // 流式对话模型
        .chatMemory(chatMemory)                        // 会话记忆（保留最近 10 条）
        .chatMemoryProvider(memoryId -> ...)           // 每个会话独立记忆
        .contentRetriever(contentRetriever)            // RAG 检索器
        .tools(new InterviewQuestionTool())            // 本地工具调用
        .toolProvider(mcpToolProvider)                 // MCP 远程工具
        .build();
```

### RAG 流程（`RagConfig`）

```
加载 docs/ 目录文档 → 按段落切分（最大 1000 字符，重叠 200）
→ 为每个片段补充文件名元信息 → text-embedding-v4 向量化 → 入库
→ 检索时返回最相关的 5 条、过滤相似度低于 0.75 的结果
```

### 前端 SSE 解析（`chat.js`）

由于对话请求是 GET 且连接保持不断开，不能直接使用原生 `EventSource`。前端基于 Axios 的 `onDownloadProgress` 回调实时读取 `xhr.responseText`，通过增量对比截取新到达的分片，再按 SSE 协议（以 `\n\n` 分隔事件、提取 `data:` 行）解析，实现真正的实时渲染；同时支持 `AbortController` 主动中断请求（如重开会话时）。

### AI 回复的 Markdown 渲染（`utils/markdown.js`）

AI 消息不再以纯文本展示，而是完整渲染为 Markdown：

- **marked + marked-highlight**：解析标题 / 列表 / 表格 / 引用等语法，并在渲染阶段即完成代码块高亮
- **highlight.js**（按需引入常用语言子集以减小包体积）：标注了语言（如 ` ```java `）时精确高亮，未标注语言时自动检测，识别失败则按纯文本转义
- **DOMPurify**：AI 输出属于外部内容，渲染为 HTML 前统一消毒后再通过 `v-html` 挂载，防止 XSS 注入
- 用户消息仍以纯文本插值展示，天然免疫 XSS

## ⚠️ 注意事项

- `application-local.yml` 中的 API Key 属于敏感信息，请勿提交到公开仓库；如已泄露请及时前往对应平台重置。
- 会话记忆默认保存在**内存**中，后端重启后会丢失；`PersistentChatMemoryStore` 已预留持久化扩展点，可自行实现接入 MySQL 等存储。
- 「面试鸭搜索工具」依赖目标网站的真实页面结构（`.ant-table-cell > a` 选择器），页面改版后可能需要相应调整。
- MCP 联网搜索需配置有效的智谱 API Key，否则相关工具调用会失败（不影响基础对话）。

## 🗺️ 可扩展方向

- [ ] 会话记忆持久化到数据库
- [ ] 使用嵌入式 / 独立向量数据库替代内存存储
- [ ] 支持多条知识库文档热更新
- [ ] 增加用户体系与历史会话管理
- [ ] 代码块增加一键复制按钮
- [ ] Docker 一键部署
