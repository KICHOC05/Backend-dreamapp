/**
 * Firebase Function to register sleep data from wearable devices
 * 
 * Endpoint: POST /registerUserSleepData
 * Body: Complete sleep session data including all measurements
 * Collection: user_data_sleep
 * 
 * @param req - HTTP request containing sleep data
 * @param res - HTTP response
 */

import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { getFirestore } from "firebase-admin/firestore";
import { z } from "zod";
import { sleepDataUserSchema, SleepDataUser, SleepPhaseData } from "./sleepDataUserSchema";
import { requireAuth, functionsInternalKey } from "../../security/auth";

const db = getFirestore();

export const registerUserSleepData = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
    // Only allow POST requests
    if (req.method !== "POST") {
        res.status(405).json({ 
            error: "Method not allowed", 
            message: "Only POST requests are accepted" 
        });
        return;
    }

    try {
        // Log incoming request
        logger.info("Received sleep data registration request", {
            userId: req.body?.uidUser,
            date: req.body?.date,
            dataLength: req.body?.sleepPhaseData?.length
        });

        // Validate request body against schema
        const validatedData: SleepDataUser = sleepDataUserSchema.parse(req.body);
        if (!(await requireAuth(req, res, validatedData.uidUser))) return;

        // Create document ID using userId, date and startTime for easy querying
        const startTimeStamp = new Date(validatedData.startTime).getTime();
        const documentId = `${validatedData.uidUser}_${validatedData.date}_${startTimeStamp}`;

        // Check if document already exists
        const existingDoc = await db.collection('user_data_sleep').doc(documentId).get();
        if (existingDoc.exists) {
            logger.warn("Sleep session already exists", { documentId });
            res.status(409).json({
                error: "Conflict",
                message: "Sleep session already exists",
                date: validatedData.date,
                startTime: validatedData.startTime
            });
            return;
        }

        // Calculate additional metadata
        const metadata = {
            ...validatedData,
            // Override createdAt with server timestamp for accuracy
            createdAt: Date.now(), // Unix timestamp in milliseconds
            updatedAt: Date.now(),
            totalMeasurements: validatedData.sleepPhaseData.length,
            dataQuality: calculateDataQuality(validatedData.sleepPhaseData)
        };

        // Save to Firestore
        await db.collection('user_data_sleep').doc(documentId).set(metadata);

        // Log successful save
        logger.info("Sleep data saved successfully", {
            documentId,
            userId: validatedData.uidUser,
            date: validatedData.date,
            startTime: validatedData.startTime,
            totalDuration: validatedData.totalDuration,
            measurements: validatedData.sleepPhaseData.length
        });

        // Return success response
        res.status(201).json({
            success: true,
            message: "Sleep data registered successfully",
            data: {
                documentId,
                date: validatedData.date,
                startTime: validatedData.startTime,
                userId: validatedData.uidUser,
                totalMeasurements: validatedData.sleepPhaseData.length,
                sleepDuration: validatedData.sleepDuration,
                sleepEfficiency: validatedData.sleepEfficiency,
                quality: validatedData.quality
            }
        });

    } catch (error) {
        // Handle validation errors
        if (error instanceof z.ZodError) {
            logger.warn("Validation error in sleep data", {errors: error.errors});
            
            res.status(400).json({
                error: "Validation error",
                message: "Invalid sleep data format",
                details: error.errors.map(err => ({
                    field: err.path.join('.'),
                    message: err.message,
                    code: err.code
                }))
            });
            return;
        }

        // Handle Firestore errors
        if (error instanceof Error && error.message.includes('permission')) {
            logger.error("Permission error saving sleep data", error);
            res.status(403).json({
                error: "Permission denied",
                message: "Insufficient permissions to save sleep data"
            });
            return;
        }

        // Handle other errors
        logger.error("Unexpected error saving sleep data", error);
        res.status(500).json({
            error: "Internal server error",
            message: "Failed to save sleep data"
        });
    }
});

/**
 * Calculate data quality score based on measurement consistency
 * 
 * @param sleepPhaseData - Array of sleep phase measurements
 * @returns Quality score between 0-100
 */
function calculateDataQuality(sleepPhaseData: SleepPhaseData[]): number {
    if (sleepPhaseData.length === 0) return 0;

    let validMeasurements = 0;
    let totalMeasurements = sleepPhaseData.length;

    for (const measurement of sleepPhaseData) {
        // Check if measurement has valid physiological data
        if (measurement.hr_bpm > 0 && 
            measurement.hrv_rmssd >= 0 && 
            measurement.hrv_sdnn >= 0 &&
            measurement.phase !== null) {
            validMeasurements++;
        }
    }

    return Math.round((validMeasurements / totalMeasurements) * 100);
}
