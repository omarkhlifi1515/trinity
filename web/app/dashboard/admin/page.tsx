'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Shield, Users, ChevronDown } from 'lucide-react';
import { FirebaseAuthClient } from '@/lib/firebase/auth';
import { getAllUsers, updateUserRole, UserProfile, UserRole } from '@/lib/firebase/users';
import { getUserProfile } from '@/lib/firebase/users';
import { isAdmin } from '@/lib/auth/roles';

export default function AdminPage() {
    const [currentUser, setCurrentUser] = useState<any>(null);
    const [users, setUsers] = useState<UserProfile[]>([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState<string | null>(null);
    const router = useRouter();

    useEffect(() => {
        loadCurrentUser();
    }, []);

    const loadCurrentUser = async () => {
        const firebaseUser = FirebaseAuthClient.getCurrentUser();
        if (!firebaseUser) {
            router.push('/');
            return;
        }

        const profile = await getUserProfile(firebaseUser.uid);
        if (!profile || !isAdmin({ id: firebaseUser.uid, email: firebaseUser.email!, role: profile.role })) {
            router.push('/dashboard');
            return;
        }

        setCurrentUser({ id: firebaseUser.uid, email: firebaseUser.email!, role: profile.role });
        loadUsers();
    };

    const loadUsers = async () => {
        try {
            setLoading(true);
            const allUsers = await getAllUsers();
            setUsers(allUsers);
        } catch (error) {
            console.error('Error loading users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = async (uid: string, newRole: UserRole, department?: string) => {
        try {
            setUpdating(uid);
            const success = await updateUserRole(uid, newRole, department);
            if (success) {
                // Reload users
                await loadUsers();
            } else {
                alert('Failed to update user role');
            }
        } catch (error) {
            console.error('Error updating role:', error);
            alert('Error updating user role');
        } finally {
            setUpdating(null);
        }
    };

    const getRoleBadgeColor = (role: UserRole) => {
        switch (role) {
            case 'admin':
                return 'bg-red-100 text-red-800';
            case 'chef':
                return 'bg-blue-100 text-blue-800';
            case 'employee':
                return 'bg-gray-100 text-gray-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading admin panel...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="p-8">
            <div className="mb-8">
                <div className="flex items-center gap-3 mb-2">
                    <Shield className="w-8 h-8 text-red-600" />
                    <h1 className="text-3xl font-bold text-gray-900">Admin Panel</h1>
                </div>
                <p className="text-gray-600">Manage user roles and permissions</p>
            </div>

            <div className="bg-white rounded-lg shadow overflow-hidden">
                <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                    <div className="flex items-center gap-2">
                        <Users className="w-5 h-5 text-gray-600" />
                        <h2 className="text-lg font-semibold text-gray-900">User Management</h2>
                    </div>
                </div>

                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    User
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Email
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Current Role
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Department
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {users.map((user) => (
                                <tr key={user.uid} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center">
                                            <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center mr-3">
                                                <span className="text-blue-600 font-semibold">
                                                    {user.displayName?.charAt(0).toUpperCase() || user.email.charAt(0).toUpperCase()}
                                                </span>
                                            </div>
                                            <div className="text-sm font-medium text-gray-900">
                                                {user.displayName || 'User'}
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {user.email}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 text-xs font-medium rounded ${getRoleBadgeColor(user.role)}`}>
                                            {user.role.charAt(0).toUpperCase() + user.role.slice(1)}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {user.department || 'N/A'}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        {user.uid === currentUser?.id ? (
                                            <span className="text-gray-400">Current User</span>
                                        ) : (
                                            <div className="flex gap-2">
                                                <select
                                                    value={user.role}
                                                    onChange={(e) => handleRoleChange(user.uid, e.target.value as UserRole, user.department)}
                                                    disabled={updating === user.uid}
                                                    className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                                                >
                                                    <option value="employee">Employee</option>
                                                    <option value="chef">Manager</option>
                                                    <option value="admin">Admin</option>
                                                </select>
                                                {updating === user.uid && (
                                                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
                                                )}
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h3 className="text-sm font-semibold text-blue-900 mb-2">Role Permissions</h3>
                <ul className="text-sm text-blue-800 space-y-1">
                    <li><strong>Admin:</strong> Full access to all features, can assign roles</li>
                    <li><strong>Manager (Chef):</strong> Can approve leaves, add tasks, manage team</li>
                    <li><strong>Employee:</strong> Can request leaves, view tasks, basic access</li>
                </ul>
            </div>
        </div>
    );
}
