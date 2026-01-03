"use client";

import * as React from "react";
import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import dayjs from "dayjs";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Divider,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { BarChart, LineChart } from "@mui/x-charts";
import StatusChip, { type Status } from "../../../components/StatusChip";
import {
  fetchEndpoint,
  fetchEndpointChecks,
  fetchEndpointStats,
} from "../../lib/api";
import type {
  CheckResultResponse,
  EndpointResponse,
  MetricsWindow,
  StatsResponse,
} from "../../lib/apiTypes";
import { windowOptions, windowToSeconds } from "../../lib/windows";

const formatMs = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(0)} ms`;
const formatPercent = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(1)}%`;

const mapStatus = (stats?: StatsResponse): Status => {
  if (!stats || stats.lastStatus === null || stats.lastStatus === undefined) {
    return "DEGRADED";
  }
  if (stats.lastStatus >= 200 && stats.lastStatus < 400) {
    return "OK";
  }
  return "DOWN";
};

const formatChartTime = (value: string) => dayjs(value).format("HH:mm");

const buildBuckets = (
  checks: CheckResultResponse[],
  from: dayjs.Dayjs,
  to: dayjs.Dayjs,
  bucketCount: number,
) => {
  const windowMs = to.diff(from, "millisecond");
  const bucketMs = windowMs / bucketCount;
  const buckets = Array.from({ length: bucketCount }, (_, index) => ({
    start: from.add(bucketMs * index, "millisecond"),
    latencies: [] as number[],
    errors: 0,
  }));

  checks.forEach((check) => {
    const timestamp = dayjs(check.startedAt);
    if (timestamp.isBefore(from) || timestamp.isAfter(to)) return;
    const position = Math.min(
      Math.floor(timestamp.diff(from, "millisecond") / bucketMs),
      bucketCount - 1,
    );
    buckets[position].latencies.push(check.latencyMs);
    if (!check.success) {
      buckets[position].errors += 1;
    }
  });

  return buckets.map((bucket) => ({
    time: formatChartTime(bucket.start.toISOString()),
    latency:
      bucket.latencies.length > 0
        ? bucket.latencies.reduce((sum, value) => sum + value, 0) /
          bucket.latencies.length
        : null,
    errors: bucket.errors,
  }));
};

export default function EndpointDetailsPage() {
  const pathParams = useParams<{ id: string }>();
  const router = useRouter();
  const searchParams = useSearchParams();
  const resolveWindow = (value: string | null): MetricsWindow =>
    windowOptions.includes(value as MetricsWindow) ? (value as MetricsWindow) : "1h";
  const initialWindow = resolveWindow(searchParams.get("window"));
  const [window, setWindow] = React.useState<MetricsWindow>(initialWindow);
  const [endpoint, setEndpoint] = React.useState<EndpointResponse | null>(null);
  const [stats, setStats] = React.useState<StatsResponse | null>(null);
  const [checks, setChecks] = React.useState<CheckResultResponse[]>([]);
  const [range, setRange] = React.useState<{
    from: dayjs.Dayjs;
    to: dayjs.Dayjs;
  } | null>(null);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const loadData = React.useCallback(async () => {
    if (!pathParams.id) return;
    setLoading(true);
    setError(null);
    try {
      const windowSec = windowToSeconds(window);
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
  }, [pathParams.id, window]);

  React.useEffect(() => {
    const paramWindow = resolveWindow(searchParams.get("window"));
    if (paramWindow !== window) {
      setWindow(paramWindow);
      return;
    }
    void loadData();
  }, [loadData, searchParams, window]);

  const handleWindowChange = (value: MetricsWindow) => {
    setWindow(value);
    const query = new URLSearchParams(searchParams.toString());
    query.set("window", value);
    router.replace(`/endpoints/${pathParams.id}?${query.toString()}`);
  };

  if (!endpoint && !loading) {
    return (
      <Container maxWidth="md" sx={{ py: 6 }}>
        <Typography variant="h5">Endpoint не найден</Typography>
        <Button
          component={Link}
          href={`/?window=${window}`}
          sx={{ mt: 2 }}
          variant="outlined"
        >
          Назад к списку
        </Button>
      </Container>
    );
  }

  const bucketCount = 12;
  const bucketedData =
    range === null
      ? []
      : buildBuckets(checks, range.from, range.to, bucketCount);

  return (
    <Box component="main" sx={{ pb: 6 }}>
      <Container maxWidth="lg" sx={{ pt: 4 }}>
        <Stack spacing={3}>
          <Stack direction="row" spacing={2} alignItems="center">
            <IconButton component={Link} href={`/?window=${window}`}>
              <ArrowBackIcon />
            </IconButton>
            <Box>
              <Typography variant="h4">{endpoint?.name ?? "—"}</Typography>
              <Typography variant="body2" color="text.secondary">
                {endpoint?.method} {endpoint?.url}
              </Typography>
            </Box>
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
                  <Typography variant="subtitle1">Текущий статус</Typography>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <StatusChip status={mapStatus(stats ?? undefined)} />
                    <Typography variant="body2" color="text.secondary">
                      Последняя проверка{" "}
                      {checks.length > 0
                        ? dayjs(checks[checks.length - 1].finishedAt).format(
                            "HH:mm",
                          )
                        : "—"}
                    </Typography>
                  </Stack>
                </Stack>
                <Stack direction="row" spacing={3} alignItems="center">
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      p95
                    </Typography>
                    <Typography variant="h6">{formatMs(stats?.p95)}</Typography>
                  </Stack>
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      p99
                    </Typography>
                    <Typography variant="h6">{formatMs(stats?.p99)}</Typography>
                  </Stack>
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      Error rate
                    </Typography>
                    <Typography variant="h6">
                      {formatPercent(stats?.errorRate)}
                    </Typography>
                  </Stack>
                </Stack>
                <FormControl size="small" sx={{ minWidth: 140 }}>
                  <InputLabel id="window-select">Окно</InputLabel>
                  <Select
                    labelId="window-select"
                    label="Окно"
                    value={window}
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
              </Stack>
            </CardContent>
          </Card>

          <Stack direction={{ xs: "column", md: "row" }} spacing={3}>
            <Card sx={{ flex: 1 }}>
              <CardContent>
                <Stack spacing={2}>
                  <Typography variant="h6">Latency</Typography>
                  <Divider />
                  <LineChart
                    height={280}
                    series={[
                      {
                        data: bucketedData.map((point) => point.latency),
                        label: "Latency",
                        color: "#1976d2",
                      },
                    ]}
                    xAxis={[
                      {
                        data: bucketedData.map((point) => point.time),
                        scaleType: "band",
                      },
                    ]}
                  />
                </Stack>
              </CardContent>
            </Card>
            <Card sx={{ flex: 1 }}>
              <CardContent>
                <Stack spacing={2}>
                  <Typography variant="h6">Ошибки</Typography>
                  <Divider />
                  <BarChart
                    height={280}
                    series={[
                      {
                        data: bucketedData.map((point) => point.errors),
                        label: "Errors",
                        color: "#ef5350",
                      },
                    ]}
                    xAxis={[
                      {
                        data: bucketedData.map((point) => point.time),
                        scaleType: "band",
                      },
                    ]}
                  />
                </Stack>
              </CardContent>
            </Card>
          </Stack>

          {loading && (
            <Typography variant="body2" color="text.secondary">
              Загрузка данных...
            </Typography>
          )}
        </Stack>
      </Container>
    </Box>
  );
}
