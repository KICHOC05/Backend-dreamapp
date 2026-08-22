import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

const { apiMock } = vi.hoisted(() => ({
  apiMock: {
    health: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    verify: vi.fn(),
    logout: vi.fn(),
    stats: vi.fn(),
    recommendation: vi.fn(),
    predictions: vi.fn(),
    subscription: vi.fn(),
    changePlan: vi.fn(),
  },
}));

vi.mock("./api", () => {
  class MockApiError extends Error {
    constructor(
      message: string,
      public status: number,
    ) {
      super(message);
    }
  }

  return {
    API_URL: "https://api.example.test",
    ApiError: MockApiError,
    api: apiMock,
  };
});

const sessionUser = {
  id: "user-1",
  userName: "ana",
  fullname: "Ana Sueño",
  role: "USER",
  active: true,
};

const emptyStats = {
  efficiencyChart: {
    last7Days: [],
    lastMonth: [],
    last6Months: [],
    lastYear: [],
  },
  qualityPie: { lastMonth: {} },
  averagesLast7Days: {
    sleepEfficiency: 0,
    sleepDuration: 0,
    light: 0,
    deep: 0,
    rem: 0,
    awake: 0,
    avgHR: 0,
    awakenings: 0,
  },
  lastDayStats: {
    sleepEfficiency: 0,
    sleepDuration: 0,
    light: 0,
    deep: 0,
    rem: 0,
    awake: 0,
    avgHR: 0,
    awakenings: 0,
  },
};

describe("DreamApp", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    apiMock.health.mockResolvedValue({ status: "ok" });
    apiMock.login.mockResolvedValue({ data: sessionUser });
    apiMock.register.mockResolvedValue({ message: "Código enviado" });
    apiMock.verify.mockResolvedValue({ success: true });
    apiMock.logout.mockResolvedValue({ success: true });
    apiMock.stats.mockResolvedValue({ success: true, data: emptyStats });
    apiMock.predictions.mockResolvedValue({
      success: true,
      nextMonthPredictions: [],
    });
    apiMock.recommendation.mockResolvedValue({
      success: true,
      recommendation: "Mantén un horario constante.",
    });
    apiMock.subscription.mockResolvedValue({ success: true, plan: "FREE" });
    apiMock.changePlan.mockResolvedValue({
      success: true,
      plan: "PLUS",
      message: "Plan actualizado",
    });
  });

  it("muestra la portada y abre el inicio de sesión", async () => {
    const user = userEvent.setup();
    render(<App />);

    expect(
      screen.getByRole("heading", { name: /Entiende tu sueño/i }),
    ).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Ingresar" }));

    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeInTheDocument();
    await waitFor(() => expect(apiMock.health).toHaveBeenCalledOnce());
  });

  it("inicia sesión y permite recorrer las funciones autenticadas", async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole("button", { name: "Ingresar" }));
    await user.type(screen.getByLabelText("Usuario"), "ana");
    await user.type(screen.getByLabelText(/^Contraseña/), "Password123");
    await user.click(screen.getByRole("button", { name: /Ingresar al panel/ }));

    expect(
      await screen.findByRole("heading", { name: "Resumen del sueño" }),
    ).toBeInTheDocument();
    expect(apiMock.login).toHaveBeenCalledWith("ana", "Password123");

    await user.click(screen.getByRole("button", { name: /Análisis IA/ }));
    await user.click(screen.getByRole("button", { name: /Analizar con IA/ }));
    expect(
      await screen.findByText("Mantén un horario constante."),
    ).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /Suscripción/ }));
    await user.click(
      screen.getAllByRole("button", { name: "Cambiar a este plan" })[0],
    );
    expect(await screen.findByText("Plan actualizado")).toBeInTheDocument();
  });

  it("presenta errores de autenticación sin entrar al panel", async () => {
    const { ApiError } = await import("./api");
    apiMock.login.mockRejectedValue(new ApiError("No autorizado", 401));
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole("button", { name: "Ingresar" }));
    await user.type(screen.getByLabelText("Usuario"), "ana");
    await user.type(screen.getByLabelText("Contraseña"), "incorrecta");
    await user.click(screen.getByRole("button", { name: /Ingresar al panel/ }));

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent("Usuario, contraseña o perfil incorrectos.");
    expect(
      screen.queryByRole("heading", { name: "Resumen del sueño" }),
    ).not.toBeInTheDocument();
  });

  it("completa el registro y la verificación por código", async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(
      screen.getAllByRole("button", { name: "Crear cuenta" })[0],
    );
    await user.type(screen.getByLabelText("Nombre"), "Ana");
    await user.type(screen.getByLabelText("Apellido"), "Sueño");
    await user.type(screen.getByLabelText("Usuario"), "ana");
    await user.type(
      screen.getByLabelText("Correo electrónico"),
      "ana@example.com",
    );
    await user.type(screen.getByLabelText(/^Contraseña/), "Password123");
    await user.click(screen.getByRole("button", { name: /Enviar código/ }));

    expect(await screen.findByText("Código enviado")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Código de 6 dígitos"), "123456");
    await user.click(
      screen.getByRole("button", { name: "Verificar y crear cuenta" }),
    );

    await waitFor(() =>
      expect(apiMock.verify).toHaveBeenCalledWith("ana@example.com", "123456"),
    );
    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeInTheDocument();
  });
});
