"use client";

import * as React from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
  createEndpoint,
  deleteEndpoint,
  fetchEndpoints,
  fetchEndpointSummary,
  updateEndpoint,
} from "../../../shared/api/endpoints";
import {
  createFolder,
  deleteFolder,
  fetchFolders,
  updateFolder,
} from "../../../shared/api/folders";
import type {
  EndpointRequest,
  EndpointResponse,
  FolderResponse,
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
  const [folders, setFolders] = React.useState<FolderResponse[]>([]);
  const [selectedFolder, setSelectedFolder] = React.useState("all");
  const [folderDialogOpen, setFolderDialogOpen] = React.useState(false);
  const [editingFolder, setEditingFolder] = React.useState<FolderResponse | null>(
    null,
  );
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<EndpointResponse | null>(null);
  const [query, setQuery] = React.useState("");
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

  const activeFolder = React.useMemo(
    () => folders.find((folder) => folder.name === selectedFolder) ?? null,
    [folders, selectedFolder],
  );

  const filteredEndpoints = endpoints.filter((endpoint) => {
    if (!endpoint.name.toLowerCase().includes(query.toLowerCase())) {
      return false;
    }
    if (selectedFolder === "all") return true;
    return activeFolder?.endpoints.includes(endpoint.id) ?? false;
  });
  const selectedCount = selectedIds.length;
  const allSelected =
    filteredEndpoints.length > 0 && selectedIds.length === filteredEndpoints.length;
  const someSelected =
    selectedIds.length > 0 && selectedIds.length < filteredEndpoints.length;

  const loadData = React.useCallback(async () => {
    setLoading(true);
    try {
      const windowSec = windowToSeconds(windowValue);
      const [endpointsResult, summaryResult, foldersResult] =
        await Promise.allSettled([
        fetchEndpoints(),
        fetchEndpointSummary(windowSec),
        fetchFolders(),
      ]);

      const errorMessages: string[] = [];

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
          errorMessages.push("Не удалось загрузить сводные метрики.");
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
        errorMessages.push(
          "Не удалось загрузить конфигурации endpoint, показаны сводки.",
        );
      } else {
        errorMessages.push(
          "Не удалось загрузить данные. Проверьте соединение с API.",
        );
      }

      if (foldersResult.status === "fulfilled") {
        setFolders(foldersResult.value);
      } else {
        errorMessages.push("Не удалось загрузить список папок.");
      }

      setError(errorMessages.length > 0 ? errorMessages.join(" ") : null);
    } finally {
      setLoading(false);
    }
  }, [windowValue]);

  const handleRefreshNow = React.useCallback(() => {
    void loadData();
  }, [loadData]);

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

  React.useEffect(() => {
    if (selectedFolder === "all") return;
    if (!folders.some((folder) => folder.name === selectedFolder)) {
      setSelectedFolder("all");
    }
  }, [folders, selectedFolder]);

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

  const handleDuplicate = async (endpoint: EndpointResponse) => {
    try {
      if (endpoint.timeoutMs === 0 && endpoint.intervalSec === 0) {
        setError("Недостаточно данных для копирования endpoint. Откройте его карточку.");
        return;
      }
      await createEndpoint({
        ...buildRequest(endpoint),
        name: `${endpoint.name} (копия)`,
      });
      await loadData();
    } catch (err) {
      setError("Не удалось создать копию endpoint.");
    }
  };

  const handleTagsChange = async (
    endpoint: EndpointResponse,
    nextTags: string[],
  ) => {
    try {
      if (endpoint.timeoutMs === 0 && endpoint.intervalSec === 0) {
        setError("Недостаточно данных для обновления endpoint. Откройте его карточку.");
        return;
      }
      await updateEndpoint(endpoint.id, {
        ...buildRequest(endpoint),
        tags: nextTags.length > 0 ? nextTags : null,
      });
      setEndpoints((prev) =>
        prev.map((item) =>
          item.id === endpoint.id
            ? { ...item, tags: nextTags.length > 0 ? nextTags : null }
            : item,
        ),
      );
    } catch (err) {
      setError("Не удалось обновить теги endpoint.");
    }
  };

  const handleFolderSelect = (value: string) => {
    setSelectedFolder(value);
    setSelectedIds([]);
  };

  const handleFolderCreate = () => {
    setEditingFolder(null);
    setFolderDialogOpen(true);
  };

  const handleFolderEdit = () => {
    const target = folders.find((folder) => folder.name === selectedFolder);
    if (!target) return;
    setEditingFolder(target);
    setFolderDialogOpen(true);
  };

  const handleFolderSave = async (draft: { name: string; endpoints: string[] }) => {
    try {
      if (editingFolder) {
        await updateFolder({
          name: editingFolder.name,
          endpoints: draft.endpoints,
          newName: draft.name,
        });
        setFolders((prev) =>
          prev.map((folder) =>
            folder.name === editingFolder.name
              ? { name: draft.name, endpoints: draft.endpoints }
              : folder,
          ),
        );
        await loadData();
      } else {
        await createFolder({ name: draft.name });
        setFolders((prev) => [
          ...prev.filter((folder) => folder.name !== draft.name),
          { name: draft.name, endpoints: draft.endpoints },
        ]);
        if (draft.endpoints.length > 0) {
          await updateFolder({
            name: draft.name,
            endpoints: draft.endpoints,
            newName: draft.name,
          });
        }
        await loadData();
      }
      setFolderDialogOpen(false);
      setEditingFolder(null);
      setSelectedFolder(draft.name);
    } catch (err) {
      setError("Не удалось сохранить папку.");
    }
  };

  const handleFolderDelete = async () => {
    const target = editingFolder?.name ?? selectedFolder;
    if (!target || target === "all") return;
    const confirmed = window.confirm("Удалить эту папку?");
    if (!confirmed) return;
    try {
      await deleteFolder({ name: target });
      setFolderDialogOpen(false);
      setEditingFolder(null);
      setSelectedFolder("all");
      await loadData();
    } catch (err) {
      setError("Не удалось удалить папку.");
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
    folders,
    selectedFolder,
    folderDialogOpen,
    editingFolder,
    activeFolder,
    filteredEndpoints,
    availableTags,
    dialogOpen,
    editing,
    query,
    selectedIds,
    selectedCount,
    allSelected,
    someSelected,
    loading,
    error,
    setQuery,
    handleWindowChange,
    handleRefreshChange,
    handleRefreshNow,
    handleCreate,
    handleEdit,
    handleSave,
    handleDelete,
    handleToggle,
    handleDuplicate,
    handleTagsChange,
    handleFolderSelect,
    handleFolderCreate,
    handleFolderEdit,
    handleFolderSave,
    handleFolderDelete,
    handleSelectAll,
    handleSelectRow,
    handleBulkToggle,
    handleBulkDelete,
    setDialogOpen,
    setFolderDialogOpen,
  };
};
