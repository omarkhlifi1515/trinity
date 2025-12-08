import './bootstrap';
import { createApp } from 'vue';
import FloatingChatBot from './components/FloatingChatBot.vue';

const el = document.createElement('div');
el.id = 'floating-chat-app';
document.body.appendChild(el);

const app = createApp({});
app.component('FloatingChatBot', FloatingChatBot);
app.mount('#floating-chat-app');


