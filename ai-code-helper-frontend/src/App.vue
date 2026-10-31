<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { chatStream } from './api/chat'
import { renderMarkdown } from './utils/markdown'

// 消息角色
const ROLE_USER = 'user'
const ROLE_AI = 'ai'

// 页面加载后自动创建一个会话 id，用于多轮对话的基础记忆（memoryId）
const memoryId = ref(0)

const messages = reactive([])

const inputText = ref('')
const streaming = ref(false) // 是否正在接收 AI 流式回复
const errorMessage = ref('') // 网络错误提示

const messageListRef = ref(null)
const inputRef = ref(null)
// 记录当前正在进行的流式请求的 abort 函数
let abortCurrent = null

// 把文本写入剪贴板：优先用 clipboard API；若被权限/焦点策略拒绝，
// 或当前非安全上下文（非 HTTPS/localhost），降级为 execCommand 兜底
const copyText = async (text) => {
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      return
    } catch {
      // NotAllowedError 等情况：继续走下方兜底方案
    }
  }
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  const ok = document.execCommand('copy')
  document.body.removeChild(textarea)
  if (!ok) throw new Error('copy failed')
}

// 代码块复制按钮：v-html 渲染的内容无法直接绑定事件，
// 因此在消息列表上做事件委托，统一拦截 .code-copy-btn 的点击
const handleChatListClick = async (e) => {
  const btn = e.target.closest('.code-copy-btn')
  if (!btn) return
  const code = btn.closest('.code-block')?.querySelector('pre code')
  if (!code) return

  try {
    await copyText(code.innerText)
    btn.textContent = '已复制'
    btn.classList.add('copied')
  } catch {
    btn.textContent = '复制失败'
  }
  // 每个按钮独立的还原定时器，避免多条代码块互相干扰
  clearTimeout(btn._copyTimer)
  btn._copyTimer = setTimeout(() => {
    btn.textContent = '复制'
    btn.classList.remove('copied')
  }, 1500)
}

const initMemoryId = () => {
  // 生成一个会话 id：时间戳*2 保证数字较大且与前一次不同，
  // 也可以根据业务替换成随机数 / 后端生成的 id 等
  memoryId.value = Math.floor(Math.random() * 1000000)
}

// 格式化时间 HH:mm
const formatTime = () => {
  const now = new Date()
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const el = messageListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const appendMessage = (role, content) => {
  messages.push({ role, content, time: formatTime() })
  scrollToBottom()
}

const send = () => {
  const message = inputText.value.trim()
  if (!message || streaming.value) return

  errorMessage.value = ''
  appendMessage(ROLE_USER, message)
  inputText.value = ''

  // 追加一条空的 AI 消息，流式分片将持续追加到 content 上
  const aiMessage = reactive({ role: ROLE_AI, content: '', time: formatTime() })
  messages.push(aiMessage)
  scrollToBottom()

  streaming.value = true

  abortCurrent = chatStream(memoryId.value, message, {
    onChunk: (chunk) => {
      aiMessage.content += chunk
      scrollToBottom()
    },
    onDone: () => {
      // 服务端 Flux 正常结束，本次回复完成
      streaming.value = false
      abortCurrent = null
      inputRef.value?.focus()
    },
    onError: (err) => {
      aiMessage.content +=
        aiMessage.content ? '\n\n[连接中断]' : '\n[抱歉，连接服务失败，请确认后端已启动！]'
      errorMessage.value = `请求失败：${err.message}`
      streaming.value = false
      abortCurrent = null
      scrollToBottom()
    },
  })
}

const reset = () => {
  if (abortCurrent) abortCurrent() // 中断未完成的流式响应
  abortCurrent = null
  streaming.value = false
  messages.length = 0
  initMemoryId()
  errorMessage.value = ''
  appendMessage(ROLE_AI, '您好，我是 AI 编程小助手 🤖\n可以问我编程学习、框架原理、求职面试等相关问题，支持多轮连续对话～')
}

onMounted(() => {
  initMemoryId()
  appendMessage(ROLE_AI, '您好，我是 AI 编程小助手 🤖\n可以问我编程学习、框架原理、求职面试等相关问题，支持多轮连续对话～')
  inputRef.value?.focus()
})

onBeforeUnmount(() => {
  if (abortCurrent) abortCurrent()
})

const suggestions = [
  'Java HashMap 的底层原理是什么？',
  '介绍几种常见的 JVM 垃圾回收器',
  'MySQL 索引为什么会失效？',
  '小程序 + 高并发系统如何设计？',
]
</script>

<template>
  <div class="chat-app">
    <header class="chat-header">
      <div class="header-left">
        <span class="logo">🤖</span>
        <div class="header-title">
          <h1>AI 编程小助手</h1>
          <span class="sub-title">编程学习 · 求职面试 · 一问一答</span>
        </div>
      </div>
      <div class="header-right">
        <span class="session-id">会话 ID：{{ memoryId }}</span>
        <button class="btn-reset" :disabled="streaming" @click="reset">重开会话</button>
      </div>
    </header>

    <main ref="messageListRef" class="chat-list" @click="handleChatListClick">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="chat-row"
        :class="msg.role === 'user' ? 'chat-row--user' : 'chat-row--ai'"
      >
        <div v-if="msg.role === 'ai'" class="avatar">🤖</div>
        <div class="bubble-wrap">
          <div class="bubble">
            <!-- AI 消息：渲染为 Markdown（含代码高亮），内容已经 DOMPurify 消毒 -->
            <template v-if="msg.role === 'ai'">
              <div class="md-body" v-html="renderMarkdown(msg.content)"></div>
              <span
                v-if="streaming && index === messages.length - 1 && msg.content"
                class="cursor"
              ></span>
            </template>
            <!-- 用户消息：纯文本展示，避免 XSS -->
            <span v-else class="bubble-text">{{ msg.content }}</span>
          </div>
          <div class="meta">{{ msg.role === 'user' ? '我' : 'AI 助手' }} · {{ msg.time }}</div>
        </div>
        <div v-if="msg.role === 'user'" class="avatar avatar--user">🙋</div>
      </div>

      <div v-if="streaming && !messages[messages.length - 1]?.content" class="typing-hint">
        AI 助手正在思考中<span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
      </div>
    </main>

    <div v-if="errorMessage" class="error-bar">{{ errorMessage }}</div>

    <footer class="chat-input">
      <div v-if="messages.filter((m) => m.role === 'user').length === 0" class="suggestions">
        <span class="suggestion-label">试试这样问：</span>
        <button v-for="item in suggestions" :key="item" class="suggestion" @click="inputText = item">
          {{ item }}
        </button>
      </div>
      <div class="input-row">
        <input
          ref="inputRef"
          v-model="inputText"
          type="text"
          placeholder="请输入你的编程 / 面试问题，Enter 发送"
          :disabled="streaming"
          maxlength="2000"
          @keydown.enter.exact.prevent="send"
        />
        <button class="btn-send" :disabled="streaming || !inputText.trim()" @click="send">
          {{ streaming ? '回复中…' : '发送' }}
        </button>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.suggestions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
</style>
