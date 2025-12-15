'use client'

import { useEffect, useState } from 'react'
import { MessageSquare, Plus, Mail, MailOpen } from 'lucide-react'
import Link from 'next/link'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { Message, subscribeToMessages, markMessageAsRead } from '@/lib/firebase/messages'

export default function MessagesContent() {
  const [messages, setMessages] = useState<Message[]>([])
  const [loading, setLoading] = useState(true)
  const [currentUserId, setCurrentUserId] = useState<string>('')

  useEffect(() => {
    let unsubscribe: () => void;

    const setup = async () => {
      try {
        const user = FirebaseAuthClient.getCurrentUser();
        if (user) {
          init(user.uid);
        } else {
          FirebaseAuthClient.onAuthStateChanged((u) => {
            if (u) init(u.uid);
          });
        }
      } catch (e) { console.error(e); }
    }

    const init = (uid: string) => {
      setCurrentUserId(uid);
      setLoading(true);
      // Subscribe to real-time updates
      unsubscribe = subscribeToMessages(uid, (msgs) => {
        setMessages(msgs);
        setLoading(false);
      });
    }

    setup();

    return () => {
      // Only unsubscribe if it was assigned
      if (unsubscribe) unsubscribe();
    }
  }, [])

  const handleMarkAsRead = async (messageId: string) => {
    try {
      await markMessageAsRead(messageId)
    } catch (e) {
      console.error(e);
    }
  }

  const unreadCount = messages.filter(m => !m.read && (m.receiverId === currentUserId || m.receiverId === 'all') && m.senderId !== currentUserId).length

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Messages</h1>
          <p className="text-gray-600">
            {unreadCount > 0 ? `${unreadCount} unread message${unreadCount > 1 ? 's' : ''}` : 'All messages read'}
          </p>
        </div>
        <Link
          href="/dashboard/messages/new"
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          New Message
        </Link>
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading messages...</p>
        </div>
      ) : messages.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <MessageSquare className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Messages Yet</h3>
          <p className="text-gray-600 mb-6">Send your first message to get started</p>
          <Link
            href="/dashboard/messages/new"
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-5 h-5" />
            New Message
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {messages.map((message) => {
            const isUnread = !message.read && (message.receiverId === currentUserId || message.receiverId === 'all') && message.senderId !== currentUserId
            return (
              <div
                key={message.id}
                className={`bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow cursor-pointer ${isUnread ? 'border-l-4 border-blue-600' : ''
                  }`}
                onClick={() => isUnread && handleMarkAsRead(message.id)}
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-3">
                    {isUnread ? (
                      <Mail className="w-5 h-5 text-blue-600" />
                    ) : (
                      <MailOpen className="w-5 h-5 text-gray-400" />
                    )}
                    <div>
                      <h3 className="font-semibold text-gray-900">{message.subject}</h3>
                      <p className="text-sm text-gray-500">
                        {message.senderId === currentUserId ? 'To: ' : 'From: '}
                        {/* We display email or name if available. For now just generic or stored info */}
                        {message.senderEmail || message.senderName || 'Unknown'}
                      </p>
                    </div>
                  </div>
                  {isUnread && (
                    <span className="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                      New
                    </span>
                  )}
                </div>
                <p className="text-sm text-gray-600 mb-2 line-clamp-2">{message.content}</p>
                <p className="text-xs text-gray-400">
                  {message.createdAt ? new Date(message.createdAt).toLocaleString() : ''}
                </p>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
