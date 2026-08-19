import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  API_URL,
  ApiError,
  api,
  type Prediction,
  type SleepStats,
  type User,
  type UserInfo,
} from "./api";
type View = "overview" | "analysis" | "subscription";
const Logo = () => (
  <div className="logo" aria-hidden="true">
    ☾
  </div>
);

function Landing({ enter, register }: { enter: () => void; register: () => void }) {
  return (
    <main className="landing">
      <nav className="topnav">
        <a className="brand" href="#inicio">
          <Logo />
          <b>DreamApp</b>
        </a>
        <div className="navlinks">
          <a href="#beneficios">Beneficios</a>
          <a href="#como">Cómo funciona</a>
          <a href="#planes">Planes</a>
          <button className="button ghost" onClick={enter}>
            Ingresar
          </button>
          <button className="button primary" onClick={register}>Crear cuenta</button>
        </div>
      </nav>
      <section className="hero" id="inicio">
        <div className="hero-copy">
          <div className="eyebrow">Tecnología para dormir mejor</div>
          <h1>
            Entiende tu sueño.
            <br />
            <em>Transforma tus días.</em>
          </h1>
          <p>
            Convierte los datos de tu wearable en métricas claras, tendencias
            útiles y recomendaciones personalizadas con inteligencia artificial.
          </p>
          <div className="hero-actions">
            <button className="button primary" onClick={enter}>
              Abrir mi panel →
            </button>
            <button className="button ghost" onClick={register}>Crear cuenta gratis</button>
            <a href="#como">Descubrir cómo funciona</a>
          </div>
          <div className="trust">
            <span>✓ Monitoreo continuo</span>
            <span>✓ Análisis con IA</span>
            <span>✓ Datos protegidos</span>
          </div>
        </div>
        <div className="hero-visual">
          <div className="orbit one" />
          <div className="orbit two" />
          <div className="moon">☾</div>
          <div className="float-card score">
            <small>Calidad del sueño</small>
            <strong>
              87<span>%</span>
            </strong>
            <div className="meter">
              <i style={{ width: "87%" }} />
            </div>
            <b>Excelente descanso</b>
          </div>
          <div className="float-card pulse">
            <span>♥</span>
            <div>
              <small>Pulso promedio</small>
              <strong>62 bpm</strong>
            </div>
          </div>
          <div className="stars">✦　·　✧　　·　✦</div>
        </div>
      </section>
      <section className="benefits" id="beneficios">
        <div>
          <small>TODO EN UN SOLO LUGAR</small>
          <h2>Tu descanso, explicado con claridad</h2>
        </div>
        <div className="feature-grid">
          <Feature icon="◷" title="Métricas precisas">
            Duración, eficiencia, fases, ritmo cardiaco y despertares.
          </Feature>
          <Feature icon="⌁" title="Tendencias visuales">
            Identifica cambios semanales y mensuales fácilmente.
          </Feature>
          <Feature icon="✦" title="Recomendaciones con IA">
            Consejos personalizados a partir de registros reales.
          </Feature>
        </div>
      </section>
      <section className="how" id="como">
        <div>
          <small>DE TU RELOJ A TU PANEL</small>
          <h2>Cómo funciona DreamApp</h2>
        </div>
        <ol>
          <li>
            <b>01</b>
            <span>
              <strong>Registra</strong>Tu wearable mide pulso, movimiento y
              fases.
            </span>
          </li>
          <li>
            <b>02</b>
            <span>
              <strong>Sincroniza</strong>La app móvil envía tus sesiones de
              manera segura.
            </span>
          </li>
          <li>
            <b>03</b>
            <span>
              <strong>Comprende</strong>Consulta métricas y recomendaciones
              desde cualquier lugar.
            </span>
          </li>
        </ol>
      </section>
      <section className="pricing" id="planes">
        <div className="pricing-heading">
          <small>PLANES PARA CADA ETAPA</small>
          <h2>Empieza gratis y crece cuando lo necesites</h2>
          <p>Sin cargos ocultos. Podrás cambiar de plan desde tu cuenta.</p>
        </div>
        <PricingGrid actionLabel="Elegir plan" onChoose={enter} />
      </section>
      <footer>
        <span>DreamApp · Monitoreo inteligente del sueño</span>
        <span>Proyecto académico IoT 2025</span>
      </footer>
    </main>
  );
}

