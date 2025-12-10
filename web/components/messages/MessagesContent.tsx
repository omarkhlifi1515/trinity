'use client'

import { useEffect, useState } from 'react'
import { MessageSquare, Plus, Mail, MailOpen } from 'lucide-react'
import Link from 'next/link'
import { getMessages, addMessage, Message, subscribeToMessages, markMessageAsRead } from '@/lib/storage/supabase-storage'
import { getCurrentUser } from '@/lib/auth/local-auth'

export default function MessagesContent() {
  const [messages, setMessages] = useState<Message[]>([])
  const [loading, setLoading] = useState(true)
  const [currentUserId, setCurrentUserId] = useState<string>('')

  useEffect(() => {
    loadUser()
    loadMessages()
    
    // Set up real-time subscription
    const channel = subscribeToMessages((newMessage) => {
      // Add new message to the list
      setMessages(prev => [newMessage, ...prev])
    })

    // Cleanup subscription on unmount
    return () => {
      channel.unsubscribe()
    }
  }, [])

  const loadUser = async () => {
    try {
      const res = await fetch('/api/auth/me')
      const data = await res.json()
      if (data.user) {
        setCurrentUserId(data.user.id)
      }
    } catch (error) {
      console.error('Error loading user:', error)
    }
  }

  const loadMessages = async () => {
    try {
      setLoading(true)
      const data = await getMessages(currentUserId)
      setMessages(data)
    } catch (error) {
      console.error('Error loading messages:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAsRead = async (messageId: string) => {
    await markMessageAsRead(messageId)
    setMessages(prev => prev.map(msg => 
      msg.id === messageId ? { ...msg, read: true } : msg
    ))
  }

  const unreadCount = messages.filter(m => !m.read && m.to_user === currentUserId).length

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
            const isUnread = !message.read && message.to_user === currentUserId
            return (
              <div
                key={message.id}
                className={`bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow cursor-pointer ${
                  isUnread ? 'border-l-4 border-blue-600' : ''
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
                        {message.from_user === currentUserId ? 'To: ' : 'From: '}
                        {message.to_user || 'All'}
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
                  {new Date(message.created_at).toLocaleString()}
                </p>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
