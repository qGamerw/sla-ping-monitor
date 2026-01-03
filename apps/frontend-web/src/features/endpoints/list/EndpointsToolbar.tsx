import {
  Button,
  Chip,
  Stack,
  TextField,
} from "@mui/material";

interface EndpointsToolbarProps {
  query: string;
  totalCount: number;
  activeCount: number;
  selectedCount: number;
  onQueryChange: (value: string) => void;
  onBulkEnable: () => void;
  onBulkDisable: () => void;
  onBulkDelete: () => void;
}

export default function EndpointsToolbar({
  query,
  totalCount,
  activeCount,
  selectedCount,
  onQueryChange,
  onBulkEnable,
  onBulkDisable,
  onBulkDelete,
}: EndpointsToolbarProps) {
  return (
    <Stack
      direction={{ xs: "column", md: "row" }}
      spacing={2}
      alignItems={{ xs: "stretch", md: "center" }}
      justifyContent="space-between"
    >
      <Stack
        direction={{ xs: "column", md: "row" }}
        spacing={2}
        alignItems={{ xs: "stretch", md: "center" }}
      >
        <TextField
          label="Поиск endpoint"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          size="small"
        />
        <Stack direction="row" spacing={1} alignItems="center">
          <Button
            variant="outlined"
            disabled={selectedCount === 0}
            onClick={onBulkEnable}
          >
            Включить
          </Button>
          <Button
            variant="outlined"
            disabled={selectedCount === 0}
            onClick={onBulkDisable}
          >
            Отключить
          </Button>
          <Button
            variant="outlined"
            color="error"
            disabled={selectedCount === 0}
            onClick={onBulkDelete}
          >
            Удалить
          </Button>
        </Stack>
      </Stack>
      <Stack direction="row" spacing={1} alignItems="center">
        <Chip
          label={`${totalCount} endpoints`}
          color="primary"
          variant="outlined"
        />
        <Chip
          label={`${activeCount} активных`}
          color="success"
          variant="outlined"
        />
      </Stack>
    </Stack>
  );
}
