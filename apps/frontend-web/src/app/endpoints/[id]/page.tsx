import { Suspense } from "react";
import { CircularProgress, Stack } from "@mui/material";
import EndpointDetailsClient from "./endpoint-details-client";

function LoadingFallback() {
  return (
    <Stack alignItems="center" justifyContent="center" sx={{ minHeight: "60vh" }}>
      <CircularProgress />
    </Stack>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<LoadingFallback />}>
      <EndpointDetailsClient />
    </Suspense>
  );
}
