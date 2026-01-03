import { Box, Button, Stack, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

interface EndpointsHeaderProps {
  onCreate: () => void;
  onBulkCreate: () => void;
}

export default function EndpointsHeader({
  onCreate,
  onBulkCreate,
}: EndpointsHeaderProps) {
  return (
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
      <Stack direction="row" spacing={2}>
        <Button variant="outlined" onClick={onBulkCreate}>
          Быстро создать
        </Button>
        <Button variant="contained" startIcon={<AddIcon />} onClick={onCreate}>
          Создать endpoint
        </Button>
      </Stack>
    </Stack>
  );
}
