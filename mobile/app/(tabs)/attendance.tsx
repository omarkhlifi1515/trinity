import React from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Clock } from 'lucide-react-native';

export default function Attendance() {
  
  const handleCheckIn = () => {
    console.log('Checked In');
    Alert.alert('Success', 'Checked In Successfully at ' + new Date().toLocaleTimeString());
  };

  const handleCheckOut = () => {
    console.log('Checked Out');
    Alert.alert('Success', 'Checked Out Successfully at ' + new Date().toLocaleTimeString());
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950 justify-center items-center px-6">
      <View className="items-center mb-12">
        <Clock size={64} color="#3b82f6" />
        <Text className="text-white text-3xl font-bold mt-4">Attendance</Text>
        <Text className="text-slate-400 text-center mt-2">
          Record your daily attendance below.
        </Text>
      </View>

      <View className="w-full space-y-6 gap-6">
        <TouchableOpacity
          onPress={handleCheckIn}
          className="w-full bg-green-600 active:bg-green-700 py-6 rounded-2xl items-center shadow-lg shadow-green-900/20 border border-green-500"
        >
          <Text className="text-white text-2xl font-bold uppercase tracking-widest">
            Check In
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={handleCheckOut}
          className="w-full bg-red-600 active:bg-red-700 py-6 rounded-2xl items-center shadow-lg shadow-red-900/20 border border-red-500"
        >
          <Text className="text-white text-2xl font-bold uppercase tracking-widest">
            Check Out
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}