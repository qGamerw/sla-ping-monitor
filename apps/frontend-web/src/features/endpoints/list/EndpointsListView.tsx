"use client";

import { Alert, Card, CardContent, Container, Divider, Stack, Typography } from "@mui/material";
import EndpointFormDialog from "../components/EndpointFormDialog";
import EndpointsHeader from "./EndpointsHeader";
import EndpointsMetricsCard from "./EndpointsMetricsCard";
import EndpointsTable from "./EndpointsTable";
import EndpointsToolbar from "./EndpointsToolbar";
import { useEndpointsList } from "./useEndpointsList";

export default function EndpointsListView() {
  const {
    windowValue,
    refreshSec,
    filteredEndpoints,
    availableTags,
    dialogOpen,
    bulkDialogOpen,
    editing,
    query,
    bulkCount,
    selectedIds,
    selectedCount,
    allSelected,
    someSelected,
    loading,
    error,
    setQuery,
    setBulkCount,
    handleWindowChange,
    handleRefreshChange,
    handleCreate,
    handleBulkOpen,
    handleEdit,
    handleSave,
    handleBulkSave,
    handleDelete,
    handleToggle,
    handleSelectAll,
    handleSelectRow,
    handleBulkToggle,
    handleBulkDelete,
    setDialogOpen,
    setBulkDialogOpen,
  } = useEndpointsList();

  return (
    <Container maxWidth="lg" sx={{ pt: 4 }}>
      <Stack spacing={3}>
        <EndpointsHeader onCreate={handleCreate} onBulkCreate={handleBulkOpen} />

        {error && <Alert severity="error">{error}</Alert>}

        <EndpointsMetricsCard
          windowValue={windowValue}
          refreshSec={refreshSec}
          onWindowChange={handleWindowChange}
          onRefreshChange={handleRefreshChange}
        />

        <Card>
          <CardContent>
            <EndpointsToolbar
              query={query}
              totalCount={filteredEndpoints.length}
              activeCount={filteredEndpoints.filter((item) => item.enabled).length}
              selectedCount={selectedCount}
              onQueryChange={setQuery}
              onBulkEnable={() => handleBulkToggle(true)}
              onBulkDisable={() => handleBulkToggle(false)}
              onBulkDelete={handleBulkDelete}
            />
            <Divider sx={{ my: 2 }} />
            <EndpointsTable
              endpoints={filteredEndpoints}
              selectedIds={selectedIds}
              allSelected={allSelected}
              someSelected={someSelected}
              windowValue={windowValue}
              refreshSec={refreshSec}
              onSelectAll={handleSelectAll}
              onSelectRow={handleSelectRow}
              onToggle={handleToggle}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
            {loading && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Загрузка данных...
              </Typography>
            )}
          </CardContent>
        </Card>
      </Stack>

      <EndpointFormDialog
        open={dialogOpen}
        availableTags={availableTags}
        initial={editing}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
      <EndpointFormDialog
        open={bulkDialogOpen}
        availableTags={availableTags}
        initial={null}
        hideNameField
        count={bulkCount}
        onCountChange={setBulkCount}
        onClose={() => setBulkDialogOpen(false)}
        onSave={handleBulkSave}
      />
    </Container>
  );
}
