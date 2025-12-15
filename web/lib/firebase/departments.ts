import { db } from './config';
import {
    collection,
    addDoc,
    query,
    getDocs,
    where,
    Timestamp,
} from 'firebase/firestore';

export interface Department {
    id: string;
    name: string;
    description: string;
    managerId?: string; // UID of the Chef/Manager
    createdAt: Date;
}

const departmentsCollection = 'departments';

/**
 * Get all departments
 */
export async function getAllDepartments(): Promise<Department[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(collection(db, departmentsCollection));
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            createdAt: data.createdAt?.toDate(),
        } as Department;
    });
}

/**
 * Create a new department
 */
export async function createDepartment(name: string, description: string): Promise<Department> {
    if (!db) throw new Error('Firestore not initialized');

    const newDept = {
        name,
        description,
        createdAt: Timestamp.now(),
    };

    const docRef = await addDoc(collection(db, departmentsCollection), newDept);

    return {
        id: docRef.id,
        ...newDept,
        createdAt: newDept.createdAt.toDate(),
    };
}
