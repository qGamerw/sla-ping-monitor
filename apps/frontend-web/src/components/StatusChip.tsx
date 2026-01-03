import { Chip } from "@mui/material";
export type Status = "OK" | "DEGRADED" | "DOWN";

const statusColor: Record<Status, "success" | "warning" | "error"> = {
  OK: "success",
  DEGRADED: "warning",
  DOWN: "error",
};

export default function StatusChip({ status }: { status: Status }) {
  return <Chip label={status} color={statusColor[status]} size="small" />;
}
