// Lightweight dashboard chatbot bootstrapper
(function () {
    if (!document.getElementById('dashboard-chat')) return;

    function loadScript(src, cb) {
        var s = document.createElement('script');
        s.src = src;
        s.onload = cb;
        s.onerror = cb;
        document.head.appendChild(s);
    }

    function init() {
        // Define a simple ChatBot component using Vue 3 global build
        var ChatBot = {
            template: `
                <div style="font-family: Arial, Helvetica, sans-serif; width: 320px; max-width: calc(100vw - 40px);">
                  <div style="box-shadow:0 10px 30px rgba(0,0,0,0.2); border-radius:12px; overflow:hidden;">
                    <div style="background:#0ea5a4; color:white; padding:10px 14px; display:flex; align-items:center; justify-content:space-between;">
                      <div style="font-weight:600">Assistant</div>
                      <button @click="toggle" style="background:transparent;border:0;color:white;font-size:16px;cursor:pointer">{{ open ? '✕' : '💬' }}</button>
                    </div>
                    <div v-if="open" style="background:white; padding:12px;">
                      <div style="height:200px; overflow:auto; border:1px solid #eef2f7; padding:8px; border-radius:6px; background:#fbfdff;">
                        <div v-for="(m,idx) in messages" :key="idx" style="margin-bottom:8px;">
                          <div style="font-size:13px; color:#0f172a;"><strong v-if="m.from==='user'">You:</strong><strong v-else>Bot:</strong> {{ m.text }}</div>
                        </div>
                      </div>
                      <div style="display:flex; gap:8px; margin-top:8px;">
                        <input v-model="input" @keydown.enter.prevent="send" placeholder="Ask something..." style="flex:1;padding:8px;border:1px solid #e6eef7;border-radius:6px;" />
                        <button @click="send" style="background:#0ea5a4;color:white;border:0;padding:8px 12px;border-radius:6px;cursor:pointer">Send</button>
                      </div>
                    </div>
                  </div>
                </div>
            `,
            data: function () {
                return {
                    open: false,
                    input: '',
                    messages: [
                        { from: 'bot', text: 'Hello — I\'m your assistant. Ask me about the HR app.' }
                    ]
                };
            },
            methods: {
                toggle: function () { this.open = !this.open; },
                send: function () {
                    var text = this.input && this.input.trim();
                    if (!text) return;
                    this.messages.push({ from: 'user', text: text });
                    this.input = '';
                    var self = this;
                    // placeholder reply — replace with fetch to your chat endpoint
                    setTimeout(function () {
                        self.messages.push({ from: 'bot', text: 'Thanks — I received: "' + text + '" (this is a demo reply).' });
                    }, 700);
                }
            }
        };

        var app = Vue.createApp({});
        app.component('chat-bot', ChatBot);
        app.mount('#dashboard-chat');
    }

    if (typeof Vue === 'undefined' || !Vue.createApp) {
        // load Vue 3 global build from CDN (falls back silently on error)
        loadScript('https://unpkg.com/vue@3/dist/vue.global.prod.js', init);
    } else {
        init();
    }
})();
