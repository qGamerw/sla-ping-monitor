"use client";

import * as React from "react";
import { useRouter, useSearchParams } from "next/navigation";
import dayjs from "dayjs";
import {
  createEndpoint,
  deleteEndpoint,
  fetchEndpoints,
  fetchEndpointSummary,
  updateEndpoint,
} from "../../../shared/api/endpoints";
import type {
  EndpointRequest,
  EndpointResponse,
  MetricsWindow,
} from "../../../shared/api/types";
import { windowOptions, windowToSeconds } from "../../../shared/api/windows";
import type { EndpointRow } from "./types";
import { buildRequest } from "./utils";

const resolveWindow = (value: string | null): MetricsWindow =>
  windowOptions.includes(value as MetricsWindow) ? (value as MetricsWindow) : "15m";

const resolveRefresh = (value: string | null): number | null => {
  if (!value) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

export const useEndpointsList = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialWindow = resolveWindow(searchParams.get("window"));
  const initialRefresh = resolveRefresh(searchParams.get("refresh"));

  const [windowValue, setWindowValue] = React.useState<MetricsWindow>(
    initialWindow,
  );
  const [refreshSec, setRefreshSec] = React.useState<number | null>(
    initialRefresh,
  );
  const [endpoints, setEndpoints] = React.useState<EndpointRow[]>([]);
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [bulkDialogOpen, setBulkDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<EndpointResponse | null>(null);
  const [query, setQuery] = React.useState("");
  const [bulkCount, setBulkCount] = React.useState(1);
  const [selectedIds, setSelectedIds] = React.useState<string[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const availableTags = React.useMemo(
    () =>
      Array.from(
        new Set(endpoints.flatMap((item) => item.tags ?? [])),
      ).sort(),
    [endpoints],
  );

  const filteredEndpoints = endpoints.filter((endpoint) =>
    endpoint.name.toLowerCase().includes(query.toLowerCase()),
  );
  const selectedCount = selectedIds.length;
  const allSelected =
    filteredEndpoints.length > 0 && selectedIds.length === filteredEndpoints.length;
  const someSelected =
    selectedIds.length > 0 && selectedIds.length < filteredEndpoints.length;

  const loadData = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const windowSec = windowToSeconds(windowValue);
      const [endpointsResult, summaryResult] = await Promise.allSettled([
        fetchEndpoints(),
        fetchEndpointSummary(windowSec),
      ]);

      const summaryList =
        summaryResult.status === "fulfilled" ? summaryResult.value : [];
      const summaryMap = new Map(
        summaryList.map((summary) => [summary.id, summary]),
      );

      if (endpointsResult.status === "fulfilled") {
        setEndpoints(
          endpointsResult.value.map((endpoint) => ({
            ...endpoint,
            summary: summaryMap.get(endpoint.id),
          })),
        );
        if (summaryResult.status === "rejected") {
          setError("Не удалось загрузить сводные метрики.");
        }
      } else if (summaryList.length > 0) {
        setEndpoints(
          summaryList.map((summary) => ({
            id: summary.id,
            name: summary.name,
            url: summary.url,
            method: "GET",
            headers: null,
            timeoutMs: 0,
            expectedStatus: [],
            intervalSec: 0,
            enabled: summary.enabled,
            tags: null,
            createdAt: new Date(0).toISOString(),
            updatedAt: new Date(0).toISOString(),
            summary,
          })),
        );
        setError("Не удалось загрузить конфигурации endpoint, показаны сводки.");
      } else {
        setError("Не удалось загрузить данные. Проверьте соединение с API.");
      }
    } finally {
      setLoading(false);
    }
  }, [windowValue]);

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
    const params = new URLSearchParams(searchParams.toString());
    params.set("window", value);
    router.replace(`/?${params.toString()}`);
  };

  const handleRefreshChange = (value: string) => {
    const nextValue = value === "" ? null : Number(value);
    setRefreshSec(nextValue);
    const params = new URLSearchParams(searchParams.toString());
    if (nextValue) {
      params.set("refresh", String(nextValue));
    } else {
      params.delete("refresh");
    }
    router.replace(`/?${params.toString()}`);
  };

  const handleCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const handleBulkOpen = () => {
    setBulkCount(1);
    setBulkDialogOpen(true);
  };

  const handleEdit = (endpoint: EndpointResponse) => {
    if (endpoint.timeoutMs === 0 && endpoint.intervalSec === 0) {
      setError(
        "Недоступно редактирование без полной конфигурации endpoint. Обновите данные.",
      );
      return;
    }
    setEditing(endpoint);
    setDialogOpen(true);
  };

  const handleSave = async (draft: EndpointRequest) => {
    try {
      if (editing) {
        await updateEndpoint(editing.id, draft);
        setEndpoints((prev) =>
          prev.map((endpoint) =>
            endpoint.id === editing.id
              ? {
                  ...endpoint,
                  ...draft,
                  tags: draft.tags ?? null,
                  headers: draft.headers ?? null,
                }
              : endpoint,
          ),
        );
        setDialogOpen(false);
        return;
      }
      await createEndpoint(draft);
      setDialogOpen(false);
      await loadData();
    } catch (err) {
      setError("Не удалось сохранить endpoint. Проверьте данные формы.");
    }
  };

  const handleBulkSave = async (draft: EndpointRequest) => {
    if (bulkCount <= 0) {
      setError("Количество для быстрого создания должно быть больше нуля.");
      return;
    }
    try {
      const baseName = draft.name?.trim() || `Endpoint ${dayjs().format("HHmmss")}`;
      const requests = Array.from({ length: bulkCount }, (_, index) => ({
        ...draft,
        name: bulkCount > 1 ? `${baseName} #${index + 1}` : baseName,
      }));
      for (const request of requests) {
        await createEndpoint(request);
      }
      setBulkDialogOpen(false);
      await loadData();
    } catch (err) {
      setError("Не удалось создать endpoints.");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const confirmed = window.confirm("Удалить этот endpoint?");
      if (!confirmed) return;
      await deleteEndpoint(id);
      setSelectedIds((prev) => prev.filter((item) => item !== id));
      await loadData();
    } catch (err) {
      setError("Не удалось удалить endpoint.");
    }
  };

  const handleToggle = async (endpoint: EndpointResponse) => {
    try {
      if (endpoint.timeoutMs === 0 && endpoint.intervalSec === 0) {
        setError("Недостаточно данных для обновления endpoint. Откройте его карточку.");
        return;
      }
      await updateEndpoint(endpoint.id, {
        ...buildRequest(endpoint),
        enabled: !endpoint.enabled,
      });
      setEndpoints((prev) =>
        prev.map((item) =>
          item.id === endpoint.id ? { ...item, enabled: !item.enabled } : item,
        ),
      );
    } catch (err) {
      setError("Не удалось обновить состояние endpoint.");
    }
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(filteredEndpoints.map((endpoint) => endpoint.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectRow = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id],
    );
  };

  const handleBulkToggle = async (enabled: boolean) => {
    try {
      const targets = endpoints.filter((endpoint) =>
        selectedIds.includes(endpoint.id),
      );
      await Promise.all(
        targets.map((endpoint) =>
          updateEndpoint(endpoint.id, { ...buildRequest(endpoint), enabled }),
        ),
      );
      setEndpoints((prev) =>
        prev.map((endpoint) =>
          selectedIds.includes(endpoint.id) ? { ...endpoint, enabled } : endpoint,
        ),
      );
      setSelectedIds([]);
    } catch (err) {
      setError("Не удалось обновить выбранные endpoints.");
    }
  };

  const handleBulkDelete = async () => {
    if (selectedIds.length === 0) return;
    const confirmed = window.confirm("Удалить выбранные endpoints?");
    if (!confirmed) return;
    try {
      await Promise.all(selectedIds.map((id) => deleteEndpoint(id)));
      setEndpoints((prev) =>
        prev.filter((endpoint) => !selectedIds.includes(endpoint.id)),
      );
      setSelectedIds([]);
    } catch (err) {
      setError("Не удалось удалить выбранные endpoints.");
    }
  };

  return {
    windowValue,
    refreshSec,
    endpoints,
    filteredEndpoints,
    availableTags,
    dialogOpen,
    bulkDialogOpen,
    editing,
    query,
    bulkCount,
    selectedIds,
    selectedCount,
    allSelected,
    someSelected,
    loading,
    error,
    setQuery,
    setBulkCount,
    handleWindowChange,
    handleRefreshChange,
    handleCreate,
    handleBulkOpen,
    handleEdit,
    handleSave,
    handleBulkSave,
    handleDelete,
    handleToggle,
    handleSelectAll,
    handleSelectRow,
    handleBulkToggle,
    handleBulkDelete,
    setDialogOpen,
    setBulkDialogOpen,
  };
};
