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
  const clean = DOMPurify.sanitize(html)
  // 在消毒之后注入代码块头部（仅包含我们自己的静态结构，不会引入外部内容）
  return decorateCodeBlocks(clean)
}

/**
 * 给每个代码块包裹一层容器，加入「语言标签 + 一键复制」头部。
 * 复制按钮不绑定事件（v-html 无法附加监听），由消息列表通过事件委托统一处理。
 */
function decorateCodeBlocks(html) {
  const container = document.createElement('div')
  container.innerHTML = html

  container.querySelectorAll('pre > code').forEach((code) => {
    const pre = code.parentElement
    // 已包裹过的不重复处理
    if (pre.parentElement?.classList.contains('code-block')) return

    // 从 hljs 的 language-xxx 类名中提取语言标签
    const langClass = Array.from(code.classList).find((c) => c.startsWith('language-'))
    const lang = langClass ? langClass.slice('language-'.length) : ''

    const wrapper = document.createElement('div')
    wrapper.className = 'code-block'

    const header = document.createElement('div')
    header.className = 'code-block-header'
    const langLabel = document.createElement('span')
    langLabel.className = 'code-block-lang'
    langLabel.textContent = lang || '代码'
    const copyBtn = document.createElement('button')
    copyBtn.type = 'button'
    copyBtn.className = 'code-copy-btn'
    copyBtn.textContent = '复制'
    header.append(langLabel, copyBtn)

    pre.replaceWith(wrapper)
    wrapper.append(header, pre)
  })

  return container.innerHTML
}
