<template>
  <!-- 浮动按钮 -->
  <button v-if="!open" class="ai-float-btn" @click="open = true">
    <span class="ai-float-icon">🤖</span>
  </button>

  <!-- 侧边抽屉 -->
  <Teleport to="body">
    <div v-if="open" class="ai-overlay" @click.self="open = false">
      <div class="ai-panel" @click.stop>
        <div class="ai-panel-header">
          <div class="ai-header-left">
            <span class="ai-avatar">🤖</span>
            <div>
              <div class="ai-title">AI 智能导购</div>
              <div class="ai-subtitle">小L · 随时为您服务</div>
            </div>
          </div>
          <button class="ai-close" @click="open = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="ai-messages" ref="msgContainer">
          <div v-if="messages.length === 0" class="ai-welcome">
            <p class="welcome-text">你好！我是 LUMINA 的 AI 导购 <strong>小L</strong> ✨</p>
            <p class="welcome-hint">可以帮你找商品、查订单、问物流、咨询尺码……试试问我吧</p>
            <div class="quick-asks">
              <button v-for="q in quickQuestions" :key="q" class="quick-btn" @click="sendQuick(q)">{{ q }}</button>
            </div>
          </div>

          <div v-for="(msg, i) in messages" :key="i" class="msg-wrapper">
            <div v-if="msg.role === 'user'" class="msg-user">{{ msg.content }}</div>
            <div v-else class="msg-ai">
              <div class="msg-ai-text" v-html="renderMd(msg.content)"></div>
              <div v-if="msg.products?.length" class="msg-products">
                <div v-for="p in msg.products" :key="p.id" class="msg-pc" @click="goProduct(p.id)">
                  <div class="mp-img">
                    <img v-if="p.image" :src="p.image" :alt="p.name" />
                    <span v-else>📦</span>
                  </div>
                  <div class="mp-info">
                    <div class="mp-name">{{ p.name }}</div>
                    <div class="mp-price">¥{{ p.price }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="msg-ai">
            <div class="typing"><span></span><span></span><span></span></div>
          </div>
        </div>

        <div class="ai-foot">
          <input v-model="input" class="ai-input" placeholder="输入需求..." @keyup.enter="send" :disabled="loading" />
          <button class="ai-send" @click="send" :disabled="loading || !input.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { chatApi } from '@/api/ai'
import type { ChatMessage, ProductRef } from '@/api/ai'

const router = useRouter()
const open = ref(false)
const messages = ref<{ role: string; content: string; products?: ProductRef[] }[]>([])
const input = ref('')
const loading = ref(false)
const msgContainer = ref<HTMLElement>()
const history = ref<ChatMessage[]>([])

const quickQuestions = [
  '帮我推荐几款热销商品',
  '有没有优惠券可以领？',
  '退换货有什么政策？',
  '我穿M码合适吗？',
]

function sendQuick(q: string) { input.value = q; send() }

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text })
  loading.value = true
  await scrollDown()

  try {
    const res = await chatApi(text, history.value)
    history.value.push({ role: 'user', content: text })
    history.value.push({ role: 'assistant', content: res.reply })
    if (history.value.length > 20) history.value = history.value.slice(-20)

    messages.value.push({ role: 'assistant', content: res.reply, products: res.products || undefined })
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，我暂时无法处理，请稍后再试。' })
  } finally {
    loading.value = false
    await scrollDown()
  }
}

