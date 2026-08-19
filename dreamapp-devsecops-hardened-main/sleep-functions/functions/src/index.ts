/**
 * @file index.ts
 * @description Main entry point for Firebase Cloud Functions
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 * 
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// import {onRequest} from "firebase-functions/v2/https";
// import * as logger from "firebase-functions/logger";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

import { initializeApp } from "firebase-admin/app";
initializeApp();

// User management functions
export { registerUser, getUserByUid, deleteUser, updateUser, searchUser } from "./user/user";

// Sleep data management functions - Mobile app to cloud computing
export { registerUserSleepData } from "./sleep/dailyStats/registerDataSleepUser";

// Sleep statistics functions - Scheduled data processing

// Weekly statistics
export { generateWeeklyStats } from "./sleep/weeklyStats/generateWeeklyStats";
export { generateWeeklyStatsForUser } from "./sleep/weeklyStats/generateWeeklyStatsForUser";
export { generateAllUsersWeeklyStats } from "./sleep/weeklyStats/generateAllUsersWeeklyStats";

// Monthly statistics
export { generateMonthlyStats } from "./sleep/monthlyStats/generateMonthlyStats";
export { generateMonthlyStatsForUser } from "./sleep/monthlyStats/generateMonthlyStatsForUser";
export { generateAllUsersMonthlyStats } from "./sleep/monthlyStats/generateAllUsersMonthlyStats";

// Get Data for Analytics
export { getAllSleepSummaryByUser } from "./dataSummary/sleepSummary";
export { getAllUsers } from "./dataSummary/userJson";
