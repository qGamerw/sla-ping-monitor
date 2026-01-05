"use client";

import * as React from "react";
import {
  Autocomplete,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  MenuItem,
  Stack,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import type { EndpointRequest, EndpointResponse } from "../../../shared/api/types";

export type EndpointDraft = Omit<EndpointRequest, "headers" | "tags"> & {
  headers: Record<string, string>;
  tags: string[];
};

interface EndpointFormDialogProps {
  open: boolean;
  availableTags: string[];
  initial?: EndpointResponse | null;
  hideNameField?: boolean;
  count?: number;
  onCountChange?: (value: number) => void;
  onClose: () => void;
  onSave: (draft: EndpointRequest) => void;
}

const defaultDraft: EndpointDraft = {
  name: "",
  url: "https://",
  method: "GET",
  headers: {
    accept: "text/plain",
  },
  timeoutMs: 3000,
  expectedStatus: [200, 399],
  intervalSec: 5,
  enabled: true,
  tags: [],
};

const httpMethods = [
  "GET",
  "POST",
  "PUT",
  "PATCH",
  "DELETE",
  "HEAD",
  "OPTIONS",
  "TRACE",
  "CONNECT",
];

export default function EndpointFormDialog({
  open,
  availableTags,
  initial,
  onClose,
  onSave,
  hideNameField = false,
  count,
  onCountChange,
}: EndpointFormDialogProps) {
  const [draft, setDraft] = React.useState<EndpointDraft>(defaultDraft);
  const [headers, setHeaders] = React.useState(
    Object.entries(defaultDraft.headers ?? {}).map(([key, value]) => ({
      key,
      value,
    })),
  );
  const [timeoutInput, setTimeoutInput] = React.useState(
    String(defaultDraft.timeoutMs),
  );
  const [intervalInput, setIntervalInput] = React.useState(
    String(defaultDraft.intervalSec),
  );
  const [expectedStatusInput, setExpectedStatusInput] = React.useState(
    defaultDraft.expectedStatus.join(", "),
  );
  const prevOpenRef = React.useRef(open);
  const isFormValid =
    draft.name.trim().length > 0 &&
    draft.url.trim().length > 0 &&
    draft.method.trim().length > 0 &&
    timeoutInput.trim().length > 0 &&
    intervalInput.trim().length > 0 &&
    expectedStatusInput
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean).length > 0;

  React.useEffect(() => {
    const wasOpen = prevOpenRef.current;
    prevOpenRef.current = open;

    if (open && !wasOpen) {
      if (initial) {
        setDraft({
          name: initial.name,
          url: initial.url,
          method: initial.method,
          headers: initial.headers ?? {},
          timeoutMs: initial.timeoutMs,
          expectedStatus: initial.expectedStatus,
          intervalSec: initial.intervalSec,
          enabled: initial.enabled,
          tags: initial.tags ?? [],
        });
        setHeaders(
          Object.entries(initial.headers ?? {}).map(([key, value]) => ({
            key,
            value,
          })),
        );
        setTimeoutInput(String(initial.timeoutMs));
        setIntervalInput(String(initial.intervalSec));
        setExpectedStatusInput(initial.expectedStatus.join(", "));
        return;
      }
      setDraft(defaultDraft);
      setHeaders(
        Object.entries(defaultDraft.headers ?? {}).map(([key, value]) => ({
          key,
          value,
        })),
      );
      setTimeoutInput(String(defaultDraft.timeoutMs));
      setIntervalInput(String(defaultDraft.intervalSec));
      setExpectedStatusInput(defaultDraft.expectedStatus.join(", "));
    }

    if (!open) {
      setDraft(defaultDraft);
      setHeaders(
        Object.entries(defaultDraft.headers ?? {}).map(([key, value]) => ({
          key,
          value,
        })),
      );
      setTimeoutInput(String(defaultDraft.timeoutMs));
      setIntervalInput(String(defaultDraft.intervalSec));
      setExpectedStatusInput(defaultDraft.expectedStatus.join(", "));
    }
  }, [initial, open]);

  const handleChange = (field: keyof EndpointDraft) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      setDraft((prev) => ({ ...prev, [field]: event.target.value }));
    };

  const handleToggle = (
    _event: React.ChangeEvent<HTMLInputElement>,
    checked: boolean,
  ) => {
    setDraft((prev) => ({ ...prev, enabled: checked }));
  };

  const handleSave = () => {
    const headerMap = headers.reduce<Record<string, string>>((acc, item) => {
      if (item.key.trim()) {
        acc[item.key.trim()] = item.value.trim();
      }
      return acc;
    }, {});
    const timeoutMs = Number.parseInt(timeoutInput, 10);
    const intervalSec = Number.parseInt(intervalInput, 10);
    const expectedStatus = expectedStatusInput
      .split(",")
      .map((value) => Number.parseInt(value.trim(), 10))
      .filter((value) => Number.isFinite(value));
    onSave({
      ...draft,
      timeoutMs: Number.isFinite(timeoutMs) ? Math.max(0, timeoutMs) : 0,
      intervalSec: Number.isFinite(intervalSec) ? Math.max(0, intervalSec) : 0,
      headers: Object.keys(headerMap).length > 0 ? headerMap : null,
      expectedStatus,
      tags: draft.tags.length > 0 ? draft.tags : null,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {initial ? "Редактировать endpoint" : "Создать endpoint"}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {!hideNameField && (
            <TextField
              label="Название"
              value={draft.name}
              onChange={handleChange("name")}
              required
              fullWidth
            />
          )}
          <TextField
            label="URL"
            value={draft.url}
            onChange={handleChange("url")}
            required
            fullWidth
          />
          <TextField
            label="Метод"
            value={draft.method}
            onChange={handleChange("method")}
            select
            required
            fullWidth
          >
            {httpMethods.map((method) => (
              <MenuItem key={method} value={method}>
                {method}
              </MenuItem>
            ))}
          </TextField>
          <Stack spacing={1}>
            <Typography variant="subtitle2">Headers</Typography>
            {headers.map((header, index) => (
              <Stack key={`${header.key}-${index}`} direction="row" spacing={1}>
                <TextField
                  label="Ключ"
                  value={header.key}
                  onChange={(event) => {
                    const value = event.target.value;
                    setHeaders((prev) =>
                      prev.map((item, idx) =>
                        idx === index ? { ...item, key: value } : item,
                      ),
                    );
                  }}
                  fullWidth
                />
                <TextField
                  label="Значение"
                  value={header.value}
                  onChange={(event) => {
                    const value = event.target.value;
                    setHeaders((prev) =>
                      prev.map((item, idx) =>
                        idx === index ? { ...item, value } : item,
                      ),
                    );
                  }}
                  fullWidth
                />
                <IconButton
                  aria-label="Удалить header"
                  onClick={() => {
                    const confirmed = window.confirm(
                      "Удалить этот заголовок?",
                    );
                    if (!confirmed) return;
                    setHeaders((prev) =>
                      prev.filter((_, idx) => idx !== index),
                    );
                  }}
                >
                  <DeleteIcon />
                </IconButton>
              </Stack>
            ))}
            <Button
              variant="text"
              onClick={() => setHeaders((prev) => [...prev, { key: "", value: "" }])}
            >
              Добавить header
            </Button>
          </Stack>
          <TextField
            label="Timeout (ms)"
            type="number"
            value={timeoutInput}
            onChange={(event) => {
              const value = event.target.value;
              setTimeoutInput(value);
            }}
            inputProps={{ min: 0 }}
            required
            fullWidth
          />
          <TextField
            label="Expected status"
            value={expectedStatusInput}
            onChange={(event) =>
              setExpectedStatusInput(
                event.target.value.replace(/[^\d,\s]/g, ""),
              )
            }
            helperText="Введите статусы через запятую, например 200, 399"
            required
            fullWidth
          />
          <TextField
            label="Интервал (сек)"
            type="number"
            value={intervalInput}
            onChange={(event) => {
              const value = event.target.value;
              setIntervalInput(value);
            }}
            inputProps={{ min: 0 }}
            required
            fullWidth
          />
          <Autocomplete
            multiple
            options={availableTags}
            freeSolo
            value={draft.tags}
            onChange={(_, value) =>
              setDraft((prev) => ({ ...prev, tags: value }))
            }
            renderInput={(params) => (
              <TextField {...params} label="Теги" placeholder="Добавить тег" />
            )}
          />
          {onCountChange && (
            <TextField
              label="Количество"
              type="number"
              value={count ?? 1}
              onChange={(event) => onCountChange(Number(event.target.value))}
              inputProps={{ min: 1 }}
              fullWidth
            />
          )}
          <Box>
            <FormControlLabel
              control={<Switch checked={draft.enabled} onChange={handleToggle} />}
              label={draft.enabled ? "Включено" : "Выключено"}
            />
          </Box>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleSave} variant="contained" disabled={!isFormValid}>
          Сохранить
        </Button>
      </DialogActions>
    </Dialog>
  );
}
