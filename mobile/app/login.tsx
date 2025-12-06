import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert, KeyboardAvoidingView, Platform, ActivityIndicator } from 'react-native';
import { supabase } from '../lib/supabase';
import { useRouter } from 'expo-router';
import { Mail, Lock } from 'lucide-react-native';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  async function signInWithEmail() {
    setLoading(true);
    const { error } = await supabase.auth.signInWithPassword({
      email,
      password,
    });

    if (error) {
      Alert.alert('Sign In Error', error.message);
    } else {
      // Auth state change will trigger redirect in _layout.tsx
    }
    setLoading(false);
  }

  return (
    <KeyboardAvoidingView 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      className="flex-1 justify-center items-center bg-slate-950 px-6"
    >
      <View className="w-full max-w-sm">
        <Text className="text-3xl font-bold text-white text-center mb-8">
          Trinity <Text className="text-blue-500">Mobile</Text>
        </Text>

        <View className="space-y-4">
          <View>
            <Text className="text-slate-400 mb-2 text-sm font-medium">Email</Text>
            <View className="relative">
              <View className="absolute left-3 top-3 z-10">
                 <Mail color="#94a3b8" size={20} />
              </View>
              <TextInput
                className="w-full bg-slate-900 border border-slate-800 rounded-lg py-3 pl-10 pr-4 text-white placeholder:text-slate-600 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                placeholder="name@company.com"
                placeholderTextColor="#475569"
                autoCapitalize="none"
                value={email}
                onChangeText={setEmail}
              />
            </View>
          </View>

          <View>
             <Text className="text-slate-400 mb-2 text-sm font-medium">Password</Text>
             <View className="relative">
              <View className="absolute left-3 top-3 z-10">
                 <Lock color="#94a3b8" size={20} />
              </View>
              <TextInput
                className="w-full bg-slate-900 border border-slate-800 rounded-lg py-3 pl-10 pr-4 text-white placeholder:text-slate-600 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                placeholder="••••••••"
                placeholderTextColor="#475569"
                secureTextEntry
                autoCapitalize="none"
                value={password}
                onChangeText={setPassword}
              />
             </View>
          </View>
        </View>

        <TouchableOpacity
          className={`mt-8 w-full py-3 rounded-lg flex-row justify-center items-center ${loading ? 'bg-blue-700' : 'bg-blue-600 active:bg-blue-700'}`}
          onPress={signInWithEmail}
          disabled={loading}
        >
          {loading ? (
             <ActivityIndicator color="white" />
          ) : (
            <Text className="text-white font-semibold text-lg">Sign In</Text>
          )}
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}