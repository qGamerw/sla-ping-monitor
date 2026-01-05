"use client";

import * as React from "react";
import {
  Autocomplete,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import type { EndpointResponse, FolderResponse } from "../../../shared/api/types";

interface FolderDraft {
  name: string;
  endpoints: string[];
}

interface FolderFormDialogProps {
  open: boolean;
  endpoints: EndpointResponse[];
  folderNames: string[];
  folder?: FolderResponse | null;
  onClose: () => void;
  onSave: (draft: FolderDraft) => void;
  onDelete?: () => void;
}

export default function FolderFormDialog({
  open,
  endpoints,
  folderNames,
  folder,
  onClose,
  onSave,
  onDelete,
}: FolderFormDialogProps) {
  const [name, setName] = React.useState("");
  const [selectedIds, setSelectedIds] = React.useState<string[]>([]);

  const endpointOptions = React.useMemo(
    () => [...endpoints].sort((a, b) => a.name.localeCompare(b.name)),
    [endpoints],
  );

  React.useEffect(() => {
    if (folder) {
      setName(folder.name);
      setSelectedIds(folder.endpoints ?? []);
      return;
    }
    setName("");
    setSelectedIds([]);
  }, [folder, open]);

  const selectedEndpoints = React.useMemo(
    () => endpointOptions.filter((endpoint) => selectedIds.includes(endpoint.id)),
    [endpointOptions, selectedIds],
  );

  const handleSave = () => {
    if (!name.trim()) return;
    onSave({ name: name.trim(), endpoints: selectedIds });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {folder ? "Редактировать папку" : "Создать папку"}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Autocomplete
            freeSolo
            options={folderNames}
            value={name}
            inputValue={name}
            onInputChange={(_, value) => setName(value)}
            onChange={(_, value) => setName(value ?? "")}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Название папки"
                placeholder="Введите название"
                fullWidth
              />
            )}
          />
          <Stack spacing={1}>
            <Typography variant="subtitle2">Endpoints в папке</Typography>
            <Autocomplete
              multiple
              options={endpointOptions}
              value={selectedEndpoints}
              onChange={(_, value) =>
                setSelectedIds(value.map((item) => item.id))
              }
              getOptionLabel={(option) => option.name}
              isOptionEqualToValue={(option, value) => option.id === value.id}
              renderOption={(props, option) => (
                <li {...props} key={option.id}>
                  <Stack>
                    <Typography variant="body2">{option.name}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {option.url}
                    </Typography>
                  </Stack>
                </li>
              )}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Добавить endpoints"
                  placeholder="Выберите endpoints"
                />
              )}
            />
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions>
        {onDelete && (
          <Button color="error" onClick={onDelete}>
            Удалить
          </Button>
        )}
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleSave} variant="contained" disabled={!name.trim()}>
          Сохранить
        </Button>
      </DialogActions>
    </Dialog>
  );
}
