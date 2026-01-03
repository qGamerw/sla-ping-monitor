"use client";

import * as React from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import dayjs from "dayjs";
import {
  fetchEndpoint,
  fetchEndpointChecks,
  fetchEndpointStats,
  updateEndpoint,
} from "../../../shared/api/endpoints";
import type {
  CheckResultResponse,
  EndpointRequest,
  EndpointResponse,
  MetricsWindow,
  StatsResponse,
} from "../../../shared/api/types";
import { windowOptions, windowToSeconds } from "../../../shared/api/windows";

const resolveWindow = (value: string | null): MetricsWindow =>
  windowOptions.includes(value as MetricsWindow) ? (value as MetricsWindow) : "1h";

const resolveRefresh = (value: string | null): number | null => {
  if (!value) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

export const useEndpointDetails = () => {
  const pathParams = useParams<{ id: string }>();
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialWindow = resolveWindow(searchParams.get("window"));
  const initialRefresh = resolveRefresh(searchParams.get("refresh"));

  const [windowValue, setWindowValue] = React.useState<MetricsWindow>(
    initialWindow,
  );
  const [endpoint, setEndpoint] = React.useState<EndpointResponse | null>(null);
  const [stats, setStats] = React.useState<StatsResponse | null>(null);
  const [checks, setChecks] = React.useState<CheckResultResponse[]>([]);
  const [refreshSec, setRefreshSec] = React.useState<number | null>(
    initialRefresh,
  );
  const [range, setRange] = React.useState<{
    from: dayjs.Dayjs;
    to: dayjs.Dayjs;
  } | null>(null);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = React.useState(false);

  const loadData = React.useCallback(async () => {
    if (!pathParams.id) return;
    setLoading(true);
    setError(null);
    try {
      const windowSec = windowToSeconds(windowValue);
      const to = dayjs();
      const from = to.subtract(windowSec, "second");
      const [endpointData, statsData, checksData] = await Promise.all([
        fetchEndpoint(pathParams.id),
        fetchEndpointStats(pathParams.id, windowSec),
        fetchEndpointChecks(pathParams.id, from.toISOString(), to.toISOString()),
      ]);
      setEndpoint(endpointData ?? null);
      setStats(statsData ?? null);
      setChecks(checksData);
      setRange({ from, to });
    } catch (err) {
      setError("Не удалось загрузить данные по endpoint.");
    } finally {
      setLoading(false);
    }
  }, [pathParams.id, windowValue]);

  React.useEffect(() => {
    const paramWindow = resolveWindow(searchParams.get("window"));
    const paramRefresh = resolveRefresh(searchParams.get("refresh"));
    let shouldReload = true;

    if (paramWindow !== windowValue) {
      setWindowValue(paramWindow);
      shouldReload = false;
    }
    if (paramRefresh !== refreshSec) {
      setRefreshSec(paramRefresh);
      shouldReload = false;
    }
    if (shouldReload) {
      void loadData();
    }
  }, [loadData, refreshSec, searchParams, windowValue]);

  React.useEffect(() => {
    if (!refreshSec) return;
    const intervalId = globalThis.setInterval(() => {
      void loadData();
    }, refreshSec * 1000);
    return () => globalThis.clearInterval(intervalId);
  }, [loadData, refreshSec]);

  const handleWindowChange = (value: MetricsWindow) => {
    setWindowValue(value);
    const query = new URLSearchParams(searchParams.toString());
    query.set("window", value);
    router.replace(`/endpoints/${pathParams.id}?${query.toString()}`);
  };

  const handleRefreshChange = (value: string) => {
    const nextValue = value === "" ? null : Number(value);
    setRefreshSec(nextValue);
    const query = new URLSearchParams(searchParams.toString());
    if (nextValue) {
      query.set("refresh", String(nextValue));
    } else {
      query.delete("refresh");
    }
    router.replace(`/endpoints/${pathParams.id}?${query.toString()}`);
  };

  const handleEdit = () => {
    if (!endpoint) return;
    setDialogOpen(true);
  };

  const handleSave = async (draft: EndpointRequest) => {
    if (!endpoint) return;
    try {
      await updateEndpoint(endpoint.id, draft);
      setEndpoint((prev) =>
        prev
          ? {
              ...prev,
              ...draft,
              tags: draft.tags ?? null,
              headers: draft.headers ?? null,
            }
          : prev,
      );
      setDialogOpen(false);
    } catch (err) {
      setError("Не удалось сохранить endpoint. Проверьте данные формы.");
    }
  };

  const handleToggle = async () => {
    if (!endpoint) return;
    try {
      const payload: EndpointRequest = {
        name: endpoint.name,
        url: endpoint.url,
        method: endpoint.method,
        headers: endpoint.headers ?? null,
        timeoutMs: endpoint.timeoutMs,
        expectedStatus: endpoint.expectedStatus,
        intervalSec: endpoint.intervalSec,
        enabled: !endpoint.enabled,
        tags: endpoint.tags ?? null,
      };
      await updateEndpoint(endpoint.id, payload);
      setEndpoint((prev) => (prev ? { ...prev, enabled: !prev.enabled } : prev));
    } catch (err) {
      setError("Не удалось обновить состояние endpoint.");
    }
  };

  const backLink = `/?window=${windowValue}${
    refreshSec ? `&refresh=${refreshSec}` : ""
  }`;

  return {
    endpoint,
    stats,
    checks,
    range,
    windowValue,
    refreshSec,
    loading,
    error,
    dialogOpen,
    backLink,
    setDialogOpen,
    handleWindowChange,
    handleRefreshChange,
    handleEdit,
    handleSave,
    handleToggle,
  };
};
