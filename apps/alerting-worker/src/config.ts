export type WorkerConfig = {
  backendApiBaseUrl: string;
};

export const loadConfig = (): WorkerConfig => ({
  backendApiBaseUrl: process.env.BACKEND_API_BASE_URL ?? "http://localhost:8080",
});
