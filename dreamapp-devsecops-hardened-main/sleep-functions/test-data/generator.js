/**
 * Sleep Data Generator & Auto-Registrator
 * 
 * Generates realistic sleep data and automatically registers it
 * using the registerUserSleepData Firebase Function
 */

const http = require('http');

// Configuration
const CONFIG = {
    //     userId: 'l20rMb0Sz6QjViBxPdi5Yfab8R23',
    userId: 'W3lmcpb9NPT0pVJrfZfBw8xHIcm1',
    deviceId: 'wearable_device_001',
    timezone: 'America/New_York',
    daysToGenerate: 364, // Generate last 60 days (2 months of data)
    functionUrl: 'http://127.0.0.1:5001/dream-34ed4/us-central1/registerUserSleepData'
};

// Sleep quality levels with probabilities
const SLEEP_QUALITIES = {
    'EXCELLENT': { weight: 0.15, efficiency: [90, 95], awakenings: [0, 2] },
    'GOOD': { weight: 0.45, efficiency: [80, 89], awakenings: [1, 3] },
    'FAIR': { weight: 0.30, efficiency: [70, 79], awakenings: [2, 5] },
    'POOR': { weight: 0.10, efficiency: [50, 69], awakenings: [3, 8] }
};

// Helper functions
function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}

function randomInt(min, max) {
    return Math.floor(randomBetween(min, max + 1));
}

function getWeightedRandomQuality() {
    const rand = Math.random();
    let cumulative = 0;
    
    for (const [quality, config] of Object.entries(SLEEP_QUALITIES)) {
        cumulative += config.weight;
        if (rand <= cumulative) {
            return quality;
        }
    }
    return 'GOOD'; // fallback
}

function generateBedtime() {
    // Bedtime between 21:00 and 24:00 (9 PM - 12 AM)
    const hour = randomInt(21, 23);
    const minute = randomInt(0, 59);
    return { hour, minute };
}

function generateSleepDuration(quality) {
    // Base duration: 6.5 to 9 hours (390 to 540 minutes)
    let baseDuration = randomInt(390, 540);
    
    // Adjust based on quality
    switch (quality) {
        case 'EXCELLENT':
            baseDuration = Math.max(420, baseDuration); // At least 7 hours
            break;
        case 'POOR':
            baseDuration = Math.min(450, baseDuration); // Max 7.5 hours
            break;
    }
    
    return baseDuration;
}

function generateHeartRateData(phase, baseHR) {
    const variations = {
        'AWAKE': { min: 0.9, max: 1.2 },
        'LIGHT': { min: 0.8, max: 1.0 },
        'DEEP': { min: 0.7, max: 0.9 },
        'REM': { min: 0.95, max: 1.15 }
    };
    
    const variation = variations[phase] || variations['LIGHT'];
    return Math.round(baseHR * randomBetween(variation.min, variation.max));
}

function generateHRVData(phase) {
    const baseValues = {
        'AWAKE': { rmssd: [25, 40], sdnn: [20, 35] },
        'LIGHT': { rmssd: [35, 50], sdnn: [30, 45] },
        'DEEP': { rmssd: [45, 65], sdnn: [40, 55] },
        'REM': { rmssd: [30, 45], sdnn: [25, 40] }
    };
    
    const values = baseValues[phase] || baseValues['LIGHT'];
    return {
        rmssd: Math.round(randomBetween(values.rmssd[0], values.rmssd[1])),
        sdnn: Math.round(randomBetween(values.sdnn[0], values.sdnn[1]))
    };
}

function generateSleepPhases(sleepDuration) {
    // Typical sleep phase distribution
    const distributions = {
        light: 0.45,   // 45% light sleep
        deep: 0.25,    // 25% deep sleep
        rem: 0.20,     // 20% REM sleep
        awake: 0.10    // 10% awake time
    };
    
    return {
        lightSleepMinutes: Math.round(sleepDuration * distributions.light),
        deepSleepMinutes: Math.round(sleepDuration * distributions.deep),
        remSleepMinutes: Math.round(sleepDuration * distributions.rem),
        awakeDuration: Math.round(sleepDuration * distributions.awake)
    };
}

