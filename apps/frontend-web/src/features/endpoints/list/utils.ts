import type {
  EndpointRequest,
  EndpointResponse,
  EndpointSummaryResponse,
} from "../../../shared/api/types";
import type { Status } from "../components/StatusChip";

export const formatMs = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(0)} ms`;

export const formatPercent = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(1)}%`;

export const mapStatus = (
  summary: EndpointSummaryResponse | undefined,
  enabled: boolean,
): Status => {
  if (!enabled) return "OFF";
  if (!summary) return "DEGRADED";
  if (summary.lastSuccess === true) return "OK";
  if (summary.lastSuccess === false) return "DOWN";
  return "DEGRADED";
};

export const buildRequest = (endpoint: EndpointResponse): EndpointRequest => ({
  name: endpoint.name,
  url: endpoint.url,
  method: endpoint.method,
  headers: endpoint.headers ?? null,
  timeoutMs: endpoint.timeoutMs,
  expectedStatus: endpoint.expectedStatus,
  intervalSec: endpoint.intervalSec,
  enabled: endpoint.enabled,
  tags: endpoint.tags ?? null,
});
