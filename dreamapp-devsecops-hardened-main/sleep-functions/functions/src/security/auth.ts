import { getAuth } from "firebase-admin/auth";
import { timingSafeEqual } from "crypto";
import { defineSecret } from "firebase-functions/params";

export const functionsInternalKey = defineSecret("FUNCTIONS_INTERNAL_KEY");

function safeEqual(left: string, right: string): boolean {
  const a = Buffer.from(left);
  const b = Buffer.from(right);
  return a.length === b.length && timingSafeEqual(a, b);
}

export async function requireAuth(
  req: any,
  res: any,
  expectedUid?: string,
  requireAdmin = false
): Promise<boolean> {
  const internalKey = runCatchingSecret();
  const suppliedInternalKey = String(req.get("x-internal-api-key") || "");
  if (internalKey && suppliedInternalKey && safeEqual(internalKey, suppliedInternalKey)) return true;

  const authorization = String(req.get("authorization") || "");
  if (!authorization.startsWith("Bearer ")) {
    res.status(401).json({error: "Authentication required"});
    return false;
  }

  try {
    const token = await getAuth().verifyIdToken(authorization.slice(7), true);
    if (requireAdmin && token.admin !== true) {
      res.status(403).json({error: "Administrator access required"});
      return false;
    }
    if (expectedUid && token.uid !== expectedUid && token.admin !== true) {
      res.status(403).json({error: "Access denied"});
      return false;
    }
    return true;
  } catch {
    res.status(401).json({error: "Invalid or expired token"});
    return false;
  }
}

function runCatchingSecret(): string {
  try {
    return functionsInternalKey.value();
  } catch {
    return "";
  }
}
