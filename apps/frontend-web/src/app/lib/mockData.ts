import dayjs from "dayjs";

export type Status = "OK" | "DEGRADED" | "DOWN";

export type MetricsWindow =
  | "5m"
  | "15m"
  | "1h"
  | "3h"
  | "6h"
  | "12h"
  | "1d";

export interface EndpointMetrics {
  p95: number;
  p99: number;
  errorRate: number;
  lastCheck: string;
}

export interface EndpointSummary {
  id: string;
  name: string;
  url: string;
  method: string;
  headers: Record<string, string>;
  timeoutMs: number;
  expectedStatus: number[];
  intervalSec: number;
  status: Status;
  enabled: boolean;
  tags: string[];
  metrics: Record<MetricsWindow, EndpointMetrics>;
}

const now = dayjs();

export const windowOptions: MetricsWindow[] = [
  "5m",
  "15m",
  "1h",
  "3h",
  "6h",
  "12h",
  "1d",
];

export const initialEndpoints: EndpointSummary[] = [
  {
    id: "payments-api",
    name: "Payments API",
    url: "https://api.example.com/payments/health",
    method: "GET",
    headers: {
      accept: "text/plain",
    },
    timeoutMs: 3000,
    expectedStatus: [200, 399],
    intervalSec: 5,
    status: "OK",
    enabled: true,
    tags: ["payments", "critical"],
    metrics: {
      "5m": {
        p95: 120,
        p99: 190,
        errorRate: 0.4,
        lastCheck: now.subtract(45, "second").toISOString(),
      },
      "15m": {
        p95: 135,
        p99: 210,
        errorRate: 0.6,
        lastCheck: now.subtract(2, "minute").toISOString(),
      },
      "1h": {
        p95: 150,
        p99: 235,
        errorRate: 0.8,
        lastCheck: now.subtract(3, "minute").toISOString(),
      },
      "3h": {
        p95: 165,
        p99: 250,
        errorRate: 1.2,
        lastCheck: now.subtract(4, "minute").toISOString(),
      },
      "6h": {
        p95: 172,
        p99: 265,
        errorRate: 1.5,
        lastCheck: now.subtract(5, "minute").toISOString(),
      },
      "12h": {
        p95: 181,
        p99: 280,
        errorRate: 1.7,
        lastCheck: now.subtract(6, "minute").toISOString(),
      },
      "1d": {
        p95: 195,
        p99: 310,
        errorRate: 2.1,
        lastCheck: now.subtract(8, "minute").toISOString(),
      },
    },
  },
  {
    id: "auth-service",
    name: "Auth Service",
    url: "https://auth.example.com/healthz",
    method: "GET",
    headers: {
      accept: "text/plain",
    },
    timeoutMs: 3000,
    expectedStatus: [200, 399],
    intervalSec: 10,
    status: "DEGRADED",
    enabled: true,
    tags: ["auth", "sso"],
    metrics: {
      "5m": {
        p95: 340,
        p99: 520,
        errorRate: 3.4,
        lastCheck: now.subtract(2, "minute").toISOString(),
      },
      "15m": {
        p95: 310,
        p99: 480,
        errorRate: 2.9,
        lastCheck: now.subtract(4, "minute").toISOString(),
      },
      "1h": {
        p95: 290,
        p99: 430,
        errorRate: 2.1,
        lastCheck: now.subtract(6, "minute").toISOString(),
      },
      "3h": {
        p95: 275,
        p99: 400,
        errorRate: 1.8,
        lastCheck: now.subtract(8, "minute").toISOString(),
      },
      "6h": {
        p95: 260,
        p99: 370,
        errorRate: 1.4,
        lastCheck: now.subtract(10, "minute").toISOString(),
      },
      "12h": {
        p95: 240,
        p99: 350,
        errorRate: 1.1,
        lastCheck: now.subtract(12, "minute").toISOString(),
      },
      "1d": {
        p95: 230,
        p99: 330,
        errorRate: 1,
        lastCheck: now.subtract(14, "minute").toISOString(),
      },
    },
  },
  {
    id: "inventory",
    name: "Inventory",
    url: "https://inventory.example.com/ping",
    method: "POST",
    headers: {
      accept: "application/json",
    },
    timeoutMs: 5000,
    expectedStatus: [200, 299],
    intervalSec: 30,
    status: "DOWN",
    enabled: false,
    tags: ["warehouse"],
    metrics: {
      "5m": {
        p95: 980,
        p99: 1400,
        errorRate: 18.3,
        lastCheck: now.subtract(12, "minute").toISOString(),
      },
      "15m": {
        p95: 820,
        p99: 1200,
        errorRate: 14.7,
        lastCheck: now.subtract(18, "minute").toISOString(),
      },
      "1h": {
        p95: 640,
        p99: 980,
        errorRate: 9.2,
        lastCheck: now.subtract(30, "minute").toISOString(),
      },
      "3h": {
        p95: 520,
        p99: 760,
        errorRate: 5.8,
        lastCheck: now.subtract(40, "minute").toISOString(),
      },
      "6h": {
        p95: 450,
        p99: 700,
        errorRate: 4.2,
        lastCheck: now.subtract(50, "minute").toISOString(),
      },
      "12h": {
        p95: 400,
        p99: 640,
        errorRate: 3.5,
        lastCheck: now.subtract(60, "minute").toISOString(),
      },
      "1d": {
        p95: 360,
        p99: 600,
        errorRate: 2.9,
        lastCheck: now.subtract(70, "minute").toISOString(),
      },
    },
  },
];

