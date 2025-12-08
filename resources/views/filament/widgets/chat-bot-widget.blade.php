<div
    x-data="{
        isOpen: false,
        message: '',
        messages: [],
        isLoading: false,
        rateLimited: false,
        retryAfter: null,
        retryTimer: null,
        async sendMessage() {
            if (!this.message.trim() || this.isLoading || this.rateLimited) return;

            // 1. Add user message to UI immediately
            this.messages.push({
                role: 'user',
                content: this.message
            });

            const payload = { message: this.message };
            const userMessage = this.message;
            this.message = '';
            this.isLoading = true;

            try {
                // 2. Send to the route defined in your web.php
                const csrfToken = document.head.querySelector('meta[name=csrf-token]')?.content || '';
                
                const response = await fetch('/dashboard/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'X-CSRF-TOKEN': csrfToken,
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: JSON.stringify(payload),
                    credentials: 'same-origin'
                });

                const data = await response.json();

                // Handle rate limiting (429)
                if (response.status === 429) {
                    this.rateLimited = true;
                    // Try to get retry-after from headers or response body
                    const retryAfterHeader = response.headers.get('Retry-After');
                    const retryAfterBody = data.retry_after;
                    const retrySeconds = retryAfterHeader ? parseInt(retryAfterHeader) : 
                                        (retryAfterBody ? parseInt(retryAfterBody) : 120);
                    
                    this.retryAfter = retrySeconds;
                    this.startRetryTimer();
                    
                    // Use the error message from the server (it explains if it's Google Gemini or server limit)
                    throw new Error(data.error || `Rate limit exceeded. Please wait ${retrySeconds} seconds before trying again.`);
                }

                if (!response.ok) {
                    throw new Error(data.error || `HTTP ${response.status}: ${response.statusText}`);
                }

                // 3. Add bot response to UI
                if (data.bot_message && data.bot_message.content) {
                    this.messages.push({
                        role: 'bot',
                        content: data.bot_message.content
                    });
                    this.rateLimited = false;
                    this.retryAfter = null;
                    if (this.retryTimer) {
                        clearInterval(this.retryTimer);
                        this.retryTimer = null;
                    }
                } else {
                    throw new Error('Invalid response format from server');
                }

            } catch (error) {
                console.error('Chat Error:', error);
                const errorMessage = error.message || 'Sorry, I encountered an error. Please try again.';
                
                // Show error message - rate limit messages are updated by timer
                if (this.rateLimited && this.retryAfter) {
                    // Show rate limit message with countdown (will be updated by timer)
                    const lastSystemMsg = this.messages.findLast(msg => msg.role === 'system');
                    if (!lastSystemMsg || !lastSystemMsg.content.includes('Rate limit')) {
                        this.messages.push({
                            role: 'system',
                            content: errorMessage || `Rate limit exceeded. Please wait ${this.retryAfter} seconds before trying again.`
                        });
                    }
                } else if (!errorMessage.includes('Rate limit') && !errorMessage.includes('rate limit')) {
                    // Show non-rate-limit errors normally
                    this.messages.push({
                        role: 'system',
                        content: errorMessage
                    });
                }
            } finally {
                this.isLoading = false;
                // Scroll to bottom
                this.$nextTick(() => {
                    const chatBox = this.$refs.chatBox;
                    chatBox.scrollTop = chatBox.scrollHeight;
                });
            }
        },
        startRetryTimer() {
            if (this.retryTimer) {
                clearInterval(this.retryTimer);
            }
            this.retryTimer = setInterval(() => {
                if (this.retryAfter > 0) {
                    this.retryAfter--;
                    // Update the last system message if it exists
                    const lastMsg = this.messages[this.messages.length - 1];
                    if (lastMsg && lastMsg.role === 'system' && lastMsg.content.includes('Rate limit')) {
                        lastMsg.content = `Rate limit exceeded. Please wait ${this.retryAfter} seconds before trying again.`;
                    }
                } else {
                    this.rateLimited = false;
                    this.retryAfter = null;
                    clearInterval(this.retryTimer);
                    this.retryTimer = null;
                    // Remove rate limit message
                    this.messages = this.messages.filter(msg => !(msg.role === 'system' && msg.content.includes('Rate limit')));
                }
            }, 1000);
        }
    }"
    class="fixed bottom-5 right-5 flex flex-col items-end gap-4"
    style="z-index: 99999 !important; position: fixed !important; bottom: 1.25rem !important; right: 1.25rem !important;"
>
    <div
        x-show="isOpen"
        x-transition
        class="w-80 h-96 bg-white border border-gray-200 rounded-lg shadow-xl flex flex-col overflow-hidden dark:bg-gray-800 dark:border-gray-700"
        style="display: none;"
    >
        <div class="bg-primary-600 p-4 text-white font-bold flex justify-between items-center" style="background-color: rgb(14 165 233);">
            <div class="flex items-center gap-2">
                <span>🤖 Gemini AI Assistant</span>
                <span x-show="rateLimited" class="text-xs bg-yellow-500 px-2 py-1 rounded">
                    Rate Limited (<span x-text="retryAfter"></span>s)
                </span>
            </div>
            <button @click="isOpen = false" class="hover:text-gray-200">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <div x-ref="chatBox" class="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-50 dark:bg-gray-900">
            <template x-for="(msg, index) in messages" :key="index">
                <div :class="msg.role === 'user' ? 'text-right' : 'text-left'">
                    <div
                        class="inline-block px-3 py-2 rounded-lg text-sm max-w-[85%]"
                        :class="msg.role === 'user'
                            ? 'bg-blue-500 text-white rounded-br-none'
                            : 'bg-white border border-gray-200 text-gray-800 rounded-bl-none dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600'"
                    >
                        <span x-text="msg.content"></span>
                    </div>
                </div>
            </template>

            <div x-show="isLoading" class="text-left">
                <div class="inline-block px-3 py-2 bg-gray-100 rounded-lg rounded-bl-none text-xs text-gray-500 animate-pulse dark:bg-gray-800">
                    Typing...
                </div>
            </div>
        </div>

        <div class="p-3 bg-white border-t border-gray-200 dark:bg-gray-800 dark:border-gray-700">
            <form @submit.prevent="sendMessage" class="flex gap-2">
                <input
                    x-model="message"
                    type="text"
                    :placeholder="rateLimited ? 'Rate limited. Please wait...' : 'Ask something...'"
                    class="flex-1 text-sm rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="rateLimited"
                >
                <button
                    type="submit"
                    class="p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="isLoading || rateLimited"
                    :title="rateLimited ? 'Please wait ' + retryAfter + ' seconds' : 'Send message'"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5" />
                    </svg>
                </button>
            </form>
        </div>
    </div>

    <button
        @click="isOpen = !isOpen"
        class="h-14 w-14 rounded-full bg-blue-600 text-white shadow-lg flex items-center justify-center hover:bg-blue-700 transition-transform hover:scale-105"
        style="background-color: rgb(14 165 233);"
    >
        <svg x-show="!isOpen" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a5.969 5.969 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" />
        </svg>

        <svg x-show="isOpen" style="display: none;" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
        </svg>
    </button>
</div>