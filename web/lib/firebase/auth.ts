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

export interface User {
    id: string;
    email: string;
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

            return {
                id: firebaseUser.uid,
                email: firebaseUser.email || email
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

            return {
                id: firebaseUser.uid,
                email: firebaseUser.email || email
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
