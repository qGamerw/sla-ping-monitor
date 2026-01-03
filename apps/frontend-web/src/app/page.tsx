"use client";

import * as React from "react";
import Link from "next/link";
import dayjs from "dayjs";
import {
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
import EndpointFormDialog, {
  EndpointDraft,
} from "../components/EndpointFormDialog";
import StatusChip from "../components/StatusChip";
import {
  initialEndpoints,
  windowOptions,
  type EndpointSummary,
  type MetricsWindow,
} from "./lib/mockData";

const formatMs = (value: number) => `${value.toFixed(0)} ms`;
const formatPercent = (value: number) => `${value.toFixed(1)}%`;

export default function HomePage() {
  const [window, setWindow] = React.useState<MetricsWindow>("15m");
  const [endpoints, setEndpoints] = React.useState<EndpointSummary[]>(
    initialEndpoints,
  );
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<EndpointSummary | null>(null);
  const [query, setQuery] = React.useState("");

  const availableTags = React.useMemo(
    () => Array.from(new Set(endpoints.flatMap((item) => item.tags))),
    [endpoints],
  );

  const filteredEndpoints = endpoints.filter((endpoint) =>
    endpoint.name.toLowerCase().includes(query.toLowerCase()),
  );

  const handleCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const handleEdit = (endpoint: EndpointSummary) => {
    setEditing(endpoint);
    setDialogOpen(true);
  };

  const handleSave = (draft: EndpointDraft) => {
    if (editing) {
      setEndpoints((prev) =>
        prev.map((endpoint) =>
          endpoint.id === editing.id
            ? {
                ...endpoint,
                name: draft.name,
                url: draft.url,
                method: draft.method,
                headers: draft.headers,
                timeoutMs: draft.timeoutMs,
                expectedStatus: draft.expectedStatus,
                intervalSec: draft.intervalSec,
                enabled: draft.enabled,
                tags: draft.tags,
              }
            : endpoint,
        ),
      );
    } else {
      const id = draft.name.toLowerCase().replace(/\s+/g, "-");
      setEndpoints((prev) => [
        {
          id,
          name: draft.name,
          url: draft.url,
          method: draft.method,
          headers: draft.headers,
          timeoutMs: draft.timeoutMs,
          expectedStatus: draft.expectedStatus,
          intervalSec: draft.intervalSec,
          status: "OK",
          enabled: draft.enabled,
          tags: draft.tags,
          metrics: prev[0]?.metrics ?? initialEndpoints[0].metrics,
        },
        ...prev,
      ]);
    }
    setDialogOpen(false);
  };

  const handleDelete = (id: string) => {
    setEndpoints((prev) => prev.filter((endpoint) => endpoint.id !== id));
  };

  const handleToggle = (id: string) => {
    setEndpoints((prev) =>
      prev.map((endpoint) =>
        endpoint.id === id
          ? { ...endpoint, enabled: !endpoint.enabled }
          : endpoint,
      ),
    );
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
                    <TableCell>p95</TableCell>
                    <TableCell>p99</TableCell>
                    <TableCell>Error rate</TableCell>
                    <TableCell>Последняя проверка</TableCell>
                    <TableCell align="right">Действия</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredEndpoints.map((endpoint) => {
                    const metrics = endpoint.metrics[window];
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
                              {endpoint.tags.map((tag) => (
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
                          <StatusChip status={endpoint.status} />
                        </TableCell>
                        <TableCell>{formatMs(metrics.p95)}</TableCell>
                        <TableCell>{formatMs(metrics.p99)}</TableCell>
                        <TableCell>{formatPercent(metrics.errorRate)}</TableCell>
                        <TableCell>
                          {dayjs(metrics.lastCheck).format("HH:mm:ss")}
                        </TableCell>
                        <TableCell align="right">
                          <Stack direction="row" spacing={1} justifyContent="flex-end">
                            <Tooltip title="Вкл/выкл">
                              <Switch
                                size="small"
                                checked={endpoint.enabled}
                                onChange={() => handleToggle(endpoint.id)}
                              />
                            </Tooltip>
                            <Tooltip title="Открыть">
                              <IconButton
                                component={Link}
                                href={`/endpoints/${endpoint.id}`}
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
