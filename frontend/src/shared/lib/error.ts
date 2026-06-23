import axios from "axios";

import { isRecord } from "@shared/lib/guard";
import { sanitizeText } from "@shared/lib/sanitize";

function isClientError(status?: number): boolean {
  return typeof status === "number" && status >= 400 && status < 500;
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data;
    const status = error.response?.status;

    if (!error.response) {
      return "Network request failed. Check your connection and try again.";
    }

    if (status && status >= 500) {
      return "The server could not complete the request. Please try again later.";
    }

    if (isRecord(responseData)) {
      const message = responseData.message;
      const detail = responseData.error;

      if (typeof message === "string" && message.length > 0) {
        return sanitizeText(message);
      }

      if (typeof detail === "string" && detail.length > 0 && isClientError(status)) {
        return sanitizeText(detail);
      }
    }

    return "The request could not be completed.";
  }

  if (error instanceof Error) {
    return sanitizeText(error.message) || "An unexpected error occurred.";
  }

  return "An unexpected error occurred";
}
