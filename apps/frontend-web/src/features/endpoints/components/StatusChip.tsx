import { Chip } from "@mui/material";
export type Status = "OK" | "DEGRADED" | "DOWN" | "OFF";

const statusColor: Record<
  Status,
  "success" | "warning" | "error" | "default"
> = {
  OK: "success",
  DEGRADED: "warning",
  DOWN: "error",
  OFF: "default",
};

export default function StatusChip({ status }: { status: Status }) {
  return <Chip label={status} color={statusColor[status]} size="small" />;
}
