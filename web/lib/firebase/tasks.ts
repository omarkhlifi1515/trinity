import { db } from './config';
import {
    collection,
    doc,
    getDoc,
    getDocs,
    addDoc,
    updateDoc,
    deleteDoc,
    query,
    where,
    Timestamp,
    orderBy
} from 'firebase/firestore';

export type TaskStatus = 'pending' | 'in-progress' | 'completed';
export type TaskPriority = 'low' | 'medium' | 'high';

export interface Task {
    id: string;
    title: string;
    description: string;
    assignedTo: string; // User UID
    assignedBy: string; // User UID
    status: TaskStatus;
    priority: TaskPriority;
    dueDate: Date;
    createdAt: Date;
    updatedAt?: Date;
}

// Collection reference
const tasksCollection = 'tasks';

/**
 * Create a new task
 */
export async function createTask(taskData: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>): Promise<Task> {
    if (!db) throw new Error('Firestore not initialized');

    const newTask = {
        ...taskData,
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        dueDate: Timestamp.fromDate(taskData.dueDate),
    };

    const docRef = await addDoc(collection(db, tasksCollection), newTask);

    return {
        id: docRef.id,
        ...taskData,
        createdAt: newTask.createdAt.toDate(),
        updatedAt: newTask.updatedAt.toDate(),
        dueDate: taskData.dueDate,
    };
}

/**
 * Get all tasks (Admin only)
 */
export async function getAllTasks(): Promise<Task[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(collection(db, tasksCollection), orderBy('createdAt', 'desc'));
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            dueDate: data.dueDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Task;
    });
}

/**
 * Get tasks assigned to a specific user
 */
export async function getUserTasks(userId: string): Promise<Task[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(
        collection(db, tasksCollection),
        where('assignedTo', '==', userId),
        orderBy('createdAt', 'desc')
    );
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            dueDate: data.dueDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Task;
    });
}

/**
 * Get tasks created by a specific user (Manager/Admin)
 */
export async function getCreatedTasks(userId: string): Promise<Task[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(
        collection(db, tasksCollection),
        where('assignedBy', '==', userId),
        orderBy('createdAt', 'desc')
    );
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            dueDate: data.dueDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Task;
    });
}

/**
 * Update a task
 */
export async function updateTask(taskId: string, updates: Partial<Task>): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');

    const taskRef = doc(db, tasksCollection, taskId);

    // Convert dates to Timestamps if present
    const firestoreUpdates: any = { ...updates, updatedAt: Timestamp.now() };
    if (updates.dueDate) {
        firestoreUpdates.dueDate = Timestamp.fromDate(updates.dueDate);
    }
    // Remove id from updates if present
    delete firestoreUpdates.id;
    delete firestoreUpdates.createdAt;

    await updateDoc(taskRef, firestoreUpdates);
}

/**
 * Delete a task
 */
export async function deleteTask(taskId: string): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');
    await deleteDoc(doc(db, tasksCollection, taskId));
}

/**
 * Get a single task
 */
export async function getTask(taskId: string): Promise<Task | null> {
    if (!db) throw new Error('Firestore not initialized');

    const docRef = doc(db, tasksCollection, taskId);
    const docSnap = await getDoc(docRef);

    if (docSnap.exists()) {
        const data = docSnap.data();
        return {
            id: docSnap.id,
            ...data,
            dueDate: data.dueDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Task;
    } else {
        return null;
    }
}
