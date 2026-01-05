"use client";

import { IconButton, Stack, Tab, Tabs, Typography } from "@mui/material";
import SettingsIcon from "@mui/icons-material/Settings";
import AddIcon from "@mui/icons-material/Add";

export const CREATE_FOLDER_TAB = "create-folder";

interface EndpointsFoldersCardProps {
  folders: string[];
  selected: string;
  onSelect: (value: string) => void;
  onCreate: () => void;
  onEdit: () => void;
}

export default function EndpointsFoldersCard({
  folders,
  selected,
  onSelect,
  onCreate,
  onEdit,
}: EndpointsFoldersCardProps) {
  const activeValue = selected === CREATE_FOLDER_TAB ? "all" : selected;

  return (
    <Stack spacing={2}>
      <Tabs
        value={activeValue}
        onChange={(_, value) => {
          if (value === CREATE_FOLDER_TAB) {
            onCreate();
            return;
          }
          onSelect(value);
        }}
        variant="scrollable"
        scrollButtons="auto"
      >
        <Tab label="Все" value="all" />
        {folders.map((folder) => {
          const isActive = folder === selected;
          return (
            <Tab
              key={folder}
              value={folder}
              label={
                <Stack direction="row" spacing={1} alignItems="center">
                  <Typography variant="body2">{folder}</Typography>
                  {isActive && (
                    <IconButton
                      size="small"
                      aria-label="Редактировать папку"
                      onClick={(event) => {
                        event.stopPropagation();
                        onEdit();
                      }}
                    >
                      <SettingsIcon fontSize="inherit" />
                    </IconButton>
                  )}
                </Stack>
              }
            />
          );
        })}
        <Tab icon={<AddIcon />} iconPosition="start" label="Создать" value={CREATE_FOLDER_TAB} />
      </Tabs>
    </Stack>
  );
}
