/**
 * Firebase Function to generate monthly sleep statistics
 * 
 * This function runs monthly (scheduled) to calculate comprehensive sleep statistics
 * for each user based on all their sleep records in the current month.
 * 
 * Collection: monthly_sleep_stats
 * 
 * @param event - Scheduled event (runs on the 1st of each month at 10:00 AM UTC)
 */

import { onSchedule } from "firebase-functions/v2/scheduler";
import * as logger from "firebase-functions/logger";
import { getFirestore } from "firebase-admin/firestore";
import { MonthlySleepStats, monthlySleepStatsSchema } from "./monthlySleepStatsSchema";
import { SleepDataUser } from "../dailyStats/sleepDataUserSchema";

const db = getFirestore();

// Run on the 1st of each month at 10:00 AM UTC
export const generateMonthlyStats = onSchedule("0 10 1 * *", async (event) => {
    logger.info("Starting monthly sleep statistics generation");
    
    try {
        // Get current date to determine which month to process
        const now = new Date();
        const targetMonth = now.getMonth(); // Current month (0-11)
        const targetYear = now.getFullYear();
        
        // If it's January, also process December of previous year
        const monthsToProcess = [];
        if (targetMonth === 0) {
            // January - process both December of previous year and January
            monthsToProcess.push(
                { year: targetYear - 1, month: 11 }, // December previous year
                { year: targetYear, month: 0 } // January current year
            );
        } else {
            // Process previous month
            monthsToProcess.push({ year: targetYear, month: targetMonth - 1 });
        }
        
        // Get all unique users from sleep data
        const usersSnapshot = await db.collection('user_data_sleep')
            .select('uidUser')
            .get();
        
        const uniqueUsers = new Set<string>();
        usersSnapshot.docs.forEach(doc => {
            uniqueUsers.add(doc.data().uidUser);
        });
        
        logger.info(`Found ${uniqueUsers.size} unique users to process for monthly stats`);
        
        // Process each user for each month
        for (const monthInfo of monthsToProcess) {
            logger.info(`Processing month: ${monthInfo.year}-${monthInfo.month + 1}`);
            
            for (const userId of uniqueUsers) {
                try {
                    await generateMonthlyStatsForUser(userId, monthInfo.year, monthInfo.month);
                    logger.info(`Successfully generated monthly stats for user: ${userId}, month: ${monthInfo.year}-${monthInfo.month + 1}`);
                } catch (error) {
                    logger.error(`Failed to generate monthly stats for user: ${userId}, month: ${monthInfo.year}-${monthInfo.month + 1}`, error);
                }
            }
        }
        
        logger.info("Monthly sleep statistics generation completed successfully");
        
    } catch (error) {
        logger.error("Failed to generate monthly sleep statistics", error);
        throw error;
    }
});

/**
 * Generate monthly statistics for a specific user and month
 * 
 * @param userId - User ID to generate stats for
 * @param year - Year to process
 * @param month - Month to process (0-11, where 0 = January)
 */
async function generateMonthlyStatsForUser(userId: string, year: number, month: number): Promise<void> {
    // Calculate date range for the specific month
    const startDate = new Date(year, month, 1);
    const endDate = new Date(year, month + 1, 0); // Last day of the month
    
    const monthStartDateStr = startDate.toISOString().split('T')[0];
    const monthEndDateStr = endDate.toISOString().split('T')[0];
    
    logger.info(`Generating monthly stats for user ${userId} for ${year}-${month + 1} (${monthStartDateStr} to ${monthEndDateStr})`);
    
    // Get user's sleep data for the entire month
    const sleepDataSnapshot = await db.collection('user_data_sleep')
        .where('uidUser', '==', userId)
        .where('date', '>=', monthStartDateStr)
        .where('date', '<=', monthEndDateStr)
        .orderBy('date', 'asc')
        .get();
    
    if (sleepDataSnapshot.empty) {
        logger.warn(`No sleep data found for user ${userId} in ${year}-${month + 1}`);
        return;
    }
    
    const sleepRecords: SleepDataUser[] = sleepDataSnapshot.docs.map(doc => doc.data() as SleepDataUser);
    const sourceNights = sleepDataSnapshot.docs.map(doc => doc.id);
    
    logger.info(`Found ${sleepRecords.length} sleep records for user ${userId} in ${year}-${month + 1}`);
    
    // Try to get weekly stats for this month to enhance calculations
    const weeklyStatsSnapshot = await db.collection('weekly_sleep_stats')
        .where('uidUser', '==', userId)
        .where('weekStartDate', '>=', monthStartDateStr)
        .where('weekStartDate', '<=', monthEndDateStr)
        .get();
    
    //const weeklyStats = weeklyStatsSnapshot.docs.map(doc => doc.data());
    const sourceWeeks = weeklyStatsSnapshot.docs.map(doc => doc.id);
    
    // Calculate monthly statistics
    const monthlyStats = calculateMonthlyStats(
        userId, 
        year, 
        month, 
        sleepRecords, 
        sourceNights, 
        monthStartDateStr, 
        monthEndDateStr,
        sourceWeeks.length > 0 ? sourceWeeks : [] // Use empty array instead of undefined
    );
    
    // Validate the calculated stats
    const validatedStats = monthlySleepStatsSchema.parse(monthlyStats);
    
    // Create document ID for the monthly stats
    const monthlyStatsId = `${userId}_${year}_${month + 1}`;
    
    // Save to Firestore
    await db.collection('monthly_sleep_stats').doc(monthlyStatsId).set(validatedStats);
    
    logger.info(`Monthly stats saved for user ${userId} with ID: ${monthlyStatsId}`);
}

