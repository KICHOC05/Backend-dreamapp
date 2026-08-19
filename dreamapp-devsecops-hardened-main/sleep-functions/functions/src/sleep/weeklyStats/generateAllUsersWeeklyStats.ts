/**
 * Firebase Function to manually trigger weekly stats generation for all users
 * 
 * This is a development/admin function that mimics the scheduled function
 * but can be called manually via HTTP request.
 * 
 * Endpoint: POST /generateAllUsersWeeklyStats
 * Body: {} (empty body or optional filters)
 * 
 * @param req - HTTP request
 * @param res - HTTP response
 */

import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { getFirestore } from "firebase-admin/firestore";
import { WeeklySleepStats, weeklySleepStatsSchema } from "./weeklySleepStatsSchema";
import { SleepDataUser } from "../dailyStats/sleepDataUserSchema";
import { requireAuth, functionsInternalKey } from "../../security/auth";

const db = getFirestore();

export const generateAllUsersWeeklyStats = onRequest({secrets: [functionsInternalKey]}, async (req, res) => {
    // Only allow POST requests
    if (req.method !== "POST") {
        res.status(405).json({ 
            error: "Method not allowed", 
            message: "Only POST requests are accepted" 
        });
        return;
    }
    if (!(await requireAuth(req, res, undefined, true))) return;

    logger.info("Starting manual weekly sleep statistics generation for all users");
    
    try {
        const results: {
            successful: string[],
            failed: { userId: string, error: string }[],
            skipped: string[]
        } = {
            successful: [],
            failed: [],
            skipped: []
        };

        // Get all unique users from sleep data
        const usersSnapshot = await db.collection('user_data_sleep')
            .select('uidUser')
            .get();
        
        const uniqueUsers = new Set<string>();
        usersSnapshot.docs.forEach(doc => {
            uniqueUsers.add(doc.data().uidUser);
        });
        
        logger.info(`Found ${uniqueUsers.size} unique users to process`);
        
        // Process each user
        for (const userId of uniqueUsers) {
            try {
                const hasData = await generateWeeklyStatsForUser(userId);
                if (hasData) {
                    results.successful.push(userId);
                    logger.info(`Successfully generated weekly stats for user: ${userId}`);
                } else {
                    results.skipped.push(userId);
                    logger.info(`Skipped user ${userId} - no recent data`);
                }
            } catch (error) {
                const errorMessage = error instanceof Error ? error.message : 'Unknown error';
                results.failed.push({ userId, error: errorMessage });
                logger.error(`Failed to generate weekly stats for user: ${userId}`, error);
            }
        }
        
        logger.info("Manual weekly sleep statistics generation completed", {
            totalUsers: uniqueUsers.size,
            successful: results.successful.length,
            failed: results.failed.length,
            skipped: results.skipped.length
        });

        res.status(200).json({
            success: true,
            message: "Weekly statistics generation completed",
            summary: {
                totalUsers: uniqueUsers.size,
                successful: results.successful.length,
                failed: results.failed.length,
                skipped: results.skipped.length
            },
            details: results
        });
        
    } catch (error) {
        logger.error("Failed to generate weekly sleep statistics for all users", error);
        res.status(500).json({
            error: "Internal server error",
            message: "Failed to generate weekly statistics"
        });
    }
});

/**
 * Generate weekly statistics for a specific user
 * 
 * @param userId - User ID to generate stats for
 * @returns boolean - true if stats were generated, false if no data found
 */
async function generateWeeklyStatsForUser(userId: string): Promise<boolean> {
    // Calculate date range for the last 7 days
    const endDate = new Date();
    endDate.setUTCHours(23, 59, 59, 999); // End of today (include today)
    
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 6); // 6 days ago + today = 7 days total
    startDate.setUTCHours(0, 0, 0, 0); // Start of that day
    
    const weekStartDateStr = startDate.toISOString().split('T')[0];
    const weekEndDateStr = endDate.toISOString().split('T')[0];
    
    // Get user's sleep data for the last 7 days
    const sleepDataSnapshot = await db.collection('user_data_sleep')
        .where('uidUser', '==', userId)
        .where('date', '>=', weekStartDateStr)
        .where('date', '<=', weekEndDateStr) // Changed from < to <= to include today
        .orderBy('date', 'desc')
        .limit(7)
        .get();
    
    if (sleepDataSnapshot.empty) {
        return false; // No data found
    }
    
    const sleepRecords: SleepDataUser[] = sleepDataSnapshot.docs.map(doc => doc.data() as SleepDataUser);
    const sourceNights = sleepDataSnapshot.docs.map(doc => doc.id);
    
    // Calculate weekly statistics
    const weeklyStats = calculateWeeklyStats(userId, sleepRecords, sourceNights, weekStartDateStr, weekEndDateStr);
    
    // Validate the calculated stats
    const validatedStats = weeklySleepStatsSchema.parse(weeklyStats);
    
    // Create document ID for the weekly stats
    const weeklyStatsId = `${userId}_${weekStartDateStr}_${weekEndDateStr}`;
    
    // Save to Firestore
    await db.collection('weekly_sleep_stats').doc(weeklyStatsId).set(validatedStats);
    
    return true; // Success
}

// Reuse the same calculation function from generateWeeklyStatsForUser.ts
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

// Helper functions
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
