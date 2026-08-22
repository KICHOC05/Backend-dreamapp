import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, api, clearSession } from "./api";

const fetchMock = vi.fn<typeof fetch>();

describe("cliente de DreamApp API", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
    sessionStorage.clear();
  });

  it("autentica, envía JSON y conserva temporalmente el token", async () => {
    fetchMock.mockResolvedValue(
      new Response(
        JSON.stringify({
          success: true,
          data: {
            id: "user-1",
            userName: "ana",
            fullname: "Ana Sueño",
            role: "USER",
            active: true,
          },
          token: "temporary-token",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    const result = await api.login("ana", "Password123");

    expect(result.data.userName).toBe("ana");
    expect(sessionStorage.getItem("dreamapp_session")).toBe("temporary-token");
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/auth\/login$/),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ userName: "ana", password: "Password123" }),
      }),
    );
  });

  it("adjunta el bearer token y codifica identificadores de usuario", async () => {
    sessionStorage.setItem("dreamapp_session", "session-token");
    fetchMock.mockResolvedValue(
      new Response(JSON.stringify({ success: true, data: {} }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await api.stats("user/with spaces");

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/sleep\/stats\?uid=user%2Fwith%20spaces$/),
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer session-token",
        }),
      }),
    );
  });

  it("convierte respuestas fallidas en ApiError con el mensaje del servidor", async () => {
    fetchMock.mockResolvedValue(
      new Response(JSON.stringify({ error: "Credenciales inválidas" }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      }),
    );

    const error = await api.login("ana", "incorrecta").catch((reason) => reason);

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      message: "Credenciales inválidas",
      status: 401,
    });
  });

  it("normaliza errores de conectividad", async () => {
    fetchMock.mockRejectedValue(new TypeError("network unavailable"));

    const error = await api.health().catch((reason) => reason);

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      message: "No se pudo conectar con DreamApp API.",
      status: 0,
    });
  });

  it("elimina explícitamente la sesión", () => {
    sessionStorage.setItem("dreamapp_session", "session-token");

    clearSession();

    expect(sessionStorage.getItem("dreamapp_session")).toBeNull();
  });
});
