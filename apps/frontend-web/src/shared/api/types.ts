export type MetricsWindow = "5m" | "15m" | "1h" | "3h" | "6h" | "12h" | "1d";

export interface ApiError {
  message: unknown;
}

export interface BaseResponse<T> {
  susses: boolean;
  body?: T;
  error?: ApiError;
}

export interface EndpointRequest {
  name: string;
  url: string;
  method: string;
  headers?: Record<string, string> | null;
  timeoutMs: number;
  expectedStatus: number[];
  intervalSec: number;
  enabled: boolean;
  tags?: string[] | null;
}

export interface EndpointResponse extends EndpointRequest {
  id: string;
  nextRunAt?: string | null;
  leaseOwner?: string | null;
  leaseUntil?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StatsResponse {
  sampleCount: number;
  p50?: number | null;
  p95?: number | null;
  p99?: number | null;
  avg?: number | null;
  min?: number | null;
  max?: number | null;
  errorRate?: number | null;
  lastStatus?: number | null;
  insufficientSamples: boolean;
}

export interface CheckResultResponse {
  id: string;
  endpointId: string;
  startedAt: string;
  finishedAt: string;
  latencyMs: number;
  statusCode?: number | null;
  success: boolean;
  errorType?: string | null;
  errorMessage?: string | null;
}

export interface EndpointSummaryResponse {
  id: string;
  name: string;
  url: string;
  enabled: boolean;
  lastCheckAt?: string | null;
  lastStatusCode?: number | null;
  lastSuccess?: boolean | null;
  windowStats?: StatsResponse;
}

export interface FolderResponse {
  name: string;
  endpoints: string[];
}

export interface NodesResponse {
  cpuUsed: string;
  ramUsed: string;
}
