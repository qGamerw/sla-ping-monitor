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
  Stack,
  Switch,
  TextField,
} from "@mui/material";
import type { EndpointSummary } from "../app/lib/mockData";

export interface EndpointDraft {
  name: string;
  url: string;
  method: string;
  enabled: boolean;
  tags: string[];
}

interface EndpointFormDialogProps {
  open: boolean;
  availableTags: string[];
  initial?: EndpointSummary | null;
  onClose: () => void;
  onSave: (draft: EndpointDraft) => void;
}

const defaultDraft: EndpointDraft = {
  name: "",
  url: "https://",
  method: "GET",
  enabled: true,
  tags: [],
};

export default function EndpointFormDialog({
  open,
  availableTags,
  initial,
  onClose,
  onSave,
}: EndpointFormDialogProps) {
  const [draft, setDraft] = React.useState<EndpointDraft>(defaultDraft);

  React.useEffect(() => {
    if (initial) {
      setDraft({
        name: initial.name,
        url: initial.url,
        method: initial.method,
        enabled: initial.enabled,
        tags: initial.tags,
      });
      return;
    }
    setDraft(defaultDraft);
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
    onSave(draft);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {initial ? "Редактировать endpoint" : "Создать endpoint"}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Название"
            value={draft.name}
            onChange={handleChange("name")}
            fullWidth
          />
          <TextField
            label="URL"
            value={draft.url}
            onChange={handleChange("url")}
            fullWidth
          />
          <TextField
            label="Метод"
            value={draft.method}
            onChange={handleChange("method")}
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
        <Button onClick={handleSave} variant="contained">
          Сохранить
        </Button>
      </DialogActions>
    </Dialog>
  );
}
