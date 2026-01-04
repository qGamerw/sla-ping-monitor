import { Box, Button, Stack, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

interface EndpointsHeaderProps {
  onCreate: () => void;
}

export default function EndpointsHeader({ onCreate }: EndpointsHeaderProps) {
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
      <Button variant="contained" startIcon={<AddIcon />} onClick={onCreate}>
        Создать endpoint
      </Button>
    </Stack>
  );
}
