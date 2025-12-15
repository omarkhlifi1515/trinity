'use client';

import { db } from './config';
import {
    collection,
    doc,
    getDoc,
    setDoc,
    updateDoc,
    query,
    getDocs,
    where,
    Timestamp
} from 'firebase/firestore';

export type UserRole = 'admin' | 'chef' | 'employee';

export interface UserProfile {
    uid: string;
    email: string;
    role: UserRole;
    department?: string;
    displayName?: string;
    createdAt: Date;
    updatedAt?: Date;
}

/**
 * Create a new user profile in Firestore
 */
export async function createUserProfile(
    uid: string,
    email: string,
    displayName?: string
): Promise<UserProfile> {
    if (!db) throw new Error('Firestore not initialized');

    // Determine role based on email - simple logic for now
    const role: UserRole = email.toLowerCase() === 'admin@gmail.com' ? 'admin' : 'employee';

    const userProfile: UserProfile = {
        uid,
        email,
        role,
        displayName: displayName || email.split('@')[0],
        createdAt: new Date(),
    };

    const userRef = doc(db, 'users', uid);
    await setDoc(userRef, {
        ...userProfile,
        createdAt: Timestamp.fromDate(userProfile.createdAt),
    });

    return userProfile;
}

/**
 * Get user profile from Firestore
 */
export async function getUserProfile(uid: string): Promise<UserProfile | null> {
    if (!db) throw new Error('Firestore not initialized');

    const userRef = doc(db, 'users', uid);
    const userSnap = await getDoc(userRef);

    if (!userSnap.exists()) {
        return null;
    }

    const data = userSnap.data();
    return {
        uid: data.uid,
        email: data.email,
        role: data.role,
        department: data.department,
        displayName: data.displayName,
        createdAt: data.createdAt?.toDate() || new Date(),
        updatedAt: data.updatedAt?.toDate(),
    };
}

/**
 * Update user role (admin only)
 */
export async function updateUserRole(
    uid: string,
    role: UserRole,
    department?: string
): Promise<boolean> {
    if (!db) throw new Error('Firestore not initialized');

    try {
        const userRef = doc(db, 'users', uid);
        await updateDoc(userRef, {
            role,
            department: department || null,
            updatedAt: Timestamp.now(),
        });
        return true;
    } catch (error) {
        console.error('Error updating user role:', error);
        return false;
    }
}

/**
 * Get all users (admin only)
 */
export async function getAllUsers(): Promise<UserProfile[]> {
    if (!db) throw new Error('Firestore not initialized');

    const usersRef = collection(db, 'users');
    const querySnapshot = await getDocs(usersRef);

    return querySnapshot.docs.map(doc => {
        const data = doc.data();
        return {
            uid: data.uid,
            email: data.email,
            role: data.role,
            department: data.department,
            displayName: data.displayName,
            createdAt: data.createdAt?.toDate() || new Date(),
            updatedAt: data.updatedAt?.toDate(),
        };
    });
}

/**
 * Get users by role
 */
export async function getUsersByRole(role: UserRole): Promise<UserProfile[]> {
    if (!db) throw new Error('Firestore not initialized');

    const usersRef = collection(db, 'users');
    const q = query(usersRef, where('role', '==', role));
    const querySnapshot = await getDocs(q);

    return querySnapshot.docs.map(doc => {
        const data = doc.data();
        return {
            uid: data.uid,
            email: data.email,
            role: data.role,
            department: data.department,
            displayName: data.displayName,
            createdAt: data.createdAt?.toDate() || new Date(),
            updatedAt: data.updatedAt?.toDate(),
        };
    });
}

/**
 * Get users by department
 */
export async function getUsersByDepartment(department: string): Promise<UserProfile[]> {
    if (!db) throw new Error('Firestore not initialized');

    const usersRef = collection(db, 'users');
    const q = query(usersRef, where('department', '==', department));
    const querySnapshot = await getDocs(q);

    return querySnapshot.docs.map(doc => {
        const data = doc.data();
        return {
            uid: data.uid,
            email: data.email,
            role: data.role,
            department: data.department,
            displayName: data.displayName,
            createdAt: data.createdAt?.toDate() || new Date(),
            updatedAt: data.updatedAt?.toDate(),
        };
    });
}

/**
 * Check if user exists in Firestore
 */
export async function userProfileExists(uid: string): Promise<boolean> {
    if (!db) throw new Error('Firestore not initialized');

    const userRef = doc(db, 'users', uid);
    const userSnap = await getDoc(userRef);
    return userSnap.exists();
}
