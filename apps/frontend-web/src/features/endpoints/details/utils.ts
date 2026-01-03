import dayjs from "dayjs";
import type { CheckResultResponse, StatsResponse } from "../../../shared/api/types";
import type { Status } from "../components/StatusChip";

export const formatMs = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(0)} ms`;

export const formatPercent = (value?: number | null) =>
  value === null || value === undefined ? "—" : `${value.toFixed(1)}%`;

export const mapStatus = (
  stats: StatsResponse | undefined,
  enabled: boolean,
): Status => {
  if (!enabled) return "OFF";
  if (!stats || stats.lastStatus === null || stats.lastStatus === undefined) {
    return "DEGRADED";
  }
  if (stats.lastStatus >= 200 && stats.lastStatus < 400) {
    return "OK";
  }
  return "DOWN";
};

export const formatChartTime = (value: string) => dayjs(value).format("HH:mm");

export const buildBuckets = (
  checks: CheckResultResponse[],
  from: dayjs.Dayjs,
  to: dayjs.Dayjs,
  bucketCount: number,
) => {
  const windowMs = to.diff(from, "millisecond");
  const bucketMs = windowMs / bucketCount;
  const buckets = Array.from({ length: bucketCount }, (_, index) => ({
    start: from.add(bucketMs * index, "millisecond"),
    latencies: [] as number[],
    errors: 0,
  }));

  checks.forEach((check) => {
    const timestamp = dayjs(check.startedAt);
    if (timestamp.isBefore(from) || timestamp.isAfter(to)) return;
    const position = Math.min(
      Math.floor(timestamp.diff(from, "millisecond") / bucketMs),
      bucketCount - 1,
    );
    buckets[position].latencies.push(check.latencyMs);
    if (!check.success) {
      buckets[position].errors += 1;
    }
  });

  return buckets.map((bucket) => ({
    time: formatChartTime(bucket.start.toISOString()),
    latency:
      bucket.latencies.length > 0
        ? bucket.latencies.reduce((sum, value) => sum + value, 0) /
          bucket.latencies.length
        : null,
    errors: bucket.errors,
  }));
};
