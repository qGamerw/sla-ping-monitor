import axios from "axios";
import type {
  BaseResponse,
  CheckResultResponse,
  EndpointRequest,
  EndpointResponse,
  EndpointSummaryResponse,
  StatsResponse,
} from "./types";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080",
});

export const fetchEndpoints = async () => {
  const { data } = await api.get<BaseResponse<EndpointResponse[]>>(
    "/api/endpoints",
  );
  if (!data.susses) {
    throw new Error("Failed to load endpoints");
  }
  return data.body ?? [];
};

export const fetchEndpoint = async (id: string) => {
  const { data } = await api.get<BaseResponse<EndpointResponse>>(
    `/api/endpoints/${id}`,
  );
  if (!data.susses) {
    throw new Error("Failed to load endpoint");
  }
  return data.body;
};

export const createEndpoint = async (payload: EndpointRequest) => {
  await api.post("/api/endpoints", payload);
};

export const updateEndpoint = async (id: string, payload: EndpointRequest) => {
  await api.put(`/api/endpoints/${id}`, payload);
};

export const deleteEndpoint = async (id: string) => {
  await api.delete(`/api/endpoints/${id}`);
};

export const fetchEndpointSummary = async (windowSec: number) => {
  const { data } = await api.get<BaseResponse<EndpointSummaryResponse[]>>(
    "/api/endpoints/summary",
    { params: { windowSec } },
  );
  if (!data.susses) {
    throw new Error("Failed to load endpoint summary");
  }
  return data.body ?? [];
};

export const fetchEndpointStats = async (id: string, windowSec: number) => {
  const { data } = await api.get<BaseResponse<StatsResponse>>(
    `/api/endpoints/${id}/stats`,
    { params: { windowSec } },
  );
  if (!data.susses) {
    throw new Error("Failed to load endpoint stats");
  }
  return data.body;
};

export const fetchEndpointChecks = async (
  id: string,
  from: string,
  to: string,
) => {
  const { data } = await api.get<BaseResponse<CheckResultResponse[]>>(
    `/api/endpoints/${id}/checks`,
    { params: { from, to } },
  );
  if (!data.susses) {
    throw new Error("Failed to load endpoint checks");
  }
  return data.body ?? [];
};
