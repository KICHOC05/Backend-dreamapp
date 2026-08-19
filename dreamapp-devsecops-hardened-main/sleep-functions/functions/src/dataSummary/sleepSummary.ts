/**
 * @file sleepSummary.ts
 * @description
 * Firebase Cloud Function to retrieve all sleep summary records for a user from the 'user_data_sleep' collection.
 * Returns an array of daily sleep metrics ordered by date for analytics and visualization.
 *
 * @endpoint GET /getAllSleepSummaryByUser?uid={firebase_uid}
 * @query {string} uid - Firebase Authentication UID (required)
 *
 * @returns {Object} 200 - { success: true, data: Array<SleepSummary> }
 * @returns {Object} 400 - { error: "Missing uid parameter" }
 * @returns {Object} 500 - { success: false, error: "Internal server error" }
 *
 * @example
 * // Response:
 * {
 *   "success": true,
 *   "data": [
 *     {
 *       "date": "2024-07-01",
 *       "quality": 85,
 *       "sleepEfficiency": 92,
 *       "sleepDuration": 420,
 *       "light": 200,
 *       "deep": 120,
 *       "rem": 80,
 *       "awake": 20,
 *       "avgHR": 60,
 *       "avgHRV": 45,
 *       "awakenings": 2
 *     },
 *     ...
 *   ]
 * }
 *
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 */
import { onRequest } from "firebase-functions/v2/https";
import { getFirestore } from "firebase-admin/firestore";
import { requireAuth, functionsInternalKey } from "../security/auth";

const db = getFirestore();

/**
 * Get all sleep summary records for a user
 *
 * @description
 * Retrieves all documents from 'user_data_sleep' where uidUser matches the given UID, ordered by date.
 * Returns an array of daily sleep metrics for analytics and visualization.
 *
 * @endpoint GET /getAllSleepSummaryByUser?uid={firebase_uid}
 * @query {string} uid - Firebase Authentication UID (required)
 * @returns {Object} 200 - { success: true, data: Array<SleepSummary> }
 * @returns {Object} 400 - { error: "Missing uid parameter" }
 * @returns {Object} 500 - { success: false, error: "Internal server error" }
 */
export const getAllSleepSummaryByUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
  const uid = req.query.uid as string;

  if (!uid) {
    res.status(400).json({ error: "Missing uid parameter" });
    return;
  }
  if (!(await requireAuth(req, res, uid))) return;

  try {
    const snapshot = await db.collection("user_data_sleep")
      .where("uidUser", "==", uid)
      .orderBy("date")
      .get();

    if (snapshot.empty) {
      res.status(200).json({ success: true, data: [] });
      return;
    }

    const summary = snapshot.docs.map(doc => {
      const d = doc.data();
      return {
        date: d.date,
        quality: d.quality,
        sleepEfficiency: d.sleepEfficiency,
        sleepDuration: d.sleepDuration,
        light: d.lightSleepMinutes,
        deep: d.deepSleepMinutes,
        rem: d.remSleepMinutes,
        awake: d.awakeDuration,
        avgHR: d.avgHeartRate,
        avgHRV: d.avgRmssd,
        awakenings: d.awakeningsCount
      };
    });

    res.status(200).json({ success: true, data: summary });

  } catch (err) {
    console.error("Error fetching sleep summary:", err);
    res.status(500).json({ success: false, error: "Internal server error" });
  }
});
