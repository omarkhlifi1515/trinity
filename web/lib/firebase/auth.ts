'use client';

import {
    signInWithEmailAndPassword,
    createUserWithEmailAndPassword,
    signOut,
    onAuthStateChanged,
    User as FirebaseUser,
    UserCredential
} from 'firebase/auth';
import { auth } from './config';
import { createUserProfile, getUserProfile, userProfileExists, UserProfile } from './users';

export interface User {
    id: string;
    email: string;
    role?: string;
    department?: string;
    displayName?: string;
}

/**
 * Firebase Authentication Client for Web
 * Matches the mobile app's ApiClient interface
 */
export const FirebaseAuthClient = {
    /**
     * Sign in with email and password
     */
    async login(email: string, password: string): Promise<User> {
        if (!auth) throw new Error('Firebase auth not initialized');

        try {
            const userCredential: UserCredential = await signInWithEmailAndPassword(auth, email, password);
            const firebaseUser = userCredential.user;

            // Get or create user profile
            let profile = await getUserProfile(firebaseUser.uid);
            if (!profile) {
                // Create profile if it doesn't exist (for existing Firebase users)
                profile = await createUserProfile(firebaseUser.uid, firebaseUser.email || email);
            }

            return {
                id: firebaseUser.uid,
                email: firebaseUser.email || email,
                role: profile.role,
                department: profile.department,
                displayName: profile.displayName,
            };
        } catch (error: any) {
            throw new Error(`Login failed: ${error.message}`);
        }
    },

    /**
     * Sign up with email and password
     */
    async signup(email: string, password: string): Promise<User> {
        if (!auth) throw new Error('Firebase auth not initialized');

        try {
            const userCredential: UserCredential = await createUserWithEmailAndPassword(auth, email, password);
            const firebaseUser = userCredential.user;

            // Create user profile in Firestore
            const profile = await createUserProfile(firebaseUser.uid, firebaseUser.email || email);

            return {
                id: firebaseUser.uid,
                email: firebaseUser.email || email,
                role: profile.role,
                department: profile.department,
                displayName: profile.displayName,
            };
        } catch (error: any) {
            throw new Error(`Signup failed: ${error.message}`);
        }
    },

    /**
     * Sign out current user
     */
    async logout(): Promise<void> {
        if (!auth) return;

        try {
            await signOut(auth);
        } catch (error: any) {
            console.error('Logout error:', error.message);
        }
    },

    /**
     * Get current authenticated user with profile
     */
    async getCurrentUserWithProfile(): Promise<User | null> {
        if (!auth) return null;
        const firebaseUser = auth.currentUser;
        if (!firebaseUser) return null;

        const profile = await getUserProfile(firebaseUser.uid);
        if (!profile) return null;

        return {
            id: firebaseUser.uid,
            email: firebaseUser.email || '',
            role: profile.role,
            department: profile.department,
            displayName: profile.displayName,
        };
    },

    /**
     * Get current authenticated user
     */
    getCurrentUser(): FirebaseUser | null {
        if (!auth) return null;
        return auth.currentUser;
    },

    /**
     * Listen to auth state changes
     */
    onAuthStateChanged(callback: (user: FirebaseUser | null) => void) {
        if (!auth) {
            // Return a no-op unsubscribe function
            return () => { };
        }
        return onAuthStateChanged(auth, callback);
    }
};

export default FirebaseAuthClient;
