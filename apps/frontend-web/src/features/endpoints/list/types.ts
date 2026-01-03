import type {
  EndpointResponse,
  EndpointSummaryResponse,
} from "../../../shared/api/types";

export interface EndpointRow extends EndpointResponse {
  summary?: EndpointSummaryResponse;
}
