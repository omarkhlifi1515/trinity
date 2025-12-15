'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { MessageSquare, ArrowLeft } from 'lucide-react'
import { sendMessage } from '@/lib/firebase/messages'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getAllUsers, UserProfile } from '@/lib/firebase/users'

export default function NewMessagePage() {
    const router = useRouter()
    const [loading, setLoading] = useState(false)
    const [employees, setEmployees] = useState<UserProfile[]>([])
    const [currentUser, setCurrentUser] = useState<any>(null)

    const [formData, setFormData] = useState({
        recipientId: '',
        subject: '',
        content: ''
    })

    useEffect(() => {
        loadData();
    }, [])

    const loadData = async () => {
        // Load user
        let uid = '';
        const u = FirebaseAuthClient.getCurrentUser();
        if (u) {
            setCurrentUser(u);
            uid = u.uid;
        } else {
            FirebaseAuthClient.onAuthStateChanged((u) => {
                if (u) {
                    setCurrentUser(u);
                    uid = u.uid;
                }
            });
        }

        // Load employees for dropdown
        try {
            const users = await getAllUsers();
            setEmployees(users);
        } catch (e) { console.error(e); }
    }


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!currentUser) return;

        setLoading(true)
        try {
            const recipient = employees.find(e => e.uid === formData.recipientId);

            await sendMessage({
                senderId: currentUser.uid,
                senderEmail: currentUser.email!,
                senderName: currentUser.displayName,
                receiverId: formData.recipientId, // 'all' or specific ID
                receiverEmail: recipient?.email,
                subject: formData.subject,
                content: formData.content
            })
            router.push('/dashboard/messages')
        } catch (error) {
            console.error('Error sending message:', error)
            alert('Failed to send message')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="p-8 max-w-2xl mx-auto">
            <button
                onClick={() => router.back()}
                className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-8"
            >
                <ArrowLeft className="w-4 h-4" />
                Back to Messages
            </button>

            <div className="bg-white rounded-lg shadow p-8">
                <div className="flex items-center gap-4 mb-6">
                    <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                        <MessageSquare className="w-6 h-6 text-blue-600" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">New Message</h1>
                        <p className="text-gray-600">Send a message to a colleague</p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Recipient
                        </label>
                        <select
                            required
                            value={formData.recipientId}
                            onChange={(e) => setFormData({ ...formData, recipientId: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                        >
                            <option value="">Select a recipient</option>
                            <option value="all">Reference (All Users - if authorized)</option>
                            {employees.map(emp => (
                                <option key={emp.uid} value={emp.uid}>
                                    {emp.displayName || emp.email} ({emp.role})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Subject
                        </label>
                        <input
                            type="text"
                            required
                            value={formData.subject}
                            onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                            placeholder="Message subject"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Message
                        </label>
                        <textarea
                            required
                            rows={6}
                            value={formData.content}
                            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none resize-none"
                            placeholder="Type your message here..."
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                    >
                        {loading ? 'Sending...' : 'Send Message'}
                    </button>
                </form>
            </div>
        </div>
    )
}
