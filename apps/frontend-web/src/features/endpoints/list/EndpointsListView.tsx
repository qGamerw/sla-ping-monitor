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
    editing,
    query,
    selectedIds,
    selectedCount,
    allSelected,
    someSelected,
    loading,
    error,
    setQuery,
    handleWindowChange,
    handleRefreshChange,
    handleRefreshNow,
    handleCreate,
    handleEdit,
    handleSave,
    handleDelete,
    handleToggle,
    handleDuplicate,
    handleTagsChange,
    handleSelectAll,
    handleSelectRow,
    handleBulkToggle,
    handleBulkDelete,
    setDialogOpen,
  } = useEndpointsList();

  return (
    <Container maxWidth="lg" sx={{ pt: 4 }}>
      <Stack spacing={3}>
        <EndpointsHeader onCreate={handleCreate} />

        {error && <Alert severity="error">{error}</Alert>}

        <EndpointsMetricsCard
          windowValue={windowValue}
          refreshSec={refreshSec}
          onWindowChange={handleWindowChange}
          onRefreshChange={handleRefreshChange}
          onRefreshNow={handleRefreshNow}
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
              onDuplicate={handleDuplicate}
              onTagsChange={handleTagsChange}
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
    </Container>
  );
}
