/**
 * Firebase Function to manually generate weekly sleep statistics for a specific user
 * 
 * Endpoint: POST /generateWeeklyStatsForUser
 * Body: { uidUser: string }
 * Collection: weekly_sleep_stats
 * 
 * @param req - HTTP request containing user ID
 * @param res - HTTP response
 */

import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { getFirestore } from "firebase-admin/firestore";
import { z } from "zod";
import { WeeklySleepStats, weeklySleepStatsSchema } from "./weeklySleepStatsSchema";
import { SleepDataUser } from "../dailyStats/sleepDataUserSchema";
import { requireAuth, functionsInternalKey } from "../../security/auth";

const db = getFirestore();

// Request schema validation
const requestSchema = z.object({
    uidUser: z.string().min(1, "User ID is required"),
    daysBack: z.number().min(1).max(30).optional().default(7) // Allow custom range, default 7 days
});

export const generateWeeklyStatsForUser = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
    // Only allow POST requests
    if (req.method !== "POST") {
        res.status(405).json({ 
            error: "Method not allowed", 
            message: "Only POST requests are accepted" 
        });
        return;
    }

    try {
        // Validate request body
        const { uidUser, daysBack } = requestSchema.parse(req.body);
        if (!(await requireAuth(req, res, uidUser))) return;
        
        logger.info("Generating weekly stats for user", { uidUser, daysBack });

        // Calculate date range
        const endDate = new Date();
        endDate.setUTCHours(23, 59, 59, 999); // End of today (include today)
        
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - (daysBack - 1)); // Include today in the count
        startDate.setUTCHours(0, 0, 0, 0); // Start of that day
        
        const weekStartDateStr = startDate.toISOString().split('T')[0];
        const weekEndDateStr = endDate.toISOString().split('T')[0];
        
        // Get user's sleep data for the specified period
        const sleepDataSnapshot = await db.collection('user_data_sleep')
            .where('uidUser', '==', uidUser)
            .where('date', '>=', weekStartDateStr)
            .where('date', '<=', weekEndDateStr) // Changed from < to <= to include today
            .orderBy('date', 'desc')
            .limit(daysBack)
            .get();
        
        if (sleepDataSnapshot.empty) {
            res.status(404).json({
                error: "No data found",
                message: `No sleep data found for user ${uidUser} in the last ${daysBack} days`,
                dateRange: {
                    startDate: weekStartDateStr,
                    endDate: weekEndDateStr
                }
            });
            return;
        }
        
        const sleepRecords: SleepDataUser[] = sleepDataSnapshot.docs.map(doc => doc.data() as SleepDataUser);
        const sourceNights = sleepDataSnapshot.docs.map(doc => doc.id);
        
        logger.info(`Found ${sleepRecords.length} sleep records for user ${uidUser}`);
        
        // Calculate weekly statistics
        const weeklyStats = calculateWeeklyStats(uidUser, sleepRecords, sourceNights, weekStartDateStr, weekEndDateStr);
        
        // Validate the calculated stats
        const validatedStats = weeklySleepStatsSchema.parse(weeklyStats);
        
        // Create document ID for the weekly stats
        const weeklyStatsId = `${uidUser}_${weekStartDateStr}_${weekEndDateStr}`;
        
        // Save to Firestore
        await db.collection('weekly_sleep_stats').doc(weeklyStatsId).set(validatedStats);
        
        logger.info(`Weekly stats generated successfully for user ${uidUser}`);
        
        // Return success response with the generated statistics
        res.status(200).json({
            success: true,
            message: "Weekly statistics generated successfully",
            data: {
                documentId: weeklyStatsId,
                userId: uidUser,
                dateRange: {
                    startDate: weekStartDateStr,
                    endDate: weekEndDateStr
                },
                totalNights: validatedStats.totalNights,
                avgSleepDuration: validatedStats.avgSleepDuration,
                avgSleepEfficiency: validatedStats.avgSleepEfficiency,
                dominantQuality: validatedStats.dominantQuality,
                consistencyScore: validatedStats.consistencyScore,
                sleepDurationTrend: validatedStats.sleepDurationTrend,
                sleepQualityTrend: validatedStats.sleepQualityTrend
            }
        });
        
    } catch (error) {
        // Handle validation errors
        if (error instanceof z.ZodError) {
            logger.error("Validation error in weekly stats generation", {
                errors: error.errors,
                requestBody: req.body
            });
            
            res.status(400).json({
                error: "Validation error",
                message: "Invalid request format",
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
            logger.error("Permission error generating weekly stats", error);
            res.status(403).json({
                error: "Permission denied",
                message: "Insufficient permissions to generate weekly statistics"
            });
            return;
        }
        
        // Handle other errors
        logger.error("Unexpected error generating weekly stats", error);
        res.status(500).json({
            error: "Internal server error",
            message: "Failed to generate weekly statistics"
        });
    }
});

