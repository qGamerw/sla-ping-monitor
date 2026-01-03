import dayjs from "dayjs";
import {
  Card,
  CardContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  Typography,
} from "@mui/material";
import StatusChip from "../components/StatusChip";
import type { MetricsWindow, StatsResponse } from "../../../shared/api/types";
import { windowOptions } from "../../../shared/api/windows";
import { formatMs, formatPercent, mapStatus } from "./utils";

interface EndpointStatusCardProps {
  stats: StatsResponse | null;
  enabled: boolean;
  lastCheckAt: string | null;
  windowValue: MetricsWindow;
  refreshSec: number | null;
  onWindowChange: (value: MetricsWindow) => void;
  onRefreshChange: (value: string) => void;
  onToggle: () => void;
}

export default function EndpointStatusCard({
  stats,
  enabled,
  lastCheckAt,
  windowValue,
  refreshSec,
  onWindowChange,
  onRefreshChange,
  onToggle,
}: EndpointStatusCardProps) {
  return (
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
              <StatusChip status={mapStatus(stats ?? undefined, enabled)} />
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="body2" color="text.secondary">
                  {enabled ? "Включен" : "Отключен"}
                </Typography>
                <Switch size="small" checked={enabled} onChange={onToggle} />
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Последняя проверка{" "}
                {lastCheckAt ? dayjs(lastCheckAt).format("HH:mm") : "—"}
              </Typography>
            </Stack>
          </Stack>
          <Stack direction="row" spacing={3} alignItems="center">
            <Stack>
              <Typography variant="caption" color="text.secondary">
                p50
              </Typography>
              <Typography variant="h6">{formatMs(stats?.p50)}</Typography>
            </Stack>
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
          <Stack direction="row" spacing={2} alignItems="center">
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel id="window-select">Окно</InputLabel>
              <Select
                labelId="window-select"
                label="Окно"
                value={windowValue}
                onChange={(event) =>
                  onWindowChange(event.target.value as MetricsWindow)
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
                onChange={(event) => onRefreshChange(String(event.target.value))}
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
  );
}
