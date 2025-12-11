import { initializeApp, getApps, getApp, FirebaseApp } from 'firebase/app';
import { getAuth, Auth } from 'firebase/auth';
import { getFirestore, Firestore } from 'firebase/firestore';

// Your web app's Firebase configuration
// Using the same Firebase project as the mobile app
const firebaseConfig = {
    apiKey: "AIzaSyBp_Pj4b-AnrbvSaZIIxaNLd394VX4EzjU",
    authDomain: "trinity-6d93d.firebaseapp.com",
    projectId: "trinity-6d93d",
    storageBucket: "trinity-6d93d.firebasestorage.app",
    messagingSenderId: "938399326576",
    appId: "1:938399326576:web:ced4e3a907735e8cd4afb5"
};

// Initialize Firebase only on client side
let app: FirebaseApp | undefined;
let auth: Auth | undefined;
let db: Firestore | undefined;

if (typeof window !== 'undefined') {
    // Only initialize on client side
    app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();
    auth = getAuth(app);
    db = getFirestore(app);
}

// Export with proper types
export { auth, db };
export default app;
