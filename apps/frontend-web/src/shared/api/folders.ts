import axios from "axios";
import type { BaseResponse, FolderResponse } from "./types";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080",
});

export const fetchFolders = async () => {
  const { data } = await api.get<BaseResponse<FolderResponse[]>>("/api/folders");
  if (!data.susses) {
    throw new Error("Failed to load folders");
  }
  return data.body ?? [];
};

export const createFolder = async (payload: { name: string }) => {
  await api.post("/api/folders", payload);
};

export const updateFolder = async (payload: {
  name: string;
  endpoints: string[];
  newName?: string;
}) => {
  await api.put("/api/folders", payload);
};

export const deleteFolder = async (payload: { name: string }) => {
  await api.delete("/api/folders", { data: payload });
};