const planData = [
  { id: "FREE", name: "Gratis", price: "$0", note: "Para conocer DreamApp", features: ["1 perfil", "Resumen semanal", "Métricas esenciales"] },
  { id: "PLUS", name: "Plus", price: "$99", note: "MXN al mes", featured: true, features: ["Hasta 5 perfiles", "Análisis con IA", "Predicción mensual", "Historial ampliado"] },
  { id: "PRO", name: "Profesional", price: "$249", note: "MXN al mes", features: ["Perfiles ilimitados", "Panel para especialistas", "Reportes avanzados", "Soporte prioritario"] },
];

function PricingGrid({actionLabel,onChoose,current,busy}:{actionLabel:string;onChoose:(plan:string)=>void;current?:string;busy?:boolean}) {
  return <div className="pricing-grid">{planData.map(plan=><article className={`price-card ${plan.featured?'featured':''} ${current===plan.id?'current':''}`} key={plan.id}>
    {plan.featured&&<span className="popular">MÁS ELEGIDO</span>}
    <h3>{plan.name}</h3><div className="price">{plan.price}<small>{plan.id==='FREE'?' para siempre':' / mes'}</small></div><p>{plan.note}</p>
    <ul>{plan.features.map(item=><li key={item}>✓ {item}</li>)}</ul>
    <button className={`button ${plan.featured?'primary':'ghost'} wide`} disabled={busy||current===plan.id} onClick={()=>onChoose(plan.id)}>{current===plan.id?'Plan actual':actionLabel}</button>
  </article>)}</div>;
}
function Feature({
  icon,
  title,
  children,
}: {
  icon: string;
  title: string;
  children: string;
}) {
  return (
    <article>
      <i>{icon}</i>
      <h3>{title}</h3>
      <p>{children}</p>
    </article>
  );
}

function Login({
  back,
  success,
}: {
  back: () => void;
  success: (u: UserInfo) => void;
}) {
  const [userName, setUser] = useState("");
  const [password, setPass] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [online, setOnline] = useState<boolean | null>(null);
  useEffect(() => {
    api
      .health()
      .then(() => setOnline(true))
      .catch(() => setOnline(false));
  }, []);
  async function submit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      success((await api.login(userName.trim(), password)).data);
    } catch (err) {
      setError(
        err instanceof ApiError && err.status === 401
          ? "Usuario, contraseña o perfil incorrectos."
          : err instanceof Error
            ? err.message
            : "No fue posible iniciar sesión.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <main className="auth-page">
      <button className="back" onClick={back}>
        ← Volver
      </button>
      <section className="auth-intro">
        <Logo />
        <div className="eyebrow">Bienvenido de vuelta</div>
        <h1>Tu descanso cuenta una historia.</h1>
        <p>
          Accede al panel clínico para consultar usuarios, métricas y análisis
          inteligente.
        </p>
        <div className="privacy">
          <b>◉ Sesión protegida</b>
          <span>
            Las credenciales viajan cifradas y usamos una sesión temporal revocable.
          </span>
        </div>
      </section>
      <form className="auth-card" onSubmit={submit}>
        <div className="status">
          <i
            className={online ? "online" : online === false ? "offline" : ""}
          />
          {online === null
            ? "Comprobando servicio…"
            : online
              ? "DreamApp API disponible"
              : "El servidor está iniciando"}
        </div>
        <h2>Iniciar sesión</h2>
        <p>Ingresa con tu cuenta de DreamApp.</p>
        <label>
          Usuario
          <input
            autoComplete="username"
            value={userName}
            onChange={(e) => setUser(e.target.value)}
            required
            maxLength={80}
            placeholder="Tu usuario"
          />
        </label>
        <label>
          Contraseña
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPass(e.target.value)}
            required
            minLength={6}
            placeholder="••••••••"
          />
        </label>
        {error && (
          <div className="error" role="alert">
            {error}
          </div>
        )}
        <button className="button primary wide" disabled={busy}>
          {busy ? "Ingresando…" : "Ingresar al panel →"}
        </button>
        <small className="api-caption">
          Conectado a {API_URL.replace("https://", "")}
        </small>
      </form>
    </main>
  );
}