/**
 * Calculate comprehensive monthly statistics from sleep records
 */
function calculateMonthlyStats(
    userId: string,
    year: number,
    month: number,
    sleepRecords: SleepDataUser[],
    sourceNights: string[],
    monthStartDate: string,
    monthEndDate: string,
    sourceWeeks?: string[]
): MonthlySleepStats {
    const totalNights = sleepRecords.length;
    const totalDaysInMonth = new Date(year, month + 1, 0).getDate();
    const dataCompleteness = (totalNights / totalDaysInMonth) * 100;
    const now = Date.now();
    
    // Basic sleep duration statistics
    const sleepDurations = sleepRecords.map(record => record.sleepDuration);
    const avgSleepDuration = sleepDurations.reduce((a, b) => a + b, 0) / totalNights;
    const minSleepDuration = Math.min(...sleepDurations);
    const maxSleepDuration = Math.max(...sleepDurations);
    const totalSleepTime = sleepDurations.reduce((a, b) => a + b, 0);
    const sleepDurationStdDev = calculateStandardDeviation(sleepDurations);
    
    // Sleep efficiency statistics
    const sleepEfficiencies = sleepRecords.map(record => record.sleepEfficiency);
    const avgSleepEfficiency = sleepEfficiencies.reduce((a, b) => a + b, 0) / totalNights;
    const minSleepEfficiency = Math.min(...sleepEfficiencies);
    const maxSleepEfficiency = Math.max(...sleepEfficiencies);
    const sleepEfficiencyStdDev = calculateStandardDeviation(sleepEfficiencies);
    
    // Sleep phases statistics
    const avgLightSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.lightSleepMinutes, 0) / totalNights;
    const avgDeepSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.deepSleepMinutes, 0) / totalNights;
    const avgRemSleepMinutes = sleepRecords.reduce((sum, record) => sum + record.remSleepMinutes, 0) / totalNights;
    const avgAwakeDuration = sleepRecords.reduce((sum, record) => sum + record.awakeDuration, 0) / totalNights;
    
    // Calculate sleep phase percentages
    const avgLightSleepPercentage = (avgLightSleepMinutes / avgSleepDuration) * 100;
    const avgDeepSleepPercentage = (avgDeepSleepMinutes / avgSleepDuration) * 100;
    const avgRemSleepPercentage = (avgRemSleepMinutes / avgSleepDuration) * 100;
    
    // Monthly totals in hours
    const totalLightSleepHours = sleepRecords.reduce((sum, record) => sum + record.lightSleepMinutes, 0) / 60;
    const totalDeepSleepHours = sleepRecords.reduce((sum, record) => sum + record.deepSleepMinutes, 0) / 60;
    const totalRemSleepHours = sleepRecords.reduce((sum, record) => sum + record.remSleepMinutes, 0) / 60;
    const totalAwakeHours = sleepRecords.reduce((sum, record) => sum + record.awakeDuration, 0) / 60;
    
    // Quality distribution and trends
    const qualityDistribution = {
        excellent: sleepRecords.filter(r => r.quality === 'EXCELLENT').length,
        good: sleepRecords.filter(r => r.quality === 'GOOD').length,
        fair: sleepRecords.filter(r => r.quality === 'FAIR').length,
        poor: sleepRecords.filter(r => r.quality === 'POOR').length
    };
    
    const dominantQuality = Object.entries(qualityDistribution)
        .reduce((a, b) => qualityDistribution[a[0] as keyof typeof qualityDistribution] > qualityDistribution[b[0] as keyof typeof qualityDistribution] ? a : b)[0]
        .toUpperCase() as 'POOR' | 'FAIR' | 'GOOD' | 'EXCELLENT';
    
    // Heart rate statistics
    const heartRates = sleepRecords.map(record => record.avgHeartRate);
    const avgHeartRate = heartRates.reduce((a, b) => a + b, 0) / totalNights;
    const minHeartRate = Math.min(...sleepRecords.map(r => r.minHeartRate));
    const maxHeartRate = Math.max(...sleepRecords.map(r => r.maxHeartRate));
    const heartRateStdDev = calculateStandardDeviation(heartRates);
    
    // HRV statistics
    const rmssdValues = sleepRecords.map(record => record.avgRmssd);
    const sdnnValues = sleepRecords.map(record => record.avgSdnn);
    const avgRmssd = rmssdValues.reduce((a, b) => a + b, 0) / totalNights;
    const avgSdnn = sdnnValues.reduce((a, b) => a + b, 0) / totalNights;
    const rmssdStdDev = calculateStandardDeviation(rmssdValues);
    const sdnnStdDev = calculateStandardDeviation(sdnnValues);
    const hrvTrend = calculateTrend(rmssdValues);
    
    // Sleep disruption
    const avgAwakeningsCount = sleepRecords.reduce((sum, record) => sum + record.awakeningsCount, 0) / totalNights;
    const totalAwakenings = sleepRecords.reduce((sum, record) => sum + record.awakeningsCount, 0);
    const avgMovement = sleepRecords.reduce((sum, record) => sum + record.avgMovement, 0) / totalNights;
    
    // Sleep timing patterns
    const bedtimes = sleepRecords.map(record => new Date(record.startTime));
    const wakeTimes = sleepRecords.map(record => new Date(record.endTime));
    const avgBedtime = calculateAverageTime(bedtimes);
    const avgWakeTime = calculateAverageTime(wakeTimes);
    const bedtimeStdDev = calculateTimeStandardDeviation(bedtimes);
    const wakeTimeStdDev = calculateTimeStandardDeviation(wakeTimes);
    const scheduleConsistency = Math.max(0, Math.min(100, 100 - ((bedtimeStdDev + wakeTimeStdDev) / 120) * 100));
    
    // Weekly patterns analysis
    const dayOfWeekAnalysis = analyzeDayOfWeekPatterns(sleepRecords);
    
    // Trends
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
    const qualityTrend = calculateTrend(qualityScores);
    const consistencyTrend = calculateConsistencyTrend(sleepRecords);
    
    // Goal achievement (7-9 hours = 420-540 minutes)
    const optimalSleepNights = sleepRecords.filter(r => r.sleepDuration >= 420 && r.sleepDuration <= 540).length;
    const optimalSleepPercentage = (optimalSleepNights / totalNights) * 100;
    
    // Sleep debt analysis
    const recommendedSleepMinutes = 480; // 8 hours
    const avgSleepDebt = recommendedSleepMinutes - avgSleepDuration;
    const totalSleepDebt = sleepRecords.reduce((sum, record) => sum + (recommendedSleepMinutes - record.sleepDuration), 0);
    
    // Recovery metrics
    const recoveryDays = sleepRecords.filter(r => r.avgRmssd > avgRmssd && r.sleepEfficiency > avgSleepEfficiency).length;
    const stressIndicatorDays = sleepRecords.filter(r => r.avgRmssd < (avgRmssd * 0.8) || r.awakeningsCount > (avgAwakeningsCount * 1.5)).length;
    
    // Weekly breakdown
    const weeklyBreakdown = calculateWeeklyBreakdown(sleepRecords, year, month);
    
    // Data quality score
    const dataQualityScore = sleepRecords.reduce((sum, record) => {
        const recordWithQuality = record as any;
        return sum + (recordWithQuality.dataQuality || 100);
    }, 0) / totalNights;
    
    return {
        uidUser: userId,
        year,
        month: month + 1, // Convert from 0-11 to 1-12
        monthStartDate,
        monthEndDate,
        totalNights,
        totalDaysInMonth,
        dataCompleteness: Math.round(dataCompleteness * 100) / 100,
        dataQualityScore: Math.round(dataQualityScore),
        avgSleepDuration: Math.round(avgSleepDuration),
        minSleepDuration,
        maxSleepDuration,
        totalSleepTime,
        sleepDurationStdDev: Math.round(sleepDurationStdDev),
        avgSleepEfficiency: Math.round(avgSleepEfficiency * 100) / 100,
        minSleepEfficiency: Math.round(minSleepEfficiency * 100) / 100,
        maxSleepEfficiency: Math.round(maxSleepEfficiency * 100) / 100,
        sleepEfficiencyStdDev: Math.round(sleepEfficiencyStdDev * 100) / 100,
        avgLightSleepMinutes: Math.round(avgLightSleepMinutes),
        avgDeepSleepMinutes: Math.round(avgDeepSleepMinutes),
        avgRemSleepMinutes: Math.round(avgRemSleepMinutes),
        avgAwakeDuration: Math.round(avgAwakeDuration),
        avgLightSleepPercentage: Math.round(avgLightSleepPercentage * 100) / 100,
        avgDeepSleepPercentage: Math.round(avgDeepSleepPercentage * 100) / 100,
        avgRemSleepPercentage: Math.round(avgRemSleepPercentage * 100) / 100,
        totalLightSleepHours: Math.round(totalLightSleepHours * 100) / 100,
        totalDeepSleepHours: Math.round(totalDeepSleepHours * 100) / 100,
        totalRemSleepHours: Math.round(totalRemSleepHours * 100) / 100,
        totalAwakeHours: Math.round(totalAwakeHours * 100) / 100,
        qualityDistribution,
        dominantQuality,
        qualityTrend,
        avgHeartRate: Math.round(avgHeartRate),
        minHeartRate,
        maxHeartRate,
        heartRateStdDev: Math.round(heartRateStdDev * 100) / 100,
        avgRmssd: Math.round(avgRmssd),
        avgSdnn: Math.round(avgSdnn),
        rmssdStdDev: Math.round(rmssdStdDev * 100) / 100,
        sdnnStdDev: Math.round(sdnnStdDev * 100) / 100,
        hrvTrend,
        avgAwakeningsCount: Math.round(avgAwakeningsCount * 100) / 100,
        totalAwakenings,
        avgMovement: Math.round(avgMovement),
        avgBedtime,
        avgWakeTime,
        bedtimeStdDev: Math.round(bedtimeStdDev),
        wakeTimeStdDev: Math.round(wakeTimeStdDev),
        scheduleConsistency: Math.round(scheduleConsistency),
        bestDayOfWeek: dayOfWeekAnalysis.bestDay,
        worstDayOfWeek: dayOfWeekAnalysis.worstDay,
        weekdayVsWeekendDifference: Math.round(dayOfWeekAnalysis.weekdayVsWeekendDifference),
        sleepDurationTrend,
        sleepQualityTrend: qualityTrend,
        consistencyTrend,
        optimalSleepNights,
        optimalSleepPercentage: Math.round(optimalSleepPercentage * 100) / 100,
        recommendedSleepMinutes,
        avgSleepDebt: Math.round(avgSleepDebt),
        totalSleepDebt: Math.round(totalSleepDebt),
        recoveryDays,
        stressIndicatorDays,
        weeklyBreakdown,
        createdAt: now,
        updatedAt: now,
        dataVersion: '1.0',
        sourceNights,
        sourceWeeks: sourceWeeks || [] // Ensure sourceWeeks is never undefined
    };
}

