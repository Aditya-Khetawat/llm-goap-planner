export function sanitizeText(value: string): string {
  return value.replace(/[\u0000-\u001F\u007F]/g, "").trim();
}

export function truncateText(value: string, maxLength: number): string {
  const sanitized = sanitizeText(value);

  if (sanitized.length <= maxLength) {
    return sanitized;
  }

  return `${sanitized.slice(0, maxLength - 1).trimEnd()}…`;
}
