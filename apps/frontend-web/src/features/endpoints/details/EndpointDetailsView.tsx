"use client";

import { Alert, Box, Container, Stack, Typography } from "@mui/material";
import EndpointFormDialog from "../components/EndpointFormDialog";
import EndpointCharts from "./EndpointCharts";
import EndpointHeader from "./EndpointHeader";
import EndpointStatusCard from "./EndpointStatusCard";
import { buildBuckets } from "./utils";
import { useEndpointDetails } from "./useEndpointDetails";

export default function EndpointDetailsView() {
  const {
    endpoint,
    stats,
    checks,
    range,
    windowValue,
    refreshSec,
    loading,
    error,
    dialogOpen,
    backLink,
    setDialogOpen,
    handleWindowChange,
    handleRefreshChange,
    handleRefreshNow,
    handleEdit,
    handleSave,
    handleToggle,
  } = useEndpointDetails();

  if (!endpoint && !loading) {
    return (
      <Container maxWidth="md" sx={{ py: 6 }}>
        <Typography variant="h5">Endpoint не найден</Typography>
      </Container>
    );
  }

  const bucketCount = 12;
  const bucketedData =
    range === null ? [] : buildBuckets(checks, range.from, range.to, bucketCount);

  return (
    <Box component="main" sx={{ pb: 6 }}>
      <Container maxWidth="lg" sx={{ pt: 4 }}>
        {endpoint && (
          <EndpointHeader
            name={endpoint.name}
            method={endpoint.method}
            url={endpoint.url}
            backLink={backLink}
            onEdit={handleEdit}
          />
        )}

        {error && <Alert severity="error">{error}</Alert>}

        <Stack spacing={3} sx={{ mt: 3 }}>
          {endpoint && (
            <EndpointStatusCard
              stats={stats}
              enabled={endpoint.enabled}
              lastCheckAt={
                checks.length > 0 ? checks[checks.length - 1].finishedAt : null
              }
              windowValue={windowValue}
              refreshSec={refreshSec}
              onWindowChange={handleWindowChange}
              onRefreshChange={handleRefreshChange}
              onRefreshNow={handleRefreshNow}
              onToggle={handleToggle}
            />
          )}

          <EndpointCharts
            latencyPoints={bucketedData.map((point) => ({
              time: point.time,
              value: point.latency,
            }))}
            errorPoints={bucketedData.map((point) => ({
              time: point.time,
              value: point.errors,
            }))}
          />

          {loading && (
            <Typography variant="body2" color="text.secondary">
              Загрузка данных...
            </Typography>
          )}
        </Stack>
      </Container>

      {endpoint && (
        <EndpointFormDialog
          open={dialogOpen}
          availableTags={endpoint.tags ?? []}
          initial={endpoint}
          onClose={() => setDialogOpen(false)}
          onSave={handleSave}
        />
      )}
    </Box>
  );
}
