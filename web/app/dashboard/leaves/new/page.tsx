'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Calendar, ArrowLeft } from 'lucide-react'
import { createLeaveRequest } from '@/lib/firebase/leaves'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'

export default function NewLeavePage() {
    const router = useRouter()
    const [loading, setLoading] = useState(false)
    const [user, setUser] = useState<any>(null)
    const [formData, setFormData] = useState({
        type: 'vacation',
        startDate: '',
        endDate: '',
        reason: ''
    })

    useEffect(() => {
        loadUser();
    }, [])

    const loadUser = async () => {
        const u = FirebaseAuthClient.getCurrentUser();
        if (u) {
            const profile = await getUserProfile(u.uid);
            setUser({ ...u, ...profile });
        } else {
            FirebaseAuthClient.onAuthStateChanged(async (u) => {
                if (u) {
                    const profile = await getUserProfile(u.uid);
                    setUser({ ...u, ...profile });
                }
            })
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!user) return;

        setLoading(true)
        try {
            await createLeaveRequest({
                employeeId: user.uid,
                type: formData.type as any,
                startDate: new Date(formData.startDate),
                endDate: new Date(formData.endDate),
                reason: formData.reason
            })
            router.push('/dashboard/leaves')
        } catch (error) {
            console.error('Error requesting leave:', error)
            alert('Failed to request leave')
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
                Back to Leaves
            </button>

            <div className="bg-white rounded-lg shadow p-8">
                <div className="flex items-center gap-4 mb-6">
                    <div className="w-12 h-12 bg-pink-100 rounded-lg flex items-center justify-center">
                        <Calendar className="w-6 h-6 text-pink-600" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">Request Leave</h1>
                        <p className="text-gray-600">Submit a new leave request</p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Leave Type
                        </label>
                        <select
                            value={formData.type}
                            onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent outline-none"
                        >
                            <option value="vacation">Vacation</option>
                            <option value="sick">Sick Leave</option>
                            <option value="personal">Personal</option>
                            <option value="urgent">Urgent</option>
                        </select>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Start Date
                            </label>
                            <input
                                type="date"
                                required
                                value={formData.startDate}
                                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent outline-none"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                End Date
                            </label>
                            <input
                                type="date"
                                required
                                value={formData.endDate}
                                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent outline-none"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Reason
                        </label>
                        <textarea
                            required
                            rows={4}
                            value={formData.reason}
                            onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent outline-none resize-none"
                            placeholder="Please explain why you need to take leave..."
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-pink-600 text-white py-2 rounded-lg hover:bg-pink-700 transition-colors disabled:opacity-50"
                    >
                        {loading ? 'Submitting...' : 'Submit Request'}
                    </button>
                </form>
            </div>
        </div>
    )
}
