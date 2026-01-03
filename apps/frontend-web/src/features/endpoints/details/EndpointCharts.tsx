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
                },
              ]}
              xAxis={[
                {
                  data: latencyPoints.map((point) => point.time),
                  scaleType: "band",
                },
              ]}
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
                },
              ]}
              xAxis={[
                {
                  data: errorPoints.map((point) => point.time),
                  scaleType: "band",
                },
              ]}
            />
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
}
