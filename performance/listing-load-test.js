#!/usr/bin/env node

const baseUrl = process.env.BASE_URL || "http://localhost:8082";
const listingId = process.env.LISTING_ID || "";
const durationSeconds = Number(process.env.DURATION_SECONDS || 30);
const concurrency = Number(process.env.CONCURRENCY || 8);

const scenarios = [
  { name: "active-listings", path: "/api/listings?status=ACTIVE&page=0&size=20" },
  { name: "search-filter", path: "/api/listings?category=ELECTRONICS&status=ACTIVE&page=0&size=20" },
];

if (listingId) {
  scenarios.push({ name: "listing-detail", path: `/api/listings/${listingId}` });
  scenarios.push({ name: "bid-validation", path: `/api/listings/${listingId}/validation` });
}

const results = {};
for (const scenario of scenarios) {
  results[scenario.name] = [];
}

function percentile(values, p) {
  if (values.length === 0) return 0;
  const sorted = [...values].sort((a, b) => a - b);
  const index = Math.ceil((p / 100) * sorted.length) - 1;
  return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
}

async function hitScenario(scenario) {
  const startedAt = performance.now();
  let ok = false;
  try {
    const response = await fetch(`${baseUrl}${scenario.path}`);
    ok = response.ok;
    await response.arrayBuffer();
  } catch {
    ok = false;
  }
  const durationMs = performance.now() - startedAt;
  results[scenario.name].push({ durationMs, ok });
}

async function worker(deadlineMs) {
  let index = 0;
  while (Date.now() < deadlineMs) {
    await hitScenario(scenarios[index % scenarios.length]);
    index += 1;
  }
}

async function main() {
  const deadlineMs = Date.now() + durationSeconds * 1000;
  await Promise.all(Array.from({ length: concurrency }, () => worker(deadlineMs)));

  console.log("scenario,requests,avg_ms,p95_ms,p99_ms,throughput_rps,error_rate");
  for (const [scenarioName, samples] of Object.entries(results)) {
    const durations = samples.map((sample) => sample.durationMs);
    const failures = samples.filter((sample) => !sample.ok).length;
    const average = durations.reduce((sum, value) => sum + value, 0) / Math.max(1, durations.length);
    const throughput = samples.length / durationSeconds;
    const errorRate = failures / Math.max(1, samples.length);
    console.log([
      scenarioName,
      samples.length,
      average.toFixed(3),
      percentile(durations, 95).toFixed(3),
      percentile(durations, 99).toFixed(3),
      throughput.toFixed(3),
      errorRate.toFixed(3),
    ].join(","));
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