/**
 * Calculate weekly statistics from sleep records
 * (Reused from generateWeeklyStats.ts)
 */
function calculateWeeklyStats(
    userId: string,
    sleepRecords: SleepDataUser[],
    sourceNights: string[],
    weekStartDate: string,
    weekEndDate: string
): WeeklySleepStats {
    const totalNights = sleepRecords.length;
    const now = Date.now();
    
    // Basic sleep duration statistics
    const sleepDurations = sleepRecords.map(record => record.sleepDuration);
    const avgSleepDuration = sleepDurations.reduce((a, b) => a + b, 0) / totalNights;
    const minSleepDuration = Math.min(...sleepDurations);
    const maxSleepDuration = Math.max(...sleepDurations);
    const totalSleepTime = sleepDurations.reduce((a, b) => a + b, 0);
    
    // Sleep efficiency
    const sleepEfficiencies = sleepRecords.map(record => record.sleepEfficiency);
    const avgSleepEfficiency = sleepEfficiencies.reduce((a, b) => a + b, 0) / totalNights;
    
    // Sleep phases statistics
    const avgLightSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.lightSleepMinutes, 0) / totalNights;
    const avgDeepSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.deepSleepMinutes, 0) / totalNights;
    const avgRemSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.remSleepMinutes, 0) / totalNights;
    const avgAwakeDuration = sleepRecords.reduce((sum, record) => sum + record.awakeDuration, 0) / totalNights;
    
    // Calculate sleep phase percentages
    const avgLightSleepPercentage = (avgLightSleepMinutes / avgSleepDuration) * 100;
    const avgDeepSleepPercentage = (avgDeepSleepMinutes / avgSleepDuration) * 100;
    const avgRemSleepPercentage = (avgRemSleepMinutes / avgSleepDuration) * 100;
    
    // Quality distribution
    const qualityDistribution = {
        excellent: sleepRecords.filter(r => r.quality === 'EXCELLENT').length,
        good: sleepRecords.filter(r => r.quality === 'GOOD').length,
        fair: sleepRecords.filter(r => r.quality === 'FAIR').length,
        poor: sleepRecords.filter(r => r.quality === 'POOR').length
    };
    
    // Find dominant quality
    const dominantQuality = Object.entries(qualityDistribution)
        .reduce((a, b) => qualityDistribution[a[0] as keyof typeof qualityDistribution] > qualityDistribution[b[0] as keyof typeof qualityDistribution] ? a : b)[0]
        .toUpperCase() as 'POOR' | 'FAIR' | 'GOOD' | 'EXCELLENT';
    
    // Heart rate statistics
    const heartRates = sleepRecords.map(record => record.avgHeartRate);
    const avgHeartRate = heartRates.reduce((a, b) => a + b, 0) / totalNights;
    const minHeartRate = Math.min(...sleepRecords.map(r => r.minHeartRate));
    const maxHeartRate = Math.max(...sleepRecords.map(r => r.maxHeartRate));
    const heartRateVariability = calculateStandardDeviation(heartRates);
    
    // HRV statistics
    const rmssdValues = sleepRecords.map(record => record.avgRmssd);
    const sdnnValues = sleepRecords.map(record => record.avgSdnn);
    const avgRmssd = rmssdValues.reduce((a, b) => a + b, 0) / totalNights;
    const avgSdnn = sdnnValues.reduce((a, b) => a + b, 0) / totalNights;
    const hrvTrend = calculateTrend(rmssdValues);
    
    // Sleep disruption
    const avgAwakeningsCount = sleepRecords.reduce((sum, record) => sum + record.awakeningsCount, 0) / totalNights;
    const avgMovement = sleepRecords.reduce((sum, record) => sum + record.avgMovement, 0) / totalNights;
    
    // Sleep timing patterns
    const bedtimes = sleepRecords.map(record => new Date(record.startTime));
    const wakeTimes = sleepRecords.map(record => new Date(record.endTime));
    const avgBedtime = calculateAverageTime(bedtimes);
    const avgWakeTime = calculateAverageTime(wakeTimes);
    const bedtimeConsistency = calculateTimeConsistency(bedtimes);
    
    // Consistency score (based on sleep duration and bedtime consistency)
    const sleepDurationConsistency = 100 - (calculateStandardDeviation(sleepDurations) / avgSleepDuration) * 100;
    const consistencyScore = Math.max(0, Math.min(100, (sleepDurationConsistency + bedtimeConsistency) / 2));
    
    // Data quality score (average of all records)
    const dataQualityScore = sleepRecords.reduce((sum, record) => {
        const recordWithQuality = record as any;
        return sum + (recordWithQuality.dataQuality || 100);
    }, 0) / totalNights;
    
    // Trends (simplified - comparing first half vs second half of the week)
    const sleepDurationTrend = calculateTrend(sleepDurations);
    const qualityScores = sleepRecords.map(record => {
        switch (record.quality) {
            case 'EXCELLENT': return 4;
            case 'GOOD': return 3;
            case 'FAIR': return 2;
            case 'POOR': return 1;
            default: return 2;
        }
    });
    const sleepQualityTrend = calculateTrend(qualityScores);
    
    return {
        uidUser: userId,
        weekStartDate,
        weekEndDate,
        totalNights,
        dataQualityScore: Math.round(dataQualityScore),
        avgSleepDuration: Math.round(avgSleepDuration),
        minSleepDuration,
        maxSleepDuration,
        totalSleepTime,
        avgSleepEfficiency: Math.round(avgSleepEfficiency * 100) / 100,
        consistencyScore: Math.round(consistencyScore),
        avgLightSleepPercentage: Math.round(avgLightSleepPercentage * 100) / 100,
        avgDeepSleepPercentage: Math.round(avgDeepSleepPercentage * 100) / 100,
        avgRemSleepPercentage: Math.round(avgRemSleepPercentage * 100) / 100,
        avgLightSleepMinutes: Math.round(avgLightSleepMinutes),
        avgDeepSleepMinutes: Math.round(avgDeepSleepMinutes),
        avgRemSleepMinutes: Math.round(avgRemSleepMinutes),
        avgAwakeDuration: Math.round(avgAwakeDuration),
        qualityDistribution,
        dominantQuality,
        avgHeartRate: Math.round(avgHeartRate),
        minHeartRate,
        maxHeartRate,
        heartRateVariability: Math.round(heartRateVariability * 100) / 100,
        avgRmssd: Math.round(avgRmssd),
        avgSdnn: Math.round(avgSdnn),
        hrvTrend,
        avgAwakeningsCount: Math.round(avgAwakeningsCount * 100) / 100,
        avgMovement: Math.round(avgMovement),
        avgBedtime,
        avgWakeTime,
        bedtimeConsistency: Math.round(bedtimeConsistency),
        sleepDurationTrend,
        sleepQualityTrend,
        createdAt: now,
        updatedAt: now,
        dataVersion: '1.0',
        sourceNights
    };
}

