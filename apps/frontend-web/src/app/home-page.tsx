"use client";

import * as React from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import dayjs from "dayjs";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Container,
  Divider,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import EndpointFormDialog from "../components/EndpointFormDialog";
import StatusChip, { type Status } from "../components/StatusChip";
import {
  createEndpoint,
  deleteEndpoint,
  fetchEndpoints,
  fetchEndpointSummary,
  updateEndpoint,
} from "./lib/api";
import type {
  EndpointRequest,
  EndpointResponse,
  EndpointSummaryResponse,
  MetricsWindow,
} from "./lib/apiTypes";
import { windowOptions, windowToSeconds } from "./lib/windows";

interface EndpointRow extends EndpointResponse {
  summary?: EndpointSummaryResponse;
}

const formatMs = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(0)} ms`;
const formatPercent = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(1)}%`;

const mapStatus = (
  summary: EndpointSummaryResponse | undefined,
  enabled: boolean,
): Status => {
  if (!enabled) return "OFF";
  if (!summary) return "DEGRADED";
  if (summary.lastSuccess === true) return "OK";
  if (summary.lastSuccess === false) return "DOWN";
  return "DEGRADED";
};

const buildRequest = (endpoint: EndpointResponse): EndpointRequest => ({
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

export default function HomePageClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const resolveWindow = (value: string | null): MetricsWindow =>
    windowOptions.includes(value as MetricsWindow)
      ? (value as MetricsWindow)
      : "15m";
  const resolveRefresh = (value: string | null): number | null => {
    if (!value) return null;
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  };
  const initialWindow = resolveWindow(searchParams.get("window"));
  const initialRefresh = resolveRefresh(searchParams.get("refresh"));
  const [windowValue, setWindowValue] = React.useState<MetricsWindow>(
    initialWindow,
  );
  const [endpoints, setEndpoints] = React.useState<EndpointRow[]>([]);
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<EndpointResponse | null>(null);
  const [query, setQuery] = React.useState("");
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [refreshSec, setRefreshSec] = React.useState<number | null>(
    initialRefresh,
  );

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
      await deleteEndpoint(id);
      await loadData();
    } catch (err) {
      setError("Не удалось удалить endpoint.");
    }
  };

  const handleToggle = async (endpoint: EndpointResponse) => {
    try {
      if (endpoint.timeoutMs === 0 && endpoint.intervalSec === 0) {
        setError(
          "Недостаточно данных для обновления endpoint. Откройте его карточку.",
        );
        return;
      }
      await updateEndpoint(endpoint.id, {
        ...buildRequest(endpoint),
        enabled: !endpoint.enabled,
      });
      await loadData();
    } catch (err) {
      setError("Не удалось обновить состояние endpoint.");
    }
  };

  return (
    <Box component="main" sx={{ pb: 6 }}>
      <Container maxWidth="lg" sx={{ pt: 4 }}>
        <Stack spacing={3}>
          <Stack
            direction={{ xs: "column", md: "row" }}
            spacing={2}
            alignItems={{ xs: "flex-start", md: "center" }}
            justifyContent="space-between"
          >
            <Box>
              <Typography variant="h4" gutterBottom>
                Endpoints
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Управляйте мониторингом сервисов и смотрите ключевые метрики.
              </Typography>
            </Box>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleCreate}
            >
              Создать endpoint
            </Button>
          </Stack>

          {error && <Alert severity="error">{error}</Alert>}

          <Card>
            <CardContent>
              <Stack
                direction={{ xs: "column", md: "row" }}
                spacing={2}
                alignItems={{ xs: "flex-start", md: "center" }}
                justifyContent="space-between"
              >
                <Stack spacing={1}>
                  <Typography variant="h6">Окно метрик</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Все значения p95/p99 и error rate рассчитаны для выбранного
                    окна.
                  </Typography>
                </Stack>
                <Stack direction="row" spacing={2} alignItems="center">
                  <FormControl size="small" sx={{ minWidth: 140 }}>
                    <InputLabel id="window-select">Окно</InputLabel>
                    <Select
                      labelId="window-select"
                      label="Окно"
                      value={windowValue}
                      onChange={(event) =>
                        handleWindowChange(event.target.value as MetricsWindow)
                      }
                    >
                      {windowOptions.map((item) => (
                        <MenuItem key={item} value={item}>
                          {item}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  <FormControl size="small" sx={{ minWidth: 160 }}>
                    <InputLabel id="refresh-select">Refresh</InputLabel>
                    <Select
                      labelId="refresh-select"
                      label="Refresh"
                      value={refreshSec ?? ""}
                      onChange={(event) =>
                        handleRefreshChange(String(event.target.value))
                      }
                    >
                      <MenuItem value="">Не обновлять</MenuItem>
                      <MenuItem value={15}>15 секунд</MenuItem>
                      <MenuItem value={30}>30 секунд</MenuItem>
                      <MenuItem value={60}>1 минута</MenuItem>
                      <MenuItem value={300}>5 минут</MenuItem>
                    </Select>
                  </FormControl>
                </Stack>
              </Stack>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Stack
                direction={{ xs: "column", md: "row" }}
                spacing={2}
                alignItems={{ xs: "stretch", md: "center" }}
                justifyContent="space-between"
              >
                <TextField
                  label="Поиск endpoint"
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  size="small"
                />
                <Stack direction="row" spacing={1} alignItems="center">
                  <Chip
                    label={`${filteredEndpoints.length} endpoints`}
                    color="primary"
                    variant="outlined"
                  />
                  <Chip
                    label={`${filteredEndpoints.filter((item) => item.enabled).length} активных`}
                    color="success"
                    variant="outlined"
                  />
                </Stack>
              </Stack>
              <Divider sx={{ my: 2 }} />
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Endpoint</TableCell>
                    <TableCell>Статус</TableCell>
                    <TableCell>p50</TableCell>
                    <TableCell>p95</TableCell>
                    <TableCell>p99</TableCell>
                    <TableCell>Error rate</TableCell>
                    <TableCell>Последняя проверка</TableCell>
                    <TableCell align="right">Действия</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredEndpoints.map((endpoint) => {
                    const metrics = endpoint.summary?.windowStats;
                    return (
                      <TableRow key={endpoint.id} hover>
                        <TableCell>
                          <Stack spacing={0.5}>
                            <Stack direction="row" spacing={1} alignItems="center">
                              <Typography variant="subtitle2">
                                {endpoint.name}
                              </Typography>
                              {!endpoint.enabled && (
                                <Chip label="Paused" size="small" />
                              )}
                            </Stack>
                            <Typography variant="caption" color="text.secondary">
                              {endpoint.method} {endpoint.url}
                            </Typography>
                            <Stack direction="row" spacing={1}>
                              {(endpoint.tags ?? []).map((tag) => (
                                <Chip
                                  key={tag}
                                  label={tag}
                                  size="small"
                                  variant="outlined"
                                />
                              ))}
                            </Stack>
                          </Stack>
                        </TableCell>
                        <TableCell>
                          <StatusChip
                            status={mapStatus(endpoint.summary, endpoint.enabled)}
                          />
                        </TableCell>
                        <TableCell>{formatMs(metrics?.p50)}</TableCell>
                        <TableCell>{formatMs(metrics?.p95)}</TableCell>
                        <TableCell>{formatMs(metrics?.p99)}</TableCell>
                        <TableCell>{formatPercent(metrics?.errorRate)}</TableCell>
                        <TableCell>
                          {endpoint.summary?.lastCheckAt
                            ? dayjs(endpoint.summary.lastCheckAt).format("HH:mm:ss")
                            : "—"}
                        </TableCell>
                        <TableCell align="right">
                          <Stack direction="row" spacing={1} justifyContent="flex-end">
                            <Tooltip title="Вкл/выкл">
                              <Switch
                                size="small"
                                checked={endpoint.enabled}
                                onChange={() => handleToggle(endpoint)}
                              />
                            </Tooltip>
                            <Tooltip title="Открыть">
                              <IconButton
                                component={Link}
                                href={`/endpoints/${endpoint.id}?window=${windowValue}${
                                  refreshSec ? `&refresh=${refreshSec}` : ""
                                }`}
                                size="small"
                              >
                                <OpenInNewIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Редактировать">
                              <IconButton
                                onClick={() => handleEdit(endpoint)}
                                size="small"
                              >
                                <EditIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Удалить">
                              <IconButton
                                onClick={() => handleDelete(endpoint.id)}
                                size="small"
                                color="error"
                              >
                                <DeleteIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </Stack>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
              {loading && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                  Загрузка данных...
                </Typography>
              )}
            </CardContent>
          </Card>
        </Stack>
      </Container>

      <EndpointFormDialog
        open={dialogOpen}
        availableTags={availableTags}
        initial={editing}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
    </Box>
  );
}