function goProduct(id: number) { router.push('/product/' + id); open.value = false }
async function scrollDown() { await nextTick(); if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight }
function renderMd(t: string) { return t.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>') }
</script>

<style scoped>
/* Float button */
.ai-float-btn {
  position: fixed; bottom: 28px; right: 28px; z-index: 999;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #1a1a1a, #2d2d2d);
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  display: flex; align-items: center; justify-content: center;
  animation: pulse 2.5s ease-in-out infinite; border: none; cursor: pointer;
}
.ai-float-btn:hover { transform: scale(1.1); box-shadow: 0 6px 28px rgba(0,0,0,0.3); }
.ai-float-icon { font-size: 26px; }
@keyframes pulse {
  0%, 100% { box-shadow: 0 4px 20px rgba(0,0,0,0.2); }
  50% { box-shadow: 0 4px 28px rgba(201,169,110,0.4); }
}

/* Overlay */
.ai-overlay {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(0,0,0,0.3);
  display: flex; justify-content: flex-end;
}

/* Panel */
.ai-panel {
  width: 420px; max-width: 100vw; height: 100vh;
  background: #fff; box-shadow: -4px 0 40px rgba(0,0,0,0.15);
  display: flex; flex-direction: column;
}
.ai-panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #1a1a1a, #2d2d2d); color: #fff;
}
.ai-header-left { display: flex; align-items: center; gap: 12px; }
.ai-avatar { font-size: 28px; }
.ai-title { font-size: 16px; font-weight: 700; }
.ai-subtitle { font-size: 12px; color: rgba(255,255,255,0.6); }
.ai-close {
  width: 36px; height: 36px; border-radius: 8px; border: none; background: none;
  display: flex; align-items: center; justify-content: center;
  color: rgba(255,255,255,0.7); cursor: pointer;
}
.ai-close:hover { background: rgba(255,255,255,0.1); color: #fff; }

/* Messages */
.ai-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.ai-welcome { text-align: center; padding: 32px 8px; }
.welcome-text { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.welcome-hint { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.quick-asks { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.quick-btn {
  padding: 8px 14px; border-radius: 20px; font-size: 12px; border: 1px solid var(--border);
  background: var(--bg-warm); color: var(--text-secondary); cursor: pointer; transition: all .2s;
}
.quick-btn:hover { border-color: var(--accent); color: var(--accent); }

.msg-user {
  align-self: flex-end; max-width: 80%; padding: 10px 16px;
  background: #1a1a1a; color: #fff; border-radius: 18px 18px 4px 18px; font-size: 14px; line-height: 1.5;
}
.msg-ai { max-width: 100%; }
.msg-ai-text {
  padding: 10px 16px; background: #f5f3ef; border-radius: 4px 18px 18px 18px;
  font-size: 14px; line-height: 1.6; color: var(--text);
}
.msg-products { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.msg-pc {
  width: calc(50% - 4px); background: #fff; border-radius: 10px; overflow: hidden;
  cursor: pointer; border: 1px solid var(--border); transition: all .2s;
}
.msg-pc:hover { box-shadow: var(--shadow-sm); }
.mp-img {
  width: 100%; aspect-ratio: 1; background: #f5f3ef;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.mp-img img { width: 100%; height: 100%; object-fit: cover; }
.mp-info { padding: 8px 10px; }
.mp-name { font-size: 12px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mp-price { font-size: 13px; font-weight: 700; color: var(--accent); margin-top: 2px; }

.typing { display: flex; gap: 4px; padding: 12px 16px; }
.typing span {
  width: 8px; height: 8px; border-radius: 50%; background: #ccc;
  animation: bounce 1.4s infinite ease-in-out both;
}
.typing span:nth-child(1) { animation-delay: 0s; }
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); }
  40% { transform: scale(1); }
}

/* Foot */
.ai-foot { display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid var(--border); }
.ai-input {
  flex: 1; padding: 10px 16px; border: 1px solid var(--border); border-radius: 24px;
  font-size: 14px; outline: none;
}
.ai-input:focus { border-color: var(--accent); }
.ai-send {
  width: 40px; height: 40px; border-radius: 50%; border: none;
  display: flex; align-items: center; justify-content: center;
  background: #1a1a1a; color: #fff; cursor: pointer; flex-shrink: 0;
}
.ai-send:hover:not(:disabled) { background: var(--accent); }
.ai-send:disabled { opacity: .4; cursor: not-allowed; }
</style>
