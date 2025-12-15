import { db } from './config';
import {
    collection,
    addDoc,
    query,
    where,
    orderBy,
    getDocs,
    Timestamp,
    doc,
    setDoc
} from 'firebase/firestore';

export interface AttendanceRecord {
    id: string;
    employeeId: string;
    employeeName: string;
    date: string; // YYYY-MM-DD
    checkIn: string; // HH:MM
    checkOut?: string; // HH:MM
    status: 'present' | 'absent' | 'late';
    createdAt: Date;
}

const attendanceCollection = 'attendance';

/**
 * Get attendance for a specific date
 */
export async function getAttendanceByDate(date: string): Promise<AttendanceRecord[]> {
    if (!db) throw new Error('Firestore not initialized');

    const q = query(
        collection(db, attendanceCollection),
        where('date', '==', date)
    );

    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => {
        const data = doc.data();
        return {
            id: doc.id,
            ...data,
            createdAt: data.createdAt?.toDate(),
        } as AttendanceRecord;
    });
}

/**
 * Check In
 */
export async function checkIn(userId: string, userName: string): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');

    const now = new Date();
    const dateStr = now.toISOString().split('T')[0];
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // Check if already checked in
    const q = query(
        collection(db, attendanceCollection),
        where('employeeId', '==', userId),
        where('date', '==', dateStr)
    );
    const existing = await getDocs(q);

    if (!existing.empty) {
        throw new Error('Already checked in today');
    }

    await addDoc(collection(db, attendanceCollection), {
        employeeId: userId,
        employeeName: userName,
        date: dateStr,
        checkIn: timeStr,
        status: 'present', // Logic for 'late' can be added here
        createdAt: Timestamp.now()
    });
}

/**
 * Check Out - Note: In a real app we'd update the existing document found by date + userId
 * For simplicity, we assume the UI handles finding the record. 
 * But let's look it up securely.
 */
export async function checkOut(userId: string): Promise<void> {
    if (!db) throw new Error('Firestore not initialized');

    const now = new Date();
    const dateStr = now.toISOString().split('T')[0];
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const q = query(
        collection(db, attendanceCollection),
        where('employeeId', '==', userId),
        where('date', '==', dateStr)
    );
    const snapshot = await getDocs(q);

    if (snapshot.empty) {
        throw new Error('No check-in record found for today');
    }

    const docRef = snapshot.docs[0].ref;
    await setDoc(docRef, { checkOut: timeStr }, { merge: true });
}
