import { createApp } from 'vue';
import ChatBot from './components/ChatBot.vue';

// Only mount if the element exists on the page
if (typeof document !== 'undefined' && document.getElementById('dashboard-chat')) {
  const el = document.getElementById('dashboard-chat');
  const app = createApp({});
  app.component('ChatBot', ChatBot);
  app.mount('#dashboard-chat');
}
