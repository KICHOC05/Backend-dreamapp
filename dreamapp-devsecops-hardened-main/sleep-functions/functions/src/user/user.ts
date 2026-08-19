/**
 * @file user.ts
 * @description
 * Firebase Cloud Functions for user management in the IoT Sleep Monitoring application.
 * Provides endpoints for:
 * - Register a new user with profile data
 * - Get user by Firebase UID
 * - Delete user by Firebase UID  
 * - Update user profile information
 * - Search/verify user existence for Google account integration
 * 
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 */

import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { getFirestore } from "firebase-admin/firestore";
import { registerUserSchema, updateUserSchema } from "./userSchema";
import { getUserDocByUid } from "../utils/firestoreHelpers";
import { requireAuth, functionsInternalKey } from "../security/auth";

const db = getFirestore();

/**
 * Register a new user in the system
 * 
 * @description Creates a new user document in Firestore with profile information.
 * Validates input data against schema and prevents duplicate registrations.
 * 
 * @endpoint POST /registerUser
 * @body {Object} userData - User registration data
 * @body {string} userData.uidUser - Firebase Authentication UID
 * @body {number} userData.weightKg - User weight in kilograms
 * @body {number} userData.heightCm - User height in centimeters  
 * @body {number} userData.age - User age in years
 * @body {string} userData.sex - User sex ('M' or 'F')
 * 
 * @returns {Object} 201 - User registered successfully
 * @returns {Object} 400 - Validation error or user already exists
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 * 
 * @example
 * // Request body:
 * {
 *   "uidUser": "firebase_uid_123",
 *   "weightKg": 70.5,
 *   "heightCm": 175,
 *   "age": 28,
 *   "sex": "M"
 * }
 */
export const registerUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  if (req.method !== "POST") {
    res.status(405).send("Method not allowed");
    return;
  }

  const parseResult = registerUserSchema.safeParse(req.body);
  if (!parseResult.success) {
    const errorMsg = parseResult.error.errors
      .map(e => `${e.path.join('.')}: ${e.message}`)
      .join('; ');
    res.status(400).send(`Validation error: ${errorMsg}`);
    return;
  }

  const { uidUser, weightKg, heightCm, age, sex } = parseResult.data;
  if (!(await requireAuth(req, res, uidUser))) return;

  try {
    const userDoc = await getUserDocByUid(uidUser);

    if (userDoc) {
      res.status(400).send("User with this UID already exists");
      return;
    }

    await db.collection("users").add({
      uidUser,
      weightKg,
      heightCm,
      age,
      sex
    });

    res.status(201).send("User registered successfully");

  } catch (error) {
    logger.error("Error registering user", error);
    res.status(500).send("Internal server error");
  }
});

/**
 * Get user by Firebase UID
 * 
 * @description Retrieves user profile information from Firestore using Firebase UID.
 * Returns complete user document with profile data.
 * 
 * @endpoint GET /getUserByUid?uidUser={firebase_uid}
 * @query {string} uidUser - Firebase Authentication UID (required)
 * 
 * @returns {Object} 200 - User data retrieved successfully
 * @returns {Object} 200.id - Document ID in Firestore
 * @returns {Object} 200.data - User profile data
 * @returns {Object} 400 - Missing uidUser parameter
 * @returns {Object} 404 - User not found
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 * 
 * @example
 * // Response:
 * {
 *   "id": "firestore_doc_id",
 *   "data": {
 *     "uidUser": "firebase_uid_123",
 *     "weightKg": 70.5,
 *     "heightCm": 175,
 *     "age": 28,
 *     "sex": "M"
 *   }
 * }
 */
export const getUserByUid = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  if (req.method !== "GET") {
    res.status(405).send("Method not allowed");
    return;
  }

  const uidUser = req.query.uidUser as string | undefined;

  if (!uidUser) {
    res.status(400).send("Parameter 'uidUser' is required");
    return;
  }
  if (!(await requireAuth(req, res, uidUser))) return;

  try {
    const userDoc = await getUserDocByUid(uidUser);
    if (!userDoc) {
      res.status(404).send("User not found");
      return;
    }

    res.status(200).json({
      id: userDoc.id,
      data: userDoc.data()
    });

  } catch (error) {
    logger.error("Error searching for user", error);
    res.status(500).send("Internal server error");
  }
});

/**
 * Verify if user exists in the system
 * 
 * @description Checks if a user has completed the registration process.
 * Used for Google account integration to determine if profile setup is required.
 * 
 * @endpoint GET /searchUser?uidUser={firebase_uid}
 * @query {string} uidUser - Firebase Authentication UID (required)
 * 
 * @returns {Object} 200 - User registration status
 * @returns {string} 200.message - Status message
 * @returns {boolean} 200.status - Registration completion status
 * @returns {Object} 400 - Missing uidUser parameter
 * @returns {Object} 404 - User has not completed registration
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 * 
 * @example
 * // Response when user exists:
 * {
 *   "message": "User has completed the registration form",
 *   "status": true
 * }
 * 
 * // Response when user doesn't exist:
 * {
 *   "message": "User has not completed the registration form",
 *   "status": false
 * }
 */
