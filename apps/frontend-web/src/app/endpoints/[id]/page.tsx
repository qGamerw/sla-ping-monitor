"use client";

import * as React from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import dayjs from "dayjs";
import {
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
import StatusChip from "../../../components/StatusChip";
import {
  errorSeries,
  initialEndpoints,
  latencySeries,
  windowOptions,
  type MetricsWindow,
} from "../../lib/mockData";

const formatMs = (value: number) => `${value.toFixed(0)} ms`;
const formatPercent = (value: number) => `${value.toFixed(1)}%`;

export default function EndpointDetailsPage() {
  const params = useParams<{ id: string }>();
  const [window, setWindow] = React.useState<MetricsWindow>("1h");

  const endpoint = React.useMemo(
    () => initialEndpoints.find((item) => item.id === params.id),
    [params.id],
  );

  if (!endpoint) {
    return (
      <Container maxWidth="md" sx={{ py: 6 }}>
        <Typography variant="h5">Endpoint не найден</Typography>
        <Button component={Link} href="/" sx={{ mt: 2 }} variant="outlined">
          Назад к списку
        </Button>
      </Container>
    );
  }

  const metrics = endpoint.metrics[window];
  const latencyPoints = latencySeries[window];
  const errorPoints = errorSeries[window];

  return (
    <Box component="main" sx={{ pb: 6 }}>
      <Container maxWidth="lg" sx={{ pt: 4 }}>
        <Stack spacing={3}>
          <Stack direction="row" spacing={2} alignItems="center">
            <IconButton component={Link} href="/">
              <ArrowBackIcon />
            </IconButton>
            <Box>
              <Typography variant="h4">{endpoint.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {endpoint.method} {endpoint.url}
              </Typography>
            </Box>
          </Stack>

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
                    <StatusChip status={endpoint.status} />
                    <Typography variant="body2" color="text.secondary">
                      Последняя проверка {dayjs(metrics.lastCheck).format("HH:mm")}
                    </Typography>
                  </Stack>
                </Stack>
                <Stack direction="row" spacing={3} alignItems="center">
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      p95
                    </Typography>
                    <Typography variant="h6">{formatMs(metrics.p95)}</Typography>
                  </Stack>
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      p99
                    </Typography>
                    <Typography variant="h6">{formatMs(metrics.p99)}</Typography>
                  </Stack>
                  <Stack>
                    <Typography variant="caption" color="text.secondary">
                      Error rate
                    </Typography>
                    <Typography variant="h6">
                      {formatPercent(metrics.errorRate)}
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
                      setWindow(event.target.value as MetricsWindow)
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
                        data: latencyPoints.map((point) => point.value),
                        label: "Latency",
                        color: "#1976d2",
                      },
                    ]}
                    xAxis={[
                      {
                        data: latencyPoints.map((point) => point.time),
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
                        data: errorPoints.map((point) => point.value),
                        label: "Errors",
                        color: "#ef5350",
                      },
                    ]}
                    xAxis={[
                      {
                        data: errorPoints.map((point) => point.time),
                        scaleType: "band",
                      },
                    ]}
                  />
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
}