function Register({ back, done }: { back: () => void; done: () => void }) {
  const [step, setStep] = useState<"details" | "code">("details");
  const [form, setForm] = useState({ firstName: "", lastName: "", userName: "", email: "", password: "" });
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  async function submitDetails(e: FormEvent) {
    e.preventDefault(); setBusy(true); setError(""); setMessage("");
    try { const result = await api.register(form); setMessage(result.message); setStep("code"); }
    catch (err) { setError(err instanceof Error ? err.message : "No se pudo crear la cuenta."); }
    finally { setBusy(false); }
  }
  async function submitCode(e: FormEvent) {
    e.preventDefault(); setBusy(true); setError("");
    try { await api.verify(form.email, code); done(); }
    catch (err) { setError(err instanceof Error ? err.message : "No se pudo verificar el código."); }
    finally { setBusy(false); }
  }
  return <main className="auth-page register-page">
    <button className="back" onClick={back}>← Volver</button>
    <section className="auth-intro"><Logo/><div className="eyebrow">TU CUENTA PERSONAL</div><h1>Tus métricas, solo para ti.</h1><p>Crea tu cuenta y confirma tu correo. Cada persona accede exclusivamente a su propio historial de sueño.</p><div className="privacy"><b>◉ Privacidad desde el registro</b><span>El código caduca en 10 minutos y nunca almacenamos el código original.</span></div></section>
    {step === "details" ? <form className="auth-card" onSubmit={submitDetails}>
      <h2>Crear cuenta</h2><p>Todos los campos son obligatorios.</p>
      <div className="field-pair"><label>Nombre<input value={form.firstName} onChange={e=>setForm({...form,firstName:e.target.value})} required minLength={2} maxLength={100}/></label><label>Apellido<input value={form.lastName} onChange={e=>setForm({...form,lastName:e.target.value})} required minLength={2} maxLength={100}/></label></div>
      <label>Usuario<input autoComplete="username" value={form.userName} onChange={e=>setForm({...form,userName:e.target.value})} required minLength={3} maxLength={40}/></label>
      <label>Correo electrónico<input type="email" autoComplete="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} required maxLength={254}/></label>
      <label>Contraseña<input type="password" autoComplete="new-password" value={form.password} onChange={e=>setForm({...form,password:e.target.value})} required minLength={10} maxLength={72}/><small className="field-help">10 caracteres, mayúscula, minúscula y número.</small></label>
      {error&&<div className="error" role="alert">{error}</div>}<button className="button primary wide" disabled={busy}>{busy?"Enviando…":"Enviar código →"}</button>
    </form> : <form className="auth-card verify-card" onSubmit={submitCode}>
      <div className="mail-icon">✉</div><h2>Revisa tu correo</h2><p>{message}<br/><b>{form.email}</b></p>
      <label>Código de 6 dígitos<input className="code-input" inputMode="numeric" autoComplete="one-time-code" value={code} onChange={e=>setCode(e.target.value.replace(/\D/g,"").slice(0,6))} required pattern="[0-9]{6}" maxLength={6}/></label>
      {error&&<div className="error" role="alert">{error}</div>}
      <button className="button primary wide" disabled={busy||code.length!==6}>{busy?"Verificando…":"Verificar y crear cuenta"}</button>
      <button type="button" className="text-button" onClick={()=>setStep("details")}>Cambiar datos o reenviar código</button>
    </form>}
  </main>;
}

function Chart({
  points,
}: {
  points: { date: string; sleepEfficiency: number }[];
}) {
  if (!points.length)
    return (
      <div className="empty-chart">Aún no hay registros para graficar.</div>
    );
  const vals = points.map((p) => p.sleepEfficiency),
    min = Math.min(...vals, 50),
    max = Math.max(...vals, 100),
    coords = points
      .map(
        (p, i) =>
          `${(i / Math.max(points.length - 1, 1)) * 100},${92 - ((p.sleepEfficiency - min) / (max - min || 1)) * 74}`,
      )
      .join(" ");
  return (
    <div className="chart">
      <svg viewBox="0 0 100 100" preserveAspectRatio="none">
        <defs>
          <linearGradient id="area" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor="#4fd1c5" stopOpacity=".35" />
            <stop offset="1" stopColor="#4fd1c5" stopOpacity="0" />
          </linearGradient>
        </defs>
        <polygon points={`0,100 ${coords} 100,100`} fill="url(#area)" />
        <polyline
          points={coords}
          fill="none"
          stroke="#4fd1c5"
          strokeWidth="2.6"
          vectorEffect="non-scaling-stroke"
        />
      </svg>
      <div className="chart-labels">
        <span>{points[0]?.date}</span>
        <span>{points.at(-1)?.date}</span>
      </div>
    </div>
  );
}