function generateSleepPhaseData(startTime, totalDuration, phases, avgHeartRate) {
    const phaseData = [];
    const startTimestamp = new Date(startTime).getTime();
    
    // Create a realistic sleep pattern
    const pattern = [
        { phase: 'AWAKE', duration: 10 },
        { phase: 'LIGHT', duration: phases.lightSleepMinutes * 0.3 },
        { phase: 'DEEP', duration: phases.deepSleepMinutes * 0.6 },
        { phase: 'LIGHT', duration: phases.lightSleepMinutes * 0.2 },
        { phase: 'REM', duration: phases.remSleepMinutes * 0.5 },
        { phase: 'LIGHT', duration: phases.lightSleepMinutes * 0.3 },
        { phase: 'DEEP', duration: phases.deepSleepMinutes * 0.4 },
        { phase: 'REM', duration: phases.remSleepMinutes * 0.5 },
        { phase: 'LIGHT', duration: phases.lightSleepMinutes * 0.2 },
        { phase: 'AWAKE', duration: phases.awakeDuration }
    ];
    
    let currentTime = startTimestamp;
    let id = 1;
    
    pattern.forEach(segment => {
        // Sample every 30 minutes for this segment
        const samples = Math.max(1, Math.floor(segment.duration / 30));
        const intervalMs = (segment.duration * 60 * 1000) / samples;
        
        for (let i = 0; i < samples; i++) {
            const datetime = new Date(currentTime + (i * intervalMs)).toISOString();
            const hr_bpm = generateHeartRateData(segment.phase, avgHeartRate);
            const hrv = generateHRVData(segment.phase);
            
            phaseData.push({
                id: id++,
                phase: segment.phase,
                datetime: datetime,
                hr_bpm: hr_bpm,
                hrv_rmssd: hrv.rmssd,
                hrv_sdnn: hrv.sdnn
            });
        }
        
        currentTime += segment.duration * 60 * 1000;
    });
    
    return phaseData.slice(0, 20); // Limit to reasonable amount of data
}

function generateSleepData(date) {
    const quality = getWeightedRandomQuality();
    const qualityConfig = SLEEP_QUALITIES[quality];
    
    // Generate bedtime
    const bedtime = generateBedtime();
    const sleepDuration = generateSleepDuration(quality);
    const totalDuration = sleepDuration + randomInt(30, 90); // Add time in bed vs sleep time
    
    // Calculate times
    const startTime = new Date(date);
    startTime.setHours(bedtime.hour, bedtime.minute, 0, 0);
    
    const endTime = new Date(startTime.getTime() + totalDuration * 60 * 1000);
    
    // Generate sleep phases
    const phases = generateSleepPhases(sleepDuration);
    
    // Generate physiological data
    const avgHeartRate = randomInt(50, 70);
    const minHeartRate = Math.round(avgHeartRate * 0.8);
    const maxHeartRate = Math.round(avgHeartRate * 1.3);
    
    const avgRmssd = randomInt(35, 55);
    const avgSdnn = randomInt(30, 50);
    const avgMovement = randomInt(5, 25);
    
    const sleepEfficiency = randomBetween(qualityConfig.efficiency[0], qualityConfig.efficiency[1]);
    const awakeningsCount = randomInt(qualityConfig.awakenings[0], qualityConfig.awakenings[1]);
    
    // Generate detailed phase data
    const sleepPhaseData = generateSleepPhaseData(startTime, totalDuration, phases, avgHeartRate);
    
    return {
        uidUser: CONFIG.userId,
        deviceId: CONFIG.deviceId,
        date: date.toISOString().split('T')[0],
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        timezone: CONFIG.timezone,
        totalDuration: totalDuration,
        sleepDuration: sleepDuration,
        lightSleepMinutes: phases.lightSleepMinutes,
        deepSleepMinutes: phases.deepSleepMinutes,
        remSleepMinutes: phases.remSleepMinutes,
        awakeDuration: phases.awakeDuration,
        sleepEfficiency: Math.round(sleepEfficiency * 100) / 100,
        awakeningsCount: awakeningsCount,
        quality: quality,
        avgHeartRate: avgHeartRate,
        minHeartRate: minHeartRate,
        maxHeartRate: maxHeartRate,
        avgMovement: avgMovement,
        avgRmssd: avgRmssd,
        avgSdnn: avgSdnn,
        sleepPhaseData: sleepPhaseData,
        createdAt: Date.now(),
        dataVersion: '1.0'
    };
}

// Function to register sleep data via HTTP
async function registerSleepData(data) {
    return new Promise((resolve, reject) => {
        const postData = JSON.stringify(data);
        
        const options = {
            hostname: '127.0.0.1',
            port: 5001,
            path: '/dream-34ed4/us-central1/registerUserSleepData',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };
        
        const req = http.request(options, (res) => {
            let responseBody = '';
            
            res.on('data', (chunk) => {
                responseBody += chunk;
            });
            
            res.on('end', () => {
                try {
                    const response = JSON.parse(responseBody);
                    if (res.statusCode === 201) {
                        resolve(response);
                    } else {
                        reject(new Error(`HTTP ${res.statusCode}: ${response.message || responseBody}`));
                    }
                } catch (error) {
                    reject(new Error(`Parse error: ${error.message}`));
                }
            });
        });
        
        req.on('error', (error) => {
            reject(error);
        });
        
        req.write(postData);
        req.end();
    });
}

