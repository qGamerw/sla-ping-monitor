import { BarChart, LineChart } from "@mui/x-charts";
import { Card, CardContent, Divider, Stack, Typography } from "@mui/material";

interface EndpointChartsProps {
  latencyPoints: Array<{ time: string; value: number | null }>;
  errorPoints: Array<{ time: string; value: number }>;
}

export default function EndpointCharts({
  latencyPoints,
  errorPoints,
}: EndpointChartsProps) {
  const formatLatency = (value: number | null) =>
    value === null ? "—" : `${value.toFixed(0)} ms`;
  const formatErrors = (value: number | null) =>
    value === null ? "—" : `${value.toFixed(0)} errors`;

  return (
    <Stack direction={{ xs: "column", md: "row" }} spacing={3}>
      <Card sx={{ flex: 1 }}>
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Latency</Typography>
            <Divider />
            <LineChart
              height={280}
              series={[
                {
                  data: latencyPoints.map((point) => point.value),
                  label: "Latency",
                  color: "#1976d2",
                  valueFormatter: formatLatency,
                },
              ]}
              xAxis={[
                {
                  data: latencyPoints.map((point) => point.time),
                  scaleType: "band",
                },
              ]}
              tooltip={{ trigger: "axis" }}
            />
          </Stack>
        </CardContent>
      </Card>
      <Card sx={{ flex: 1 }}>
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Ошибки</Typography>
            <Divider />
            <BarChart
              height={280}
              series={[
                {
                  data: errorPoints.map((point) => point.value),
                  label: "Errors",
                  color: "#ef5350",
                  valueFormatter: formatErrors,
                },
              ]}
              xAxis={[
                {
                  data: errorPoints.map((point) => point.time),
                  scaleType: "band",
                },
              ]}
              tooltip={{ trigger: "axis" }}
            />
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
}
