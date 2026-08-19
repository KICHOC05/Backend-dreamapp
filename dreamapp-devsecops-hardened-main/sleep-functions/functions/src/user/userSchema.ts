/**
 * @file userSchema.ts
 * @description Zod validation schemas for user management endpoints
 * @author IoT Sleep Monitoring Team
 * @version 1.0.0
 */

import { z } from 'zod';

/**
 * Schema for user registration validation
 * @description Validates all required fields for creating a new user profile
 */
const registerUserSchema = z.object({
  uidUser: z.string({
    required_error: "User UID is required",
    invalid_type_error: "UID must be a string"
  }),
  weightKg: z.number({
    required_error: "Weight is required",
    invalid_type_error: "Weight must be a number"
  }).min(1, "Weight must be greater than 0"),
  heightCm: z.number({
    required_error: "Height is required",
    invalid_type_error: "Height must be a number"
  }).min(30).max(300),
  age: z.number({
    required_error: "Age is required",
    invalid_type_error: "Age must be a number"
  }).min(0).max(120),
  sex: z.enum(["men", "woman"], {
    required_error: "Sex is required",
    invalid_type_error: "Sex must be 'men' or 'woman'"
  }),
});

/**
 * Schema for user profile update validation
 * @description Validates optional fields for updating existing user profile
 */
const updateUserSchema = z.object({
  weightKg: z.number().min(1, "Weight must be greater than 0"),
  heightCm: z.number().min(30).max(300),
  age: z.number().min(0).max(120),
  sex: z.enum(["men", "woman"]),
});

export { 
  registerUserSchema,
  updateUserSchema
};
