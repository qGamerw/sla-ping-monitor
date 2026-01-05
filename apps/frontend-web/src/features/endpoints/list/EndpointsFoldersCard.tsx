"use client";

import { Stack, Tab, Tabs } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

export const CREATE_FOLDER_TAB = "create-folder";

interface EndpointsFoldersCardProps {
  folders: string[];
  selected: string;
  onSelect: (value: string) => void;
  onCreate: () => void;
}

export default function EndpointsFoldersCard({
  folders,
  selected,
  onSelect,
  onCreate,
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
        {folders.map((folder) => (
          <Tab key={folder} label={folder} value={folder} />
        ))}
        <Tab icon={<AddIcon />} iconPosition="start" label="Создать" value={CREATE_FOLDER_TAB} />
      </Tabs>
    </Stack>
  );
}
