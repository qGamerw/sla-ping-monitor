import { loadConfig } from "./config";

const config = loadConfig();

// TODO: wire evaluator and notifier pipeline.
console.log("Alerting worker starting", config);
