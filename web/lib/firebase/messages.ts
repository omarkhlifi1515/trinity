import { db } from './config';
import {
    collection,
    addDoc,
    query,
    where,
    orderBy,
    onSnapshot,
    Timestamp,
    updateDoc,
    doc
} from 'firebase/firestore';

export interface Message {
    id: string;
    senderId: string;
    senderEmail: string;
    senderName?: string;
    receiverId: string; // 'all' or specific user UID
    receiverEmail?: string;
    subject: string;
    content: string;
    read: boolean;
    createdAt: Date;
}

const messagesCollection = 'messages';

/**
 * Send a new message
 */
export async function sendMessage(messageData: Omit<Message, 'id' | 'createdAt' | 'read'>): Promise<Message> {
    if (!db) throw new Error('Firestore not initialized');

    const newMessage = {
        ...messageData,
        read: false,
        createdAt: Timestamp.now(),
    };

    const docRef = await addDoc(collection(db, messagesCollection), newMessage);

    return {
        id: docRef.id,
        ...messageData,
        read: false,
        createdAt: newMessage.createdAt.toDate(),
    };
}

/**
 * Subscribe to messages for a user (Real-time)
 */
export function subscribeToMessages(userId: string, callback: (messages: Message[]) => void): () => void {
    if (!db) return () => { };

    // Query for messages where receiver is 'all' OR specific user
    // Using 'in' operator to retrieve both sets safely in compliance with security rules.
    const q = query(
        collection(db, messagesCollection),
        where('receiverId', 'in', [userId, 'all']),
        orderBy('createdAt', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
        const msgs: Message[] = [];
        snapshot.forEach((doc) => {
            const data = doc.data();
            msgs.push({
                id: doc.id,
                ...data,
                createdAt: data.createdAt?.toDate(),
            } as Message);
        });
        callback(msgs);
    }, (error) => {
        console.error("Error subscribing to messages:", error);
        if (error.code === 'failed-precondition') {
            console.warn("Index missing for messages query: receiverId + createdAt DESC");
        }
    });

    return unsubscribe;
}

/**
 * Mark message as read
 */
export async function markMessageAsRead(messageId: string): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');

    const msgRef = doc(db, messagesCollection, messageId);
    await updateDoc(msgRef, {
        read: true
    });
}
