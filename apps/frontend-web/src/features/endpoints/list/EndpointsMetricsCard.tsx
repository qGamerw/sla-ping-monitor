import {
  Card,
  CardContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Typography,
} from "@mui/material";
import type { MetricsWindow } from "../../../shared/api/types";
import { windowOptions } from "../../../shared/api/windows";

interface EndpointsMetricsCardProps {
  windowValue: MetricsWindow;
  refreshSec: number | null;
  onWindowChange: (value: MetricsWindow) => void;
  onRefreshChange: (value: string) => void;
}

export default function EndpointsMetricsCard({
  windowValue,
  refreshSec,
  onWindowChange,
  onRefreshChange,
}: EndpointsMetricsCardProps) {
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
            <Typography variant="h6">Окно метрик</Typography>
            <Typography variant="body2" color="text.secondary">
              Все значения p50/p95/p99 и error rate рассчитаны для выбранного
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
                onChange={(event) =>
                  onRefreshChange(String(event.target.value))
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
  );
}
