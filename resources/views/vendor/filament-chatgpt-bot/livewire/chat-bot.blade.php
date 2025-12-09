{{-- Custom Filament ChatGPT Bot View - Matches Old Chatbot Style --}}
{{-- This file should be at: resources/views/vendor/filament-chatgpt-bot/livewire/chat-bot.blade.php --}}
{{-- After publishing views, replace the published file with this content --}}

<div class="relative w-full">
    <!-- Floating Chat Button - Bottom Right (Old Style) -->
    <div class="fixed bottom-5 right-5 z-30 cursor-pointer" style="z-index: 99999 !important;">
        <div class="relative h-14 w-14 rounded-full text-white flex items-center justify-center shadow-lg hover:scale-105 transition-transform" 
             style="background-color: rgb(14 165 233);" 
             wire:click="$toggle('panelHidden')" 
             id="btn-chat">
            @if($panelHidden)
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a5.969 5.969 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" />
                </svg>
            @else
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
            @endif
        </div>
    </div>

    <!-- Chat Window - Old Style -->
    <div class="fixed {{ $winPosition=="left"?"left-0":"right-0" }} bottom-0 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-t-lg shadow-xl z-30 {{ $panelHidden ? 'hidden' : '' }}" 
         style="width: 320px; height: 384px; {{ $winWidth }}" 
         id="chat-window">
        
        <!-- Header - Old Style Blue -->
        <div class="bg-primary-600 p-4 text-white font-bold flex justify-between items-center rounded-t-lg" style="background-color: rgb(14 165 233);">
            <div class="flex items-center gap-2">
                <span>🤖 Gemini AI Assistant</span>
            </div>
            <div class="flex items-center gap-2">
                <button type="button" 
                        class="inline-flex items-center justify-center rounded-lg h-8 w-8 transition text-white hover:bg-blue-700 focus:outline-none" 
                        wire:click="resetSession()" 
                        title="Clear chat">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                    </svg>
                </button>
                <button type="button" 
                        class="inline-flex items-center justify-center rounded-lg h-8 w-8 transition text-white hover:bg-blue-700 focus:outline-none" 
                        wire:click="$toggle('panelHidden')" 
                        title="Close">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </div>

        <!-- Messages Area - Old Style -->
        <div id="messages" class="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-50 dark:bg-gray-900" style="height: calc(100% - 140px);">
            @foreach($messages as $message)
                @if($message['role'] !== 'system')
                    @if($message['role'] == "assistant")
                        <!-- Bot Message - Left Aligned -->
                        <div class="text-left">
                            <div class="inline-block px-3 py-2 rounded-lg text-sm max-w-[85%] bg-white border border-gray-200 text-gray-800 rounded-bl-none dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600">
                                {!! \Illuminate\Mail\Markdown::parse($message['content'] ?? '') !!}
                            </div>
                        </div>
                    @else
                        <!-- User Message - Right Aligned -->
                        <div class="text-right">
                            <div class="inline-block px-3 py-2 rounded-lg text-sm max-w-[85%] bg-blue-500 text-white rounded-br-none">
                                {!! \Illuminate\Mail\Markdown::parse($message['content'] ?? '') !!}
                            </div>
                        </div>
                    @endif
                @endif
            @endforeach
            
            <!-- Loading Indicator -->
            <div wire:loading wire:target="sendMessage" class="text-left">
                <div class="inline-block px-3 py-2 bg-gray-100 rounded-lg rounded-bl-none text-xs text-gray-500 animate-pulse dark:bg-gray-800">
                    Typing...
                </div>
            </div>
        </div>

        <!-- Input Area - Old Style -->
        <div class="p-3 bg-white border-t border-gray-200 dark:bg-gray-800 dark:border-gray-700">
            <div class="py-2 text-sm text-blue-600 dark:text-blue-400" wire:loading wire:target="sendMessage">Message Sending...</div>
            <form wire:submit.prevent="sendMessage" class="flex gap-2">
                <input
                    wire:model.defer="question"
                    type="text"
                    placeholder="Ask something..."
                    class="flex-1 text-sm rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    id="chat-input"
                >
                <button
                    type="submit"
                    wire:loading.attr="disabled"
                    class="p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    style="background-color: rgb(14 165 233);"
                    title="Send message"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5" />
                    </svg>
                </button>
            </form>
        </div>
    </div>

    <script>
        const el = document.getElementById('messages')
        window.onload = function(){
            if(el) el.scrollTop = el.scrollHeight
        }

        window.addEventListener('sendmessage', event => {
            if(el) el.scrollTop = el.scrollHeight
        })

        // Auto-scroll on new messages
        Livewire.hook('message.processed', () => {
            setTimeout(() => {
                if(el) el.scrollTop = el.scrollHeight
            }, 100)
        })
    </script>
</div>
