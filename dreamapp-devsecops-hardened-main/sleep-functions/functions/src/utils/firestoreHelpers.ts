/**
 * @file firestoreHelpers.ts
 * @description Utility functions for Firestore database operations
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 */

import { getFirestore } from "firebase-admin/firestore";

const db = getFirestore();

/**
 * Find a user document by Firebase UID
 * 
 * @description Searches for a user document in the 'users' collection using the Firebase UID.
 * Returns the first matching document or null if no user is found.
 * 
 * @param uidUser - Firebase Authentication UID of the user
 * @returns Promise<DocumentSnapshot | null> - User document or null if not found
 * 
 * @example
 * ```typescript
 * const userDoc = await getUserDocByUid('firebase_uid_123');
 * if (userDoc) {
 *   console.log('User found:', userDoc.data());
 * } else {
 *   console.log('User not found');
 * }
 * ```
 */
export async function getUserDocByUid(uidUser: string) {
  const snapshot = await db.collection('users')
    .where('uidUser', '==', uidUser)
    .limit(1)
    .get();

  return snapshot.empty ? null : snapshot.docs[0];
}