export interface ChartPoint {
  time: string;
  value: number;
}

export const latencySeries: Record<MetricsWindow, ChartPoint[]> = {
  "5m": [
    { time: "-5m", value: 120 },
    { time: "-4m", value: 140 },
    { time: "-3m", value: 180 },
    { time: "-2m", value: 160 },
    { time: "-1m", value: 150 },
  ],
  "15m": [
    { time: "-15m", value: 180 },
    { time: "-12m", value: 210 },
    { time: "-9m", value: 190 },
    { time: "-6m", value: 220 },
    { time: "-3m", value: 205 },
  ],
  "1h": [
    { time: "-1h", value: 240 },
    { time: "-48m", value: 210 },
    { time: "-36m", value: 260 },
    { time: "-24m", value: 230 },
    { time: "-12m", value: 250 },
  ],
  "3h": [
    { time: "-3h", value: 260 },
    { time: "-2.5h", value: 240 },
    { time: "-2h", value: 280 },
    { time: "-1.5h", value: 300 },
    { time: "-1h", value: 270 },
  ],
  "6h": [
    { time: "-6h", value: 280 },
    { time: "-5h", value: 310 },
    { time: "-4h", value: 290 },
    { time: "-3h", value: 330 },
    { time: "-2h", value: 300 },
  ],
  "12h": [
    { time: "-12h", value: 310 },
    { time: "-10h", value: 320 },
    { time: "-8h", value: 340 },
    { time: "-6h", value: 315 },
    { time: "-4h", value: 330 },
  ],
  "1d": [
    { time: "-24h", value: 340 },
    { time: "-20h", value: 325 },
    { time: "-16h", value: 360 },
    { time: "-12h", value: 345 },
    { time: "-8h", value: 355 },
  ],
};

export const errorSeries: Record<MetricsWindow, ChartPoint[]> = {
  "5m": [
    { time: "-5m", value: 1 },
    { time: "-4m", value: 2 },
    { time: "-3m", value: 0 },
    { time: "-2m", value: 3 },
    { time: "-1m", value: 1 },
  ],
  "15m": [
    { time: "-15m", value: 4 },
    { time: "-12m", value: 2 },
    { time: "-9m", value: 1 },
    { time: "-6m", value: 3 },
    { time: "-3m", value: 2 },
  ],
  "1h": [
    { time: "-1h", value: 5 },
    { time: "-48m", value: 4 },
    { time: "-36m", value: 2 },
    { time: "-24m", value: 3 },
    { time: "-12m", value: 1 },
  ],
  "3h": [
    { time: "-3h", value: 6 },
    { time: "-2.5h", value: 5 },
    { time: "-2h", value: 4 },
    { time: "-1.5h", value: 3 },
    { time: "-1h", value: 2 },
  ],
  "6h": [
    { time: "-6h", value: 7 },
    { time: "-5h", value: 5 },
    { time: "-4h", value: 4 },
    { time: "-3h", value: 6 },
    { time: "-2h", value: 3 },
  ],
  "12h": [
    { time: "-12h", value: 8 },
    { time: "-10h", value: 7 },
    { time: "-8h", value: 6 },
    { time: "-6h", value: 5 },
    { time: "-4h", value: 4 },
  ],
  "1d": [
    { time: "-24h", value: 9 },
    { time: "-20h", value: 8 },
    { time: "-16h", value: 6 },
    { time: "-12h", value: 7 },
    { time: "-8h", value: 5 },
  ],
};
