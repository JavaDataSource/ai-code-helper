import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
// 引入常用语言子集（Java / Python / JS / SQL 等 30+ 种），显著减小打包体积；
// 如需其他语言可改为 import hljs from 'highlight.js'（引入全部语言）
import hljs from 'highlight.js/lib/common'
import DOMPurify from 'dompurify'

// marked 实例：注册 highlight.js 钩子，让代码块渲染时就完成高亮
const marked = new Marked(
  markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      // 有语言标注且 hljs 识别：精确高亮
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(code, { language: lang, ignoreIllegals: true }).value
      }
      // 未标注语言：尝试自动检测（对 AI 输出中不带语言名的代码块更友好）
      if (!lang) {
        return hljs.highlightAuto(code).value
      }
      // 标注了但未识别的语言名：按纯文本转义输出
      return hljs.highlight(code, { language: 'plaintext' }).value
    },
  })
)

marked.use({
  gfm: true, // 支持表格、删除线等 GFM 语法
  breaks: true, // 单个换行渲染为 <br>，贴合聊天场景
})

/**
 * 将 Markdown 文本渲染为安全的 HTML。
 * 用于展示 AI 的流式回复：支持标题 / 列表 / 表格 / 代码块高亮等。
 *
 * @param {string} text AI 回复内容（可以是流式过程中不完整的片段）
 * @returns {string} 经 DOMPurify 消毒后的 HTML 字符串
 */
export function renderMarkdown(text) {
  const html = marked.parse(text ?? '')
  // AI 内容来自外部大模型，统一消毒后再通过 v-html 渲染，防止 XSS
  return DOMPurify.sanitize(html)
}
