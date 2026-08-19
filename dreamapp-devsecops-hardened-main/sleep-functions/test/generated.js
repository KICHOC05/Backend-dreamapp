const axios = require('axios');

const url = 'http://localhost:5001/dream-34ed4/us-central1/registerSleepData'; // Cambia <tu-proyecto>
const uid = 'new_user_123';

// Calcular el lunes de la0. semana pasada
const now = new Date();
const dayOfWeek = now.getUTCDay();
const lastMonday = new Date(now);
lastMonday.setUTCDate(now.getUTCDate() - dayOfWeek - 6);
lastMonday.setUTCHours(9, 0, 0, 0); // 9:00 AM UTC

// Calcular el primer día del mes pasado
const firstDayLastMonth = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth() - 1, 1, 9, 0, 0, 0));
const lastDayLastMonth = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 0, 9, 0, 0, 0));
const daysInLastMonth = lastDayLastMonth.getUTCDate();

async function sendSleepData(date) {
  const sleepDuration = (6 + Math.random() * 3).toFixed(1);
  const deepSleepH = (1 + Math.random() * 2).toFixed(1);
  const lightSleepH = (3 + Math.random() * 2).toFixed(1);
  const remSleepH = (1 + Math.random()).toFixed(1);
  const avgHeartRate = Math.floor(55 + Math.random() * 15);
  const avgSpo2 = Math.floor(95 + Math.random() * 4);
  const score = Math.floor(70 + Math.random() * 30);

  const payload = {
    uid,
    timestamp: date.toISOString(),
    sleepDuration: Number(sleepDuration),
    start: new Date(date.getTime() - 8 * 60 * 60 * 1000).toISOString(),
    end: date.toISOString(),
    deepSleepH: Number(deepSleepH),
    lightSleepH: Number(lightSleepH),
    remSleepH: Number(remSleepH),
    avgHeartRate,
    avgSpo2,
    score
  };

  try {
    const res = await axios.post(url, payload);
    console.log(`OK: ${date.toISOString()}`);
  } catch (err) {
    console.error(`ERROR: ${date.toISOString()}`, err.response?.data || err.message);
  }
}

(async () => {
  // Generar datos para cada día del mes pasado
  for (let i = 0; i < daysInLastMonth; i++) {
    const date = new Date(firstDayLastMonth);
    date.setUTCDate(firstDayLastMonth.getUTCDate() + i);
    await sendSleepData(date);
  }
  // Generar datos para cada día de la semana pasada
  for (let i = 0; i < 7; i++) {
    const date = new Date(lastMonday);
    date.setUTCDate(lastMonday.getUTCDate() + i);
    await sendSleepData(date);
  }
})();