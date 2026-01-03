"use client";

import { Box } from "@mui/material";
import EndpointsListView from "../features/endpoints/list/EndpointsListView";

export default function HomePageClient() {
  return (
    <Box component="main" sx={{ pb: 6 }}>
      <EndpointsListView />
    </Box>
  );
}
