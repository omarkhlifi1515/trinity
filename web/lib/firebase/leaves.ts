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

export type LeaveType = 'sick' | 'vacation' | 'personal' | 'urgent';
export type LeaveStatus = 'pending' | 'approved' | 'rejected';

export interface Leave {
    id: string;
    employeeId: string; // User UID
    type: LeaveType;
    startDate: Date;
    endDate: Date;
    reason: string;
    status: LeaveStatus;
    approvedBy?: string; // User UID
    rejectionReason?: string;
    createdAt: Date;
    updatedAt?: Date;
}

// Collection reference
const leavesCollection = 'leaves';

/**
 * Create a new leave request
 */
export async function createLeaveRequest(leaveData: Omit<Leave, 'id' | 'status' | 'createdAt' | 'updatedAt'>): Promise<Leave> {
    if (!db) throw new Error('Firestore not initialized');

    const newLeave = {
        ...leaveData,
        status: 'pending',
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        startDate: Timestamp.fromDate(leaveData.startDate),
        endDate: Timestamp.fromDate(leaveData.endDate),
    };

    const docRef = await addDoc(collection(db, leavesCollection), newLeave);

    return {
        id: docRef.id,
        ...leaveData,
        status: 'pending',
        createdAt: newLeave.createdAt.toDate(),
        updatedAt: newLeave.updatedAt.toDate(),
        startDate: leaveData.startDate,
        endDate: leaveData.endDate,
    };
}

/**
 * Get all leave requests (Admin/Chef)
 */
export async function getAllLeaves(): Promise<Leave[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(collection(db, leavesCollection), orderBy('createdAt', 'desc'));
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            startDate: data.startDate?.toDate(),
            endDate: data.endDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Leave;
    });
}

/**
 * Get user's leave requests
 */
export async function getUserLeaves(userId: string): Promise<Leave[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(
        collection(db, leavesCollection),
        where('employeeId', '==', userId),
        orderBy('createdAt', 'desc')
    );
    const snapshot = await getDocs(q);

    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            startDate: data.startDate?.toDate(),
            endDate: data.endDate?.toDate(),
            createdAt: data.createdAt?.toDate(),
            updatedAt: data.updatedAt?.toDate(),
        } as Leave;
    });
}

/**
 * Update leave status (Approve/Reject)
 */
export async function updateLeaveStatus(
    leaveId: string,
    status: LeaveStatus,
    approverId: string,
    rejectionReason?: string
): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');

    const leaveRef = doc(db, leavesCollection, leaveId);

    const updates: any = {
        status,
        updatedAt: Timestamp.now(),
        approvedBy: approverId
    };

    if (rejectionReason) {
        updates.rejectionReason = rejectionReason;
    }

    await updateDoc(leaveRef, updates);
}

/**
 * Get pending leaves count (for dashboard)
 */
export async function getPendingLeavesCount(): Promise<number> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(collection(db, leavesCollection), where('status', '==', 'pending'));
    const snapshot = await getDocs(q);

    return snapshot.size;
}
