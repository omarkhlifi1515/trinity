import React, { useState, useEffect, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, FlatList, KeyboardAvoidingView, Platform, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Send } from 'lucide-react-native';
import { apiRequest } from '../../lib/api';

interface Message {
  id?: string; // Optional because optimistic updates might not have an ID yet
  role: 'user' | 'assistant' | 'system'; // Adjust based on backend (User vs Trinity/Bot)
  content: string;
  created_at?: string;
}

export default function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [sending, setSending] = useState(false);
  const flatListRef = useRef<FlatList>(null);

  // Polling for chat history
  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const data = await apiRequest('chat/history');
        // Assuming data is an array of messages sorted by time
        // If the backend returns { history: [...] }, adjust accordingly.
        // We want the latest messages at the bottom.
        if (Array.isArray(data)) {
             setMessages(data);
        }
      } catch (error) {
        // Silent fail for polling or just log
        console.log('Polling chat error', error);
      }
    };

    fetchHistory(); // Initial fetch
    const intervalId = setInterval(fetchHistory, 3000); // Poll every 3 seconds

    return () => clearInterval(intervalId);
  }, []);

  useEffect(() => {
    // Scroll to bottom when messages change
    if (messages.length > 0) {
       setTimeout(() => {
          flatListRef.current?.scrollToEnd({ animated: true });
       }, 100);
    }
  }, [messages]);

  const sendMessage = async () => {
    if (!inputText.trim()) return;

    const userMsg: Message = { role: 'user', content: inputText };
    setMessages((prev) => [...prev, userMsg]); // Optimistic update
    setInputText('');
    setSending(true);

    try {
      await apiRequest('chat/send', 'POST', { message: userMsg.content });
      // The polling will pick up the actual response + persisted user message shortly
    } catch (error) {
      console.error('Failed to send message:', error);
      // Ideally show error toast or revert optimistic update
    } finally {
      setSending(false);
    }
  };

  const renderItem = ({ item }: { item: Message }) => {
    const isUser = item.role === 'user';
    return (
      <View className={`flex-row mb-4 ${isUser ? 'justify-end' : 'justify-start'}`}>
        <View
          className={`max-w-[80%] rounded-2xl px-4 py-3 ${
            isUser ? 'bg-blue-600 rounded-br-none' : 'bg-slate-800 rounded-bl-none'
          }`}
        >
          <Text className={`text-base ${isUser ? 'text-white' : 'text-slate-200'}`}>
            {item.content}
          </Text>
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="px-4 py-3 border-b border-slate-900 bg-slate-950 z-10">
        <Text className="text-white text-xl font-bold text-center">Trinity Assistant</Text>
      </View>

      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item, index) => item.id || index.toString()}
        renderItem={renderItem}
        contentContainerStyle={{ padding: 16, paddingBottom: 20 }}
        className="flex-1"
      />

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
        className="bg-slate-950 border-t border-slate-900 px-4 py-3"
      >
        <View className="flex-row items-center space-x-3">
          <TextInput
            className="flex-1 bg-slate-900 text-white rounded-full px-4 py-3 border border-slate-800 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 max-h-24"
            placeholder="Ask Trinity..."
            placeholderTextColor="#64748b"
            value={inputText}
            onChangeText={setInputText}
            multiline
          />
          <TouchableOpacity
            onPress={sendMessage}
            disabled={sending || !inputText.trim()}
            className={`w-12 h-12 rounded-full items-center justify-center ${
              !inputText.trim() ? 'bg-slate-800' : 'bg-blue-600'
            }`}
          >
            {sending ? (
                <ActivityIndicator size="small" color="white" />
            ) : (
                <Send size={20} color={!inputText.trim() ? '#475569' : 'white'} />
            )}
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}