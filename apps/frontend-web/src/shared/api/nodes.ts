import axios from "axios";
import type { BaseResponse, NodesResponse } from "./types";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080",
});

export const fetchNodes = async () => {
  const { data } = await api.get<BaseResponse<NodesResponse>>("/api/nodes");
  if (!data.susses) {
    throw new Error("Failed to load nodes");
  }
  return data.body ?? null;
};