// Helper functions
function calculateStandardDeviation(values: number[]): number {
    const mean = values.reduce((a, b) => a + b) / values.length;
    const variance = values.reduce((sum, value) => sum + Math.pow(value - mean, 2), 0) / values.length;
    return Math.sqrt(variance);
}

function calculateTrend(values: number[]): 'IMPROVING' | 'STABLE' | 'DECLINING' {
    if (values.length < 4) return 'STABLE';
    
    const firstQuarter = values.slice(0, Math.floor(values.length / 4));
    const lastQuarter = values.slice(-Math.floor(values.length / 4));
    
    const firstQuarterAvg = firstQuarter.reduce((a, b) => a + b, 0) / firstQuarter.length;
    const lastQuarterAvg = lastQuarter.reduce((a, b) => a + b, 0) / lastQuarter.length;
    
    const difference = lastQuarterAvg - firstQuarterAvg;
    const percentChange = Math.abs(difference / firstQuarterAvg) * 100;
    
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

function calculateTimeStandardDeviation(times: Date[]): number {
    const minutes = times.map(time => time.getUTCHours() * 60 + time.getUTCMinutes());
    return calculateStandardDeviation(minutes);
}

function analyzeDayOfWeekPatterns(sleepRecords: SleepDataUser[]) {
    const dayOfWeekData: { [key: number]: { durations: number[], count: number } } = {};
    
    sleepRecords.forEach(record => {
        const dayOfWeek = new Date(record.date).getDay(); // 0 = Sunday, 6 = Saturday
        if (!dayOfWeekData[dayOfWeek]) {
            dayOfWeekData[dayOfWeek] = { durations: [], count: 0 };
        }
        dayOfWeekData[dayOfWeek].durations.push(record.sleepDuration);
        dayOfWeekData[dayOfWeek].count++;
    });
    
    const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
    let bestDay = 'SUNDAY';
    let worstDay = 'SUNDAY';
    let bestAvg = 0;
    let worstAvg = Infinity;
    
    // Calculate weekday vs weekend
    let weekdayDurations: number[] = [];
    let weekendDurations: number[] = [];
    
    Object.keys(dayOfWeekData).forEach(dayStr => {
        const day = parseInt(dayStr);
        const avgDuration = dayOfWeekData[day].durations.reduce((a, b) => a + b, 0) / dayOfWeekData[day].durations.length;
        
        if (avgDuration > bestAvg) {
            bestAvg = avgDuration;
            bestDay = dayNames[day];
        }
        
        if (avgDuration < worstAvg) {
            worstAvg = avgDuration;
            worstDay = dayNames[day];
        }
        
        // Weekday = Monday(1) to Friday(5), Weekend = Saturday(6) and Sunday(0)
        if (day >= 1 && day <= 5) {
            weekdayDurations.push(...dayOfWeekData[day].durations);
        } else {
            weekendDurations.push(...dayOfWeekData[day].durations);
        }
    });
    
    const weekdayAvg = weekdayDurations.length > 0 ? weekdayDurations.reduce((a, b) => a + b, 0) / weekdayDurations.length : 0;
    const weekendAvg = weekendDurations.length > 0 ? weekendDurations.reduce((a, b) => a + b, 0) / weekendDurations.length : 0;
    const weekdayVsWeekendDifference = weekendAvg - weekdayAvg;
    
    return {
        bestDay: bestDay as 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY',
        worstDay: worstDay as 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY',
        weekdayVsWeekendDifference
    };
}

function calculateConsistencyTrend(sleepRecords: SleepDataUser[]): 'IMPROVING' | 'STABLE' | 'DECLINING' {
    if (sleepRecords.length < 7) return 'STABLE';
    
    const firstWeek = sleepRecords.slice(0, 7);
    const lastWeek = sleepRecords.slice(-7);
    
    const firstWeekStdDev = calculateStandardDeviation(firstWeek.map(r => r.sleepDuration));
    const lastWeekStdDev = calculateStandardDeviation(lastWeek.map(r => r.sleepDuration));
    
    const difference = firstWeekStdDev - lastWeekStdDev; // Lower std dev = better consistency
    
    if (Math.abs(difference) < 10) return 'STABLE';
    return difference > 0 ? 'IMPROVING' : 'DECLINING';
}

function calculateWeeklyBreakdown(sleepRecords: SleepDataUser[], year: number, month: number) {
    const weeks: { [key: number]: SleepDataUser[] } = {};
    
    sleepRecords.forEach(record => {
        const date = new Date(record.date);
        const dayOfMonth = date.getDate();
        const weekOfMonth = Math.ceil(dayOfMonth / 7);
        
        if (!weeks[weekOfMonth]) {
            weeks[weekOfMonth] = [];
        }
        weeks[weekOfMonth].push(record);
    });
    
    return Object.keys(weeks).map(weekStr => {
        const weekNumber = parseInt(weekStr);
        const weekRecords = weeks[weekNumber];
        const avgSleepDuration = weekRecords.reduce((sum, r) => sum + r.sleepDuration, 0) / weekRecords.length;
        const avgSleepEfficiency = weekRecords.reduce((sum, r) => sum + r.sleepEfficiency, 0) / weekRecords.length;
        
        // Find dominant quality
        const qualityDistribution = {
            excellent: weekRecords.filter(r => r.quality === 'EXCELLENT').length,
            good: weekRecords.filter(r => r.quality === 'GOOD').length,
            fair: weekRecords.filter(r => r.quality === 'FAIR').length,
            poor: weekRecords.filter(r => r.quality === 'POOR').length
        };
        
        const dominantQuality = Object.entries(qualityDistribution)
            .reduce((a, b) => qualityDistribution[a[0] as keyof typeof qualityDistribution] > qualityDistribution[b[0] as keyof typeof qualityDistribution] ? a : b)[0]
            .toUpperCase() as 'POOR' | 'FAIR' | 'GOOD' | 'EXCELLENT';
        
        return {
            weekNumber,
            avgSleepDuration: Math.round(avgSleepDuration),
            avgSleepEfficiency: Math.round(avgSleepEfficiency * 100) / 100,
            dominantQuality,
            nightsWithData: weekRecords.length
        };
    });
}
