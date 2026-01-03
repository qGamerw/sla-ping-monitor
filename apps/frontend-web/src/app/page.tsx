import { Suspense } from "react";
import { CircularProgress, Stack } from "@mui/material";
import HomePageClient from "./home-page";

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
      <HomePageClient />
    </Suspense>
  );
}