// Helper functions (reused from generateWeeklyStats.ts)
function calculateStandardDeviation(values: number[]): number {
    const mean = values.reduce((a, b) => a + b) / values.length;
    const variance = values.reduce((sum, value) => sum + Math.pow(value - mean, 2), 0) / values.length;
    return Math.sqrt(variance);
}

function calculateTrend(values: number[]): 'IMPROVING' | 'STABLE' | 'DECLINING' {
    if (values.length < 2) return 'STABLE';
    
    const midpoint = Math.floor(values.length / 2);
    const firstHalf = values.slice(0, midpoint);
    const secondHalf = values.slice(midpoint);
    
    const firstHalfAvg = firstHalf.reduce((a, b) => a + b, 0) / firstHalf.length;
    const secondHalfAvg = secondHalf.reduce((a, b) => a + b, 0) / secondHalf.length;
    
    const difference = secondHalfAvg - firstHalfAvg;
    const percentChange = Math.abs(difference / firstHalfAvg) * 100;
    
    if (percentChange < 5) return 'STABLE';
    return difference > 0 ? 'IMPROVING' : 'DECLINING';
}

function calculateAverageTime(times: Date[]): string {
    const totalMinutes = times.reduce((sum, time) => {
        return sum + time.getUTCHours() * 60 + time.getUTCMinutes();
    }, 0);
    
    const avgMinutes = totalMinutes / times.length;
    const hours = Math.floor(avgMinutes / 60);
    const minutes = Math.round(avgMinutes % 60);
    
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

function calculateTimeConsistency(times: Date[]): number {
    const minutes = times.map(time => time.getUTCHours() * 60 + time.getUTCMinutes());
    const stdDev = calculateStandardDeviation(minutes);
    
    return Math.max(0, Math.min(100, 100 - (stdDev / 60) * 100));
}