async function generateAndRegisterTestData() {
    console.log('🔄 Generating and registering sleep test data...');
    console.log(`📡 Function URL: ${CONFIG.functionUrl}`);
    console.log(`👤 User ID: ${CONFIG.userId}`);
    console.log(`📅 Days to generate: ${CONFIG.daysToGenerate}`);
    console.log('');
    
    const results = { successful: [], failed: [] };
    
    for (let i = 0; i < CONFIG.daysToGenerate; i++) {
        // Generate dates for the last 60 days from today (going backwards)
        const date = new Date();
        // With today
        // date.setDate(date.getDate() - (CONFIG.daysToGenerate - 1 - i)); // Start from 59 days ago and go forward to today
        // Without today
        date.setDate(date.getDate() - (CONFIG.daysToGenerate - i)); // Start from 59 days ago and go forward to today
        
        try {
            console.log(`📅 Generating data for ${date.toISOString().split('T')[0]}...`);
            
            // Generate realistic sleep data
            const sleepData = generateSleepData(date);
            
            console.log(`   💤 Quality: ${sleepData.quality}`);
            console.log(`   ⏱️  Duration: ${sleepData.sleepDuration} min`);
            console.log(`   💓 Avg HR: ${sleepData.avgHeartRate} bpm`);
            console.log(`   📤 Registering...`);
            
            // Register the data
            const response = await registerSleepData(sleepData);
            
            console.log(`   ✅ Success! Document ID: ${response.data.documentId}`);
            results.successful.push({
                date: sleepData.date,
                quality: sleepData.quality,
                duration: sleepData.sleepDuration,
                documentId: response.data.documentId
            });
            
            // Small delay between requests to avoid overwhelming the server
            if (i < CONFIG.daysToGenerate - 1) {
                console.log('   ⏳ Waiting 1 second...');
                await new Promise(resolve => setTimeout(resolve, 50));
            }
            
        } catch (error) {
            console.error(`   ❌ Failed for ${date.toISOString().split('T')[0]}: ${error.message}`);
            results.failed.push({
                date: date.toISOString().split('T')[0],
                error: error.message
            });
        }
        
        console.log('');
    }
    
    // Print summary
    console.log('🎉 Registration completed!');
    console.log('');
    console.log('� Summary:');
    console.log(`✅ Successful: ${results.successful.length}`);
    console.log(`❌ Failed: ${results.failed.length}`);
    
    if (results.successful.length > 0) {
        console.log('');
        console.log('✅ Successfully registered:');
        results.successful.forEach(item => {
            console.log(`   📅 ${item.date} - ${item.quality} (${item.duration}min) - ID: ${item.documentId}`);
        });
    }
    
    if (results.failed.length > 0) {
        console.log('');
        console.log('❌ Failed registrations:');
        results.failed.forEach(item => {
            console.log(`   📅 ${item.date} - ${item.error}`);
        });
    }
    
    console.log('');
    console.log('🚀 Next steps:');
    console.log('   📊 Weekly Statistics:');
    console.log('     POST http://127.0.0.1:5001/dream-34ed4/us-central1/generateWeeklyStatsForUser');
    console.log('     Body: {"uidUser": "' + CONFIG.userId + '", "daysBack": 7}');
    console.log('   📅 Monthly Statistics (current month):');
    console.log('     POST http://127.0.0.1:5001/dream-34ed4/us-central1/generateMonthlyStatsForUser');
    console.log('     Body: {"uidUser": "' + CONFIG.userId + '"}');
    console.log('   📅 Monthly Statistics (previous month):');
    console.log('     POST http://127.0.0.1:5001/dream-34ed4/us-central1/generateMonthlyStatsForUser');
    console.log('     Body: {"uidUser": "' + CONFIG.userId + '", "year": ' + (new Date().getFullYear()) + ', "month": ' + (new Date().getMonth()) + '}');
    console.log('   🔄 All Users:');
    console.log('     POST http://127.0.0.1:5001/dream-34ed4/us-central1/generateAllUsersWeeklyStats');
    console.log('     POST http://127.0.0.1:5001/dream-34ed4/us-central1/generateAllUsersMonthlyStats');
    
    return results;
}

// Run the generator and registrator
if (require.main === module) {
    generateAndRegisterTestData().catch(error => {
        console.error('� Fatal error:', error.message);
        process.exit(1);
    });
}

module.exports = { generateAndRegisterTestData, generateSleepData, CONFIG };
