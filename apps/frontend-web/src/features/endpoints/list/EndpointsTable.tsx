import * as React from "react";
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
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
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
  onTagsChange: (endpoint: EndpointResponse, nextTags: string[]) => void;
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
  onTagsChange,
  onEdit,
  onDelete,
}: EndpointsTableProps) {
  const [tagInputs, setTagInputs] = React.useState<Record<string, string>>({});

  const handleTagInputChange = (id: string, value: string) => {
    setTagInputs((prev) => ({ ...prev, [id]: value }));
  };

  const handleAddTag = (endpoint: EndpointResponse) => {
    const rawValue = tagInputs[endpoint.id] ?? "";
    const value = rawValue.trim();
    if (!value) return;
    const existing = endpoint.tags ?? [];
    if (existing.includes(value)) {
      setTagInputs((prev) => ({ ...prev, [endpoint.id]: "" }));
      return;
    }
    const nextTags = [...existing, value];
    onTagsChange(endpoint, nextTags);
    setTagInputs((prev) => ({ ...prev, [endpoint.id]: "" }));
  };

  const handleRemoveTag = (endpoint: EndpointResponse, tag: string) => {
    const nextTags = (endpoint.tags ?? []).filter((item) => item !== tag);
    onTagsChange(endpoint, nextTags);
  };

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
                    <Typography
                      variant="subtitle2"
                      component={Link}
                      href={`/endpoints/${endpoint.id}`}
                      color="primary"
                      sx={{ textDecoration: "none" }}
                    >
                      {endpoint.name}
                    </Typography>
                    {!endpoint.enabled && <Chip label="Paused" size="small" />}
                  </Stack>
                  <Typography variant="caption" color="text.secondary">
                    {endpoint.method} {endpoint.url}
                  </Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap">
                    {(endpoint.tags ?? []).map((tag) => (
                      <Chip
                        key={tag}
                        label={tag}
                        size="small"
                        variant="outlined"
                        onDelete={() => handleRemoveTag(endpoint, tag)}
                      />
                    ))}
                    <Stack
                      direction="row"
                      spacing={0.5}
                      alignItems="center"
                      sx={{ minWidth: 200 }}
                    >
                      <TextField
                        size="small"
                        placeholder="Добавить тег"
                        value={tagInputs[endpoint.id] ?? ""}
                        onChange={(event) =>
                          handleTagInputChange(endpoint.id, event.target.value)
                        }
                        onKeyDown={(event) => {
                          if (event.key === "Enter") {
                            event.preventDefault();
                            handleAddTag(endpoint);
                          }
                        }}
                      />
                      <IconButton
                        size="small"
                        color="primary"
                        aria-label="Добавить тег"
                        onClick={() => handleAddTag(endpoint)}
                      >
                        <AddIcon fontSize="small" />
                      </IconButton>
                    </Stack>
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
