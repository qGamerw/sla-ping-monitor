import Link from "next/link";
import { Box, Button, IconButton, Stack, Typography } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EditIcon from "@mui/icons-material/Edit";

interface EndpointHeaderProps {
  name: string;
  method: string;
  url: string;
  backLink: string;
  onEdit: () => void;
}

export default function EndpointHeader({
  name,
  method,
  url,
  backLink,
  onEdit,
}: EndpointHeaderProps) {
  return (
    <Stack direction="row" spacing={2} alignItems="center">
      <IconButton component={Link} href={backLink}>
        <ArrowBackIcon />
      </IconButton>
      <Box>
        <Typography variant="h4">{name}</Typography>
        <Typography variant="body2" color="text.secondary">
          {method} {url}
        </Typography>
      </Box>
      <Button
        variant="outlined"
        startIcon={<EditIcon />}
        onClick={onEdit}
        sx={{ ml: "auto" }}
      >
        Редактировать
      </Button>
    </Stack>
  );
}
