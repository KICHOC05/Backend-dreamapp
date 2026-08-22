import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, api } from "./api";

const fetchMock = vi.fn<typeof fetch>();

describe("cliente de DreamApp API", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
    sessionStorage.clear();
  });

  it("autentica con cookies HttpOnly sin almacenar el token en JavaScript", async () => {
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
          expiresIn: 43200,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    const result = await api.login("ana", "Password123");

    expect(result.data.userName).toBe("ana");
    expect(sessionStorage).toHaveLength(0);
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/auth\/login$/),
      expect.objectContaining({
        method: "POST",
        credentials: "include",
        body: JSON.stringify({ userName: "ana", password: "Password123" }),
        headers: expect.objectContaining({
          "X-DreamApp-Request": "DreamAppWeb",
        }),
      }),
    );
  });

  it("incluye credenciales de cookie y codifica identificadores de usuario", async () => {
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
        credentials: "include",
        headers: expect.objectContaining({
          "X-DreamApp-Request": "DreamAppWeb",
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

  it("recupera la sesión autenticada desde la cookie del servidor", async () => {
    fetchMock.mockResolvedValue(
      new Response(JSON.stringify({ success: true, data: { id: "user-1" } }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    const result = await api.session();

    expect(result.data.id).toBe("user-1");
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/auth\/session$/),
      expect.objectContaining({ credentials: "include" }),
    );
  });
});
