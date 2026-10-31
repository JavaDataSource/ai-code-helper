import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 0, // SSE 长连接，不设超时
})

/**
 * 通过 SSE 方式流式调用 /ai/chat 接口。
 *
 * 后端接口（GET，返回 Flux<ServerSentEvent<String>>）：
 *   GET /api/ai/chat?memoryId=xxx&message=xxx
 * 响应为 text/event-stream，每个分片格式为：data:<chunk>\n\n
 *
 * 由于请求是 GET 且 Connection 保持 keep-alive 不断开，
 * 无法直接使用原生 EventSource（它只适合服务端持续推送的场景），
 * 这里使用 Axios 的 onDownloadProgress 回调：
 * XHR 在分片到达时会实时触发 onprogress，agent.target.responseText
 * 中保存了服务端已推送的全部文本，通过对比上次长度即可提取新增分片，
 * 再按 SSE 协议解析出 data 内容。
 *
 * @param {number}   memoryId  聊天室（会话）id，用于区分上下文记忆
 * @param {string}   message   用户发送的消息
 * @param {Object}   handlers  回调集合
 * @param {(text: string) => void} handlers.onChunk  每收到一个 AI 回复分片时触发
 * @param {() => void}         [handlers.onDone]  服务端 Flux 正常结束时触发
 * @param {(err: Error) => void} [handlers.onError] 请求出错时触发
 * @returns {() => void} abort 函数，可随时中断本次流式请求
 */
export function chatStream(memoryId, message, { onChunk, onDone, onError } = {}) {
  // AbortController 用于取消请求（如用户关闭页面或重新开始会话）
  const abortController = new AbortController()

  let receivedLength = 0 // 已读取到并解析掉的字节数
  let buffer = ''        // 尚未凑齐一个完整 SSE 事件的残留内容

  const parseSseBuffer = (isFinal) => {
    // SSE 事件之间以空行（\n\n）分隔
    let separatorIndex
    while ((separatorIndex = buffer.indexOf('\n\n')) !== -1) {
      const rawEvent = buffer.slice(0, separatorIndex)
      buffer = buffer.slice(separatorIndex + 2)
      dispatchEvent(rawEvent)
    }
    // 检查最后一部分是否已完整（流结束时缓冲区会用完）
    if (isFinal && buffer.trim()) {
      dispatchEvent(buffer)
      buffer = ''
    }
  }

  const dispatchEvent = (rawEvent) => {
    // 按 SSE 规范，一个事件可能包含多个 data 行，换行属于内容本身
    const data = rawEvent
      .split('\n')
      .filter((line) => line.startsWith('data:'))
      .map((line) => line.slice(5).replace(/^ /, ''))
      .join('\n')

    if (!data) return
    // [DONE] 是约定俗成的流结束标记
    if (data === '[DONE]') return
    onChunk && onChunk(data)
  }

  http
    .get('/ai/chat', {
      params: { memoryId, message },
      responseType: 'text',
      signal: abortController.signal,
      headers: { Accept: 'text/event-stream' },
      // 分片到达时触发，真正实现"实时显示对话内容"
      onDownloadProgress: (progressEvent) => {
        const xhr = progressEvent.event && progressEvent.event.target
        if (!xhr || typeof xhr.responseText !== 'string') return

        const allText = xhr.responseText
        // 基于已解析长度提取本次新增的文本
        const newText = allText.slice(receivedLength)
        receivedLength = allText.length
        buffer += newText
        parseSseBuffer(false)
      },
    })
    .then(() => {
      // 服务端 Flux 完成 Connection 会正常断开，解析残留内容
      parseSseBuffer(true)
      onDone && onDone()
    })
    .catch((error) => {
      if (axios.isCancel(error) || error.code === 'ERR_CANCELED') return // 主动取消不算错误
      onError && onError(error)
    })

  return () => abortController.abort()
}
