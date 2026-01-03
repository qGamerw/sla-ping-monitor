import dayjs from "dayjs";
import Link from "next/link";
import {
  Checkbox,
  Chip,
  IconButton,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import type { EndpointResponse, MetricsWindow } from "../../../shared/api/types";
import StatusChip from "../components/StatusChip";
import type { EndpointRow } from "./types";
import { formatMs, formatPercent, mapStatus } from "./utils";

interface EndpointsTableProps {
  endpoints: EndpointRow[];
  selectedIds: string[];
  allSelected: boolean;
  someSelected: boolean;
  windowValue: MetricsWindow;
  refreshSec: number | null;
  onSelectAll: (checked: boolean) => void;
  onSelectRow: (id: string) => void;
  onToggle: (endpoint: EndpointResponse) => void;
  onEdit: (endpoint: EndpointResponse) => void;
  onDelete: (id: string) => void;
}

export default function EndpointsTable({
  endpoints,
  selectedIds,
  allSelected,
  someSelected,
  windowValue,
  refreshSec,
  onSelectAll,
  onSelectRow,
  onToggle,
  onEdit,
  onDelete,
}: EndpointsTableProps) {
  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell padding="checkbox">
            <Checkbox
              checked={allSelected}
              indeterminate={someSelected}
              onChange={(event) => onSelectAll(event.target.checked)}
            />
          </TableCell>
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
        {endpoints.map((endpoint) => {
          const metrics = endpoint.summary?.windowStats;
          const refreshQuery = refreshSec ? `&refresh=${refreshSec}` : "";
          return (
            <TableRow key={endpoint.id} hover>
              <TableCell padding="checkbox">
                <Checkbox
                  checked={selectedIds.includes(endpoint.id)}
                  onChange={() => onSelectRow(endpoint.id)}
                />
              </TableCell>
              <TableCell>
                <Stack spacing={0.5}>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Typography variant="subtitle2">{endpoint.name}</Typography>
                    {!endpoint.enabled && <Chip label="Paused" size="small" />}
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
                      onChange={() => onToggle(endpoint)}
                    />
                  </Tooltip>
                  <Tooltip title="Открыть">
                    <IconButton
                      component={Link}
                      href={`/endpoints/${endpoint.id}?window=${windowValue}${refreshQuery}`}
                      size="small"
                    >
                      <OpenInNewIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Редактировать">
                    <IconButton onClick={() => onEdit(endpoint)} size="small">
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Удалить">
                    <IconButton
                      onClick={() => onDelete(endpoint.id)}
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
  );
}