export const searchUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  if (req.method !== "GET") {
    res.status(405).send("Method not allowed");
    return;
  }

  const uidUser = req.query.uidUser as string | undefined;

  if (!uidUser) {
    res.status(400).send("Parameter 'uidUser' is required");
    return;
  }
  if (!(await requireAuth(req, res, uidUser))) return;

  try {
    const userDoc = await getUserDocByUid(uidUser);
    if(!userDoc) {
      res.status(404).json({
        message: "User has not completed the registration form",
        status: false
      })
      return;
    }

    res.status(200).json({
      message: "User has completed the registration form",
      status: true
    })
    return;
  } catch(error) {
    logger.error("Error performing search", error);
    res.status(500).send("Internal server error");
  }
})

/**
 * Delete user account and associated data
 * 
 * @description Permanently removes user account from the system.
 * This endpoint deletes the user profile document from Firestore.
 * 
 * @endpoint DELETE /deleteUser?uidUser={firebase_uid}
 * @query {string} uidUser - Firebase Authentication UID (required)
 * 
 * @returns {string} 200 - User deleted successfully
 * @returns {Object} 400 - Missing uidUser parameter
 * @returns {Object} 404 - User not found
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 * 
 * @warning This operation is irreversible. All user data will be permanently deleted.
 * 
 * @todo Implement cascading deletion of all user-related documents (sleep data, etc.)
 * 
 * @example
 * // DELETE /deleteUser?uidUser=firebase_uid_123
 * // Response: "User deleted successfully"
 */
export const deleteUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  if (req.method !== 'DELETE') {
    res.status(405).send("Method not allowed");
    return;
  }

  const uidUser = req.query.uidUser as string | undefined;

  if(!uidUser) {
    res.status(400).send("Parameter 'uidUser' is required");
    return;
  }
  if (!(await requireAuth(req, res, uidUser))) return;

  try {
    const userDoc = await getUserDocByUid(uidUser);

    if (!userDoc) {
      res.status(404).send("User not found");
      return;
    }

    const docId = userDoc.id;
    // TODO: Delete all documents that contain this uidUser before deleting the user
    await db.collection('users').doc(docId).delete();
    res.status(200).send("User deleted successfully");

  } catch (error) {
    logger.error("Error deleting user", error);
    res.status(500).send("Internal server error");
  }

})

/**
 * Update user profile information
 * 
 * @description Updates existing user profile data in Firestore.
 * Validates input data against schema and ensures user exists before updating.
 * 
 * @endpoint PUT /updateUser?uidUser={firebase_uid}
 * @query {string} uidUser - Firebase Authentication UID (required)
 * @body {Object} userData - Updated user profile data
 * @body {number} [userData.weightKg] - User weight in kilograms
 * @body {number} [userData.heightCm] - User height in centimeters
 * @body {number} [userData.age] - User age in years
 * @body {string} [userData.sex] - User sex ('M' or 'F')
 * 
 * @returns {string} 200 - User updated successfully
 * @returns {Object} 400 - Validation error or missing uidUser parameter
 * @returns {Object} 404 - User not found
 * @returns {Object} 405 - Method not allowed
 * @returns {Object} 500 - Internal server error
 * 
 * @example
 * // Request body (partial update):
 * {
 *   "weightKg": 72.0,
 *   "age": 29
 * }
 * 
 * // Response: "User updated successfully"
 */
export const updateUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {

  if (req.method !== "PUT") {
    res.status(405).send("Method not allowed");
    return;
  }

  const uidUser = req.query.uidUser as string | undefined;

  if(!uidUser) {
    res.status(400).send("Parameter 'uidUser' is required");
    return;
  }
  if (!(await requireAuth(req, res, uidUser))) return;

  const parseResult = updateUserSchema.safeParse(req.body);
  if (!parseResult.success) {
    const errorMsg = parseResult.error.errors
      .map(e => `${e.path.join('.')}: ${e.message}`)
      .join('; ');
    res.status(400).send(`Validation error: ${errorMsg}`);
    return;
  }

  const { weightKg, heightCm, age, sex } = parseResult.data;

  try {

    const userDoc = await getUserDocByUid(uidUser);
    if (!userDoc) {
      res.status(404).send("User not found");
      return;
    }

    const docId = userDoc.id;
    await db.collection('users').doc(docId).update({
      weightKg: weightKg,
      heightCm: heightCm,
      age: age,
      sex: sex
    });
    res.status(200).send("User updated successfully");
  } catch(error) {
    logger.error("Error updating user", error);
    res.status(500).send("Internal server error");
  }
})
