import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, RefreshControl, ActivityIndicator, TouchableOpacity } from 'react-native';
import { apiRequest } from '../../lib/api';
import { Calendar, Bell } from 'lucide-react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { supabase } from '../../lib/supabase';
import { useRouter } from 'expo-router';

interface NewsItem {
  id: string;
  title: string;
  body: string;
  created_at: string;
  // Add other fields if necessary based on backend
}

export default function Dashboard() {
  const [news, setNews] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const router = useRouter();

  const fetchNews = async () => {
    try {
      const data = await apiRequest('department/news');
      // Assuming the API returns an array or an object with a list. Adjust as needed.
      // If backend structure is different, this might need mapping.
      // For now, assuming data is the array of news.
      if (Array.isArray(data)) {
         setNews(data);
      } else {
          // Fallback or specific handling if wrapped
          setNews([]);
      }
    } catch (error) {
      console.error('Failed to fetch news:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchNews();
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    fetchNews();
  };

  const handleLogout = async () => {
      await supabase.auth.signOut();
      router.replace('/login');
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="px-6 py-4 border-b border-slate-900 flex-row justify-between items-center">
        <View>
            <Text className="text-slate-400 text-xs font-medium uppercase tracking-wider">Department</Text>
            <Text className="text-white text-2xl font-bold">News Feed</Text>
        </View>
        <TouchableOpacity onPress={handleLogout}>
            <Text className="text-blue-500 font-medium">Logout</Text>
        </TouchableOpacity>
      </View>

      <ScrollView
        className="flex-1 px-4 pt-4"
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#3b82f6" />
        }
      >
        {loading ? (
          <View className="mt-10 items-center">
            <ActivityIndicator size="large" color="#3b82f6" />
          </View>
        ) : news.length === 0 ? (
           <View className="mt-20 items-center">
             <Bell color="#64748b" size={48} />
             <Text className="text-slate-500 mt-4 text-lg">No news available.</Text>
           </View>
        ) : (
          news.map((item, index) => (
            <View key={item.id || index} className="bg-slate-900 rounded-xl p-5 mb-4 border border-slate-800">
              <View className="flex-row justify-between items-start mb-2">
                 <Text className="text-white text-lg font-bold flex-1 mr-2">{item.title}</Text>
                 <View className="flex-row items-center bg-slate-800 px-2 py-1 rounded">
                    <Calendar color="#94a3b8" size={12} className="mr-1" />
                    <Text className="text-slate-400 text-xs">
                        {new Date(item.created_at).toLocaleDateString()}
                    </Text>
                 </View>
              </View>
              <Text className="text-slate-300 leading-relaxed">{item.body}</Text>
            </View>
          ))
        )}
        <View className="h-20" /> 
      </ScrollView>
    </SafeAreaView>
  );
}