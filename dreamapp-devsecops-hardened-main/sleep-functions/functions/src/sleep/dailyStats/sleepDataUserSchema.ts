import { z } from "zod";

/**
 * Sleep Phase Data Schema
 * 
 * Represents a single sleep phase measurement taken every 30 seconds or when phase changes.
 * Each measurement includes physiological data (heart rate, HRV) and sleep phase classification.
 * 
 * @field id - Sequential ID for the measurement (optional, auto-generated)
 * @field phase - Sleep phase classification: LIGHT, DEEP, REM, or AWAKE
 * @field datetime - ISO 8601 timestamp of when the measurement was taken
 * @field hr_bpm - Heart rate in beats per minute (30-200 BPM range)
 * @field hrv_rmssd - HRV RMSSD metric in milliseconds (0-200ms range)
 * @field hrv_sdnn - HRV SDNN metric in milliseconds (0-200ms range)
 */
export const sleepPhaseDataSchema = z.object({
  id: z.number().positive().optional(), // Sequential ID (optional)
  phase: z.enum(['LIGHT', 'DEEP', 'REM', 'AWAKE']),
  datetime: z.string().refine((val) => {
    return !isNaN(Date.parse(val));
  }, {
    message: "datetime must be a valid ISO 8601 date string",
  }),
  hr_bpm: z.number().min(30).max(200),
  hrv_rmssd: z.number().min(0).max(200),
  hrv_sdnn: z.number().min(0).max(200)
});

/**
 * Complete Sleep Data User Schema
 * 
 * Comprehensive schema for storing a complete sleep session with all measurements,
 * calculated metrics, and metadata. Designed for single-document storage in Firestore
 * with efficient querying and analysis capabilities.
 * 
 * IDENTIFICATION FIELDS:
 * @field uidUser - User ID from authentication system
 * @field deviceId - Identifier of the wearable device used
 * 
 * TIME FIELDS:
 * @field date - Sleep session date in YYYY-MM-DD format
 * @field startTime - ISO 8601 timestamp when sleep monitoring started
 * @field endTime - ISO 8601 timestamp when sleep monitoring ended
 * @field timezone - Timezone identifier (e.g., "America/New_York")
 * 
 * DURATION METRICS (in minutes):
 * @field totalDuration - Total monitoring time (1-12 hours)
 * @field sleepDuration - Actual sleep time (excluding awake periods)
 * @field lightSleepMinutes - Total light sleep duration
 * @field deepSleepMinutes - Total deep sleep duration
 * @field remSleepMinutes - Total REM sleep duration
 * @field awakeDuration - Total time awake during monitoring
 * 
 * QUALITY METRICS:
 * @field sleepEfficiency - Percentage of sleep time vs. total time in bed (0-100%)
 * @field awakeningsCount - Number of times user woke up during the night
 * @field quality - Overall sleep quality rating (POOR, FAIR, GOOD, EXCELLENT)
 * 
 * PHYSIOLOGICAL METRICS:
 * @field avgHeartRate - Average heart rate during sleep (BPM)
 * @field minHeartRate - Minimum heart rate recorded (BPM)
 * @field maxHeartRate - Maximum heart rate recorded (BPM)
 * @field avgMovement - Average movement score (0-100)
 * @field avgRmssd - Average HRV RMSSD metric (ms)
 * @field avgSdnn - Average HRV SDNN metric (ms)
 * 
 * RAW DATA:
 * @field sleepPhaseData - Array of all sleep phase measurements (max 1440 for 12 hours)
 * 
 * METADATA:
 * @field createdAt - Unix timestamp of document creation
 * @field dataVersion - Schema version for future migrations
 */
export const sleepDataUserSchema = z.object({
  // Identification
  uidUser: z.string().min(1),
  deviceId: z.string().min(1),
  
  // Time
  date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, {
    message: "date must be in YYYY-MM-DD format"
  }),
  startTime: z.string().refine((val) => {
    return !isNaN(Date.parse(val));
  }, {
    message: "startTime must be a valid ISO 8601 date string",
  }),
  endTime: z.string().refine((val) => {
    return !isNaN(Date.parse(val));
  }, {
    message: "endTime must be a valid ISO 8601 date string",
  }),
  timezone: z.string().min(1),
  
  // Duration (in minutes)
  totalDuration: z.number().min(60).max(720),
  sleepDuration: z.number().min(30).max(720),
  
  // Sleep phases (in minutes)
  lightSleepMinutes: z.number().min(0).max(720),
  deepSleepMinutes: z.number().min(0).max(720),
  remSleepMinutes: z.number().min(0).max(720),
  awakeDuration: z.number().min(0).max(720),
  
  // Quality metrics
  sleepEfficiency: z.number().min(0).max(100),
  awakeningsCount: z.number().min(0).max(50),
  quality: z.enum(['POOR', 'FAIR', 'GOOD', 'EXCELLENT']),
  
  // Physiological metrics (averages)
  avgHeartRate: z.number().min(30).max(200),
  minHeartRate: z.number().min(30).max(200),
  maxHeartRate: z.number().min(30).max(200),
  avgMovement: z.number().min(0).max(100),
  avgRmssd: z.number().min(0).max(200),
  avgSdnn: z.number().min(0).max(200),
  
  // Sleep phase data (all measurements stored)
  sleepPhaseData: z.array(sleepPhaseDataSchema).min(1).max(1440), // Max 12 hours * 60 min * 2 (every 30 sec)
  
  // Metadata
  createdAt: z.number().positive(),
  dataVersion: z.string().default('1.0')
}).refine(data => {
  // Custom validations for data consistency
  const startTime = new Date(data.startTime).getTime();
  const endTime = new Date(data.endTime).getTime();
  
  return endTime > startTime &&
         data.sleepDuration <= data.totalDuration &&
         data.minHeartRate <= data.avgHeartRate &&
         data.avgHeartRate <= data.maxHeartRate &&
         // Validate that sleep phases sum to less than or equal to total sleep duration
         (data.lightSleepMinutes + data.deepSleepMinutes + data.remSleepMinutes) <= data.sleepDuration;
}, {
  message: "Sleep data is inconsistent - please check time relationships and phase durations"
});

export type SleepDataUser = z.infer<typeof sleepDataUserSchema>;
export type SleepPhaseData = z.infer<typeof sleepPhaseDataSchema>;