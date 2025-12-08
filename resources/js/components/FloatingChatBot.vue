<template>
  <div id="floating-chat">
    <div class="chat-header" @click="toggleOpen">
      💬 AI Chat
    </div>

    <div v-if="open" class="chat-body">
      <div class="messages">
        <div v-for="(m,i) in messages" :key="i" :class="m.sender_type=='App\\Bots\\AI'?'bot':'user'">
          {{ m.content }}
        </div>
      </div>
      <div class="input">
        <input v-model="input" @keydown.enter="send" placeholder="Ask me..." />
        <button @click="send">Send</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      open: false,
      input: '',
      messages: []
    }
  },
  methods: {
    toggleOpen() {
      this.open = !this.open;
    },
    async send() {
      if(!this.input) return;
      const msg = this.input;
      this.input='';
      
      // add user message immediately
      this.messages.push({content: msg, sender_type:'App\\Models\\User'});

      const res = await axios.post('/dashboard/chat', { message: msg });

      // add bot message
      this.messages.push(res.data.bot_message);
    }
  }
}
</script>

<style scoped>
#floating-chat {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 300px;
  z-index: 9999;
  font-family: sans-serif;
}

.chat-header {
  background: #4CAF50;
  color: #fff;
  padding: 10px;
  cursor: pointer;
  border-radius: 10px 10px 0 0;
  text-align: center;
}

.chat-body {
  border:1px solid #ddd;
  border-radius: 0 0 10px 10px;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: 400px;
}

.messages {
  flex:1;
  overflow:auto;
  padding:10px;
}

.user { text-align:right; background:#DCF8C6; margin:5px; padding:6px 10px; border-radius:6px; }
.bot { text-align:left; background:#eee; margin:5px; padding:6px 10px; border-radius:6px; }

.input { display:flex; border-top:1px solid #ddd; }
input { flex:1; border:none; padding:10px; }
button { padding:10px 15px; background:#4CAF50; color:white; border:none; }
</style>