function Dashboard({ session, exit }: { session: UserInfo; exit: () => void }) {
  const [view, setView] = useState<View>("overview"),
    [users] = useState<User[]>([{ uidUser: session.id, username: session.fullname || session.userName, weightKg: 0, heightCm: 0, age: 0, sex: "" }]),
    [selected] = useState(session.id),
    [stats, setStats] = useState<SleepStats | null>(null),
    [prediction, setPrediction] = useState<Prediction[]>([]),
    [advice, setAdvice] = useState(""),
    [plan, setPlan] = useState("FREE"),
    [planMessage, setPlanMessage] = useState(""),
    [planBusy, setPlanBusy] = useState(false),
    [busy] = useState(false),
    [error, setError] = useState("");
  useEffect(() => {
    api.subscription().then((result) => setPlan(result.plan)).catch(() => undefined);
  }, []);
  useEffect(() => {
    if (!selected) return;
    setStats(null);
    setPrediction([]);
    setAdvice("");
    Promise.allSettled([api.stats(selected), api.predictions(selected)]).then(
      ([s, p]) => {
        if (s.status === "fulfilled") setStats(s.value.data);
        if (p.status === "fulfilled")
          setPrediction(p.value.nextMonthPredictions);
      },
    );
  }, [selected]);
  const patient = useMemo(
      () => users.find((u) => u.uidUser === selected),
      [users, selected],
    ),
    avg = stats?.averagesLast7Days,
    last = stats?.lastDayStats;
  async function analyze() {
    setAdvice("Generando una recomendación personalizada…");
    try {
      setAdvice(
        (await api.recommendation(selected)).recommendation ||
          "No hay suficientes registros.",
      );
    } catch (e) {
      setAdvice(
        e instanceof Error ? e.message : "No se pudo generar la recomendación.",
      );
    }
  }
  async function logout() {
    try {
      await api.logout();
    } finally {
      exit();
    }
  }
  async function changePlan(nextPlan: string) {
    setPlanBusy(true); setPlanMessage("");
    try { const result=await api.changePlan(nextPlan); setPlan(result.plan); setPlanMessage(result.message); }
    catch(e) { setPlanMessage(e instanceof Error?e.message:"No se pudo actualizar el plan."); }
    finally { setPlanBusy(false); }
  }
  return (
    <div className="shell">
      <aside>
        <a className="brand side-brand" href="#">
          <Logo />
          <b>DreamApp</b>
        </a>
        <nav>
          {(
            [
              ["overview", "⌂", "Resumen"],
              ["analysis", "⌁", "Análisis IA"],
              ["subscription", "◇", "Suscripción"],
            ] as [View, string, string][]
          ).map(([id, icon, label]) => (
            <button
              key={id}
              className={view === id ? "active" : ""}
              onClick={() => setView(id)}
            >
              <i>{icon}</i>
              {label}
            </button>
          ))}
        </nav>
        <div className="account">
          <div className="avatar">
            {(session.fullname || session.userName)[0]}
          </div>
          <div>
            <b>{session.fullname || session.userName}</b>
            <small>{session.role}</small>
          </div>
          <button title="Cerrar sesión" onClick={logout}>
            ↪
          </button>
        </div>
      </aside>
      <main className="dashboard">
        <header>
          <div>
            <small>PANEL DE MONITOREO</small>
            <h1>
              {view === "overview"
                ? "Resumen del sueño"
                : view === "analysis"
                    ? "Análisis inteligente"
                    : view === "subscription"
                      ? "Mejorar suscripción"
                      : "Resumen del sueño"}
            </h1>
          </div>
        </header>
        {error && <div className="error">{error}</div>}
        {busy ? (
          <div className="loading">Preparando tu panel…</div>
        ) : view === "overview" ? (
          <>
            <section className="welcome">
              <div>
                <small>VISTA GENERAL</small>
                <h2>
                  {patient
                    ? `Buenas noches, ${patient.username}`
                    : "Bienvenido a DreamApp"}
                </h2>
                <p>Panorama del descanso más reciente registrado.</p>
              </div>
              <div className="sleep-score">
                <strong>
                  {last?.sleepEfficiency ?? "—"}
                  <span>%</span>
                </strong>
                <small>Eficiencia última noche</small>
              </div>
            </section>
            <section className="metric-grid">
              <Metric
                label="Duración"
                value={minutes(last?.sleepDuration)}
                detail="Última sesión"
                icon="◷"
              />
              <Metric
                label="Pulso promedio"
                value={last ? `${last.avgHR} bpm` : "—"}
                detail="Durante el sueño"
                icon="♥"
              />
              <Metric
                label="Sueño profundo"
                value={minutes(last?.deep)}
                detail="Recuperación física"
                icon="◒"
              />
              <Metric
                label="Despertares"
                value={last?.awakenings?.toString() ?? "—"}
                detail="Última sesión"
                icon="☼"
              />
            </section>
            <section className="two-cols">
              <article className="panel">
                <div className="panel-title">
                  <div>
                    <small>ÚLTIMOS 7 DÍAS</small>
                    <h3>Eficiencia del sueño</h3>
                  </div>
                  <b>{avg?.sleepEfficiency ?? "—"}%</b>
                </div>
                <Chart points={stats?.efficiencyChart.last7Days ?? []} />
              </article>
              <article className="panel phases">
                <small>PROMEDIO SEMANAL</small>
                <h3>Distribución por fases</h3>
                <Phase
                  label="Ligero"
                  value={avg?.light}
                  total={avg?.sleepDuration}
                  color="#7b8cff"
                />
                <Phase
                  label="Profundo"
                  value={avg?.deep}
                  total={avg?.sleepDuration}
                  color="#5264d8"
                />
                <Phase
                  label="REM"
                  value={avg?.rem}
                  total={avg?.sleepDuration}
                  color="#c084fc"
                />
                <Phase
                  label="Despierto"
                  value={avg?.awake}
                  total={avg?.sleepDuration}
                  color="#f5b971"
                />
              </article>
            </section>
          </>
        ) : view === "analysis" ? (
          <section className="analysis-grid">
            <article className="panel prediction">
              <small>PRÓXIMO MES</small>
              <h3>Predicción de eficiencia</h3>
              <Chart points={prediction} />
              <div className="prediction-list">
                {prediction.slice(0, 4).map((p) => (
                  <span key={p.date}>
                    <small>{p.date}</small>
                    <b>{Math.round(p.sleepEfficiency)}%</b>
                  </span>
                ))}
              </div>
            </article>
            <article className="panel ai-card">
              <div className="ai-icon">✦</div>
              <small>ASISTENTE DREAM AI</small>
              <h3>Recomendación personalizada</h3>
              <p>
                {advice ||
                  "Analiza las noches recientes y genera sugerencias concretas para mejorar el descanso."}
              </p>
              <button className="button primary" onClick={analyze}>
                ✦ Analizar con IA
              </button>
              <small>Orientativa; no sustituye atención médica.</small>
            </article>
          </section>
        ) : (
          <section className="subscription-view">
            <div className="subscription-intro"><div><small>PLAN ACTUAL</small><h2>{planData.find(p=>p.id===plan)?.name||plan}</h2><p>Elige el nivel que mejor se adapte a tu uso de DreamApp.</p></div><span className="plan-badge">{plan}</span></div>
            {planMessage&&<div className="success-message">{planMessage}</div>}
            <PricingGrid actionLabel="Cambiar a este plan" current={plan} busy={planBusy} onChoose={changePlan}/>
            <p className="payment-note">La selección actualiza tu suscripción. El cobro automático se habilitará cuando conectemos una pasarela de pago.</p>
          </section>
        )}
      </main>
    </div>
  );
}
function Metric({
  label,
  value,
  detail,
  icon,
}: {
  label: string;
  value: string;
  detail: string;
  icon: string;
}) {
  return (
    <article className="metric">
      <i>{icon}</i>
      <div>
        <small>{label}</small>
        <strong>{value}</strong>
        <span>{detail}</span>
      </div>
    </article>
  );
}
function Phase({
  label,
  value = 0,
  total = 0,
  color,
}: {
  label: string;
  value?: number;
  total?: number;
  color: string;
}) {
  const pct = Math.min(100, Math.round((value / (total || 1)) * 100));
  return (
    <div className="phase">
      <div>
        <span>{label}</span>
        <b>
          {minutes(value)} · {pct}%
        </b>
      </div>
      <div>
        <i style={{ width: `${pct}%`, background: color }} />
      </div>
    </div>
  );
}
function minutes(v?: number) {
  return v === undefined ? "—" : `${Math.floor(v / 60)}h ${v % 60}m`;
}
export default function App() {
  const [screen, setScreen] = useState<"landing" | "login" | "register" | "dashboard">(
      "landing",
    ),
    [session, setSession] = useState<UserInfo | null>(null);
  if (screen === "landing") return <Landing enter={() => setScreen("login")} register={() => setScreen("register")} />;
  if (screen === "register") return <Register back={() => setScreen("landing")} done={() => setScreen("login")} />;
  if (screen === "login" || !session)
    return (
      <Login
        back={() => setScreen("landing")}
        success={(u) => {
          setSession(u);
          setScreen("dashboard");
        }}
      />
    );
  return (
    <Dashboard
      session={session}
      exit={() => {
        setSession(null);
        setScreen("login");
      }}
    />
  );
}
