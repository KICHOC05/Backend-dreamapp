/**
 * @file userJson.ts
 * @description
 * Firebase Cloud Function to retrieve all users from the Firestore 'users' collection, including Google account profile data.
 * For each user, combines Firestore profile with Firebase Authentication data (username and profile picture).
 *
 * @endpoint GET /getAllUsers
 * @returns {Object[]} 200 - Array of user objects with Firestore and Auth data
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 *
 * @example
 * // Response:
 * [
 *   {
 *     "uidUser": "firebase_uid_123",
 *     "weightKg": 70.5,
 *     "heightCm": 175,
 *     "age": 28,
 *     "sex": "M",
 *     "username": "John Doe",
 *     "profilePictureUrl": "https://..."
 *   },
 *   ...
 * ]
 *
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 */
import { getFirestore } from 'firebase-admin/firestore';
import { getAuth } from 'firebase-admin/auth';
import { onRequest } from 'firebase-functions/https';
import { requireAuth, functionsInternalKey } from '../security/auth';

const db = getFirestore();

/**
 * Get all users with Google account data
 *
 * @description
 * Retrieves all user documents from Firestore 'users' collection and enriches each with username and profilePictureUrl from Firebase Authentication.
 *
 * @endpoint GET /getAllUsers
 * @returns {Object[]} 200 - Array of user objects with Firestore and Auth data
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 */
export const getAllUsers = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  if (req.method !== "GET") {
    res.status(405).json({
      error: "Method not allowed",
      message: "Only GET requests are accepted"
    });
    return;
  }
  if (!(await requireAuth(req, res, undefined, true))) return;

  try {
    const snapshot = await db.collection("users").get();
    const users = await Promise.all(
      snapshot.docs.map(async doc => {
        const userData = doc.data();
        let username = null;
        let profilePictureUrl = null;
        if (userData.uidUser) {
          try {
            const authUser = await getAuth().getUser(userData.uidUser);
            username = authUser.displayName || null;
            profilePictureUrl = authUser.photoURL || null;
          } catch (authErr) {
            console.warn(`[getAllUsers] No se encontró usuario en Auth para uidUser: ${userData.uidUser}`);
          }
        }
        const result = {
          ...userData,
          username,
          profilePictureUrl
        };
        return result;
      })
    );
    res.status(200).json(users);
  } catch (error) {
    res.status(500).json({
      error: "Internal Server Error",
      message: "Failed to fetch users"
    });
  }
});
