export const API_URL = (
  import.meta.env.VITE_API_URL || "https://dreamapp-api.onrender.com"
).replace(/\/$/, "");
export interface UserInfo {
  id: string;
  userName: string;
  fullname: string;
  role: string;
  active: boolean;
}
export interface User {
  uidUser: string;
  username: string;
  weightKg: number;
  heightCm: number;
  age: number;
  sex: string;
  profilePictureUrl?: string;
}
export interface SleepAverage {
  sleepEfficiency: number;
  sleepDuration: number;
  light: number;
  deep: number;
  rem: number;
  awake: number;
  avgHR: number;
  awakenings: number;
}
export interface Point {
  date: string;
  sleepEfficiency: number;
}
export interface SleepStats {
  efficiencyChart: {
    last7Days: Point[];
    lastMonth: Point[];
    last6Months: Point[];
    lastYear: Point[];
  };
  qualityPie: { lastMonth: Record<string, number> };
  averagesLast7Days: SleepAverage;
  lastDayStats: SleepAverage;
}
export interface Prediction {
  date: string;
  sleepEfficiency: number;
}
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message);
  }
}
const TOKEN_KEY = "dreamapp_session";
export const clearSession = () => sessionStorage.removeItem(TOKEN_KEY);
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  const token = sessionStorage.getItem(TOKEN_KEY);
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError("No se pudo conectar con DreamApp API.", 0);
  }
  const data = (await response.json().catch(() => null)) as Record<
    string,
    unknown
  > | null;
  if (!response.ok) {
    const message =
      typeof data?.error === "string"
        ? data.error
        : `Error del servidor (${response.status})`;
    throw new ApiError(message, response.status);
  }
  return data as T;
}
export const api = {
  health: () => request<{ status: string }>("/health"),
  login: async (userName: string, password: string) => {
    const result = await request<{
      success: boolean;
      data: UserInfo;
      token: string;
    }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ userName, password }),
    });
    sessionStorage.setItem(TOKEN_KEY, result.token);
    return result;
  },
  register: (payload: { firstName: string; lastName: string; userName: string; email: string; password: string }) =>
    request<{ success: boolean; message: string }>("/auth/register", { method: "POST", body: JSON.stringify(payload) }),
  verify: (email: string, code: string) =>
    request<{ success: boolean; message: string }>("/auth/verify", { method: "POST", body: JSON.stringify({ email, code }) }),
  logout: () =>
    request<{ success: boolean }>("/auth/logout", { method: "POST" }).finally(
      clearSession,
    ),
  users: () => request<User[]>("/users"),
  stats: (uid: string) =>
    request<{ success: boolean; data: SleepStats }>(
      `/sleep/stats?uid=${encodeURIComponent(uid)}`,
    ),
  recommendation: (uid: string) =>
    request<{ success: boolean; recommendation: string }>(
      `/ai/recommendation?uid=${encodeURIComponent(uid)}`,
    ),
  predictions: (uid: string) =>
    request<{ success: boolean; nextMonthPredictions: Prediction[] }>(
      `/ai/predictions-next-month-efficiency?uid=${encodeURIComponent(uid)}`,
    ),
  subscription: () =>
    request<{ success: boolean; plan: string }>("/subscription"),
  changePlan: (plan: string) =>
    request<{ success: boolean; plan: string; message: string }>(
      "/subscription",
      { method: "PATCH", body: JSON.stringify({ plan }) },
    ),
};
