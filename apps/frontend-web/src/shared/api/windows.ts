import type { MetricsWindow } from "./types";

export const windowOptions: MetricsWindow[] = [
  "5m",
  "15m",
  "1h",
  "3h",
  "6h",
  "12h",
  "1d",
];

const windowToSecondsMap: Record<MetricsWindow, number> = {
  "5m": 5 * 60,
  "15m": 15 * 60,
  "1h": 60 * 60,
  "3h": 3 * 60 * 60,
  "6h": 6 * 60 * 60,
  "12h": 12 * 60 * 60,
  "1d": 24 * 60 * 60,
};

export const windowToSeconds = (window: MetricsWindow) =>
  windowToSecondsMap[window];
