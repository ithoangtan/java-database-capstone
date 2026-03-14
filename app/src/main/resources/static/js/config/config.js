// config.js

/**
 * Base URL for API requests. Use empty string for same-origin (recommended when FE is served by same app).
 */
export const API_BASE_URL = typeof window !== "undefined" && window.location ? "" : "http://localhost:8080";
