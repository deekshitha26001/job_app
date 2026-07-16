"use client";

import { useState, useEffect } from "react";
import { Search, Loader2, AlertCircle, Filter } from "lucide-react";
import { AtsJobCard, AtsJob } from "@/components/ats-job-card";

export default function Jobs() {
  const [jobs, setJobs] = useState<AtsJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [query, setQuery] = useState("");
  const [provider, setProvider] = useState("All");

  useEffect(() => {
    const token = localStorage.getItem("token");
    fetch("http://localhost:8080/api/jobs", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((r) => {
        if (!r.ok) throw new Error();
        return r.json();
      })
      .then((data) => setJobs(Array.isArray(data) ? data : []))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const providers = ["All", ...Array.from(new Set(jobs.map((j) => j.atsProvider)))];

  const shown = jobs.filter((j) => {
    const matchQuery =
      j.title.toLowerCase().includes(query.toLowerCase()) ||
      j.companyName.toLowerCase().includes(query.toLowerCase()) ||
      (j.location || "").toLowerCase().includes(query.toLowerCase());
    const matchProvider = provider === "All" || j.atsProvider === provider;
    return matchQuery && matchProvider;
  });

  return (
    <div className="mx-auto max-w-6xl">
      <p className="text-sm font-medium text-violet-600">OPPORTUNITIES</p>
      <h1 className="mt-1 text-3xl font-semibold tracking-tight">Find your next role.</h1>

      {/* Search bar */}
      <div className="mt-7 flex flex-col gap-3 rounded-2xl border border-zinc-200 bg-white p-3 sm:flex-row dark:border-zinc-800 dark:bg-zinc-900">
        <label className="flex flex-1 items-center gap-2 px-2">
          <Search size={18} className="text-zinc-400" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="h-10 w-full bg-transparent text-sm outline-none"
            placeholder="Search title, company or location…"
          />
        </label>
      </div>

      {/* Provider filters */}
      {!loading && !error && providers.length > 1 && (
        <div className="mt-5 flex flex-wrap gap-2">
          {providers.map((p) => (
            <button
              key={p}
              onClick={() => setProvider(p)}
              className={`rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${
                provider === p
                  ? "bg-violet-100 text-violet-700 dark:bg-violet-950 dark:text-violet-300"
                  : "bg-zinc-100 text-zinc-600 hover:bg-zinc-200 dark:bg-zinc-800 dark:text-zinc-300"
              }`}
            >
              {p === "GREENHOUSE" ? "Greenhouse" : p === "LEVER" ? "Lever" : p === "WORKDAY" ? "Workday" : p}
            </button>
          ))}
        </div>
      )}

      {/* States */}
      {loading && (
        <div className="mt-16 flex flex-col items-center gap-3 text-zinc-400">
          <Loader2 className="animate-spin" size={32} />
          <p className="text-sm">Fetching live jobs from all registered companies…</p>
        </div>
      )}

      {!loading && error && (
        <div className="mt-16 flex flex-col items-center gap-3 text-zinc-500">
          <AlertCircle size={32} />
          <p className="text-sm font-medium">Failed to load jobs. Please try again later.</p>
        </div>
      )}

      {!loading && !error && shown.length === 0 && (
        <div className="mt-16 flex flex-col items-center gap-3 text-zinc-500">
          <Filter size={32} />
          <p className="text-sm font-medium">
            {jobs.length === 0
              ? "No jobs yet. Ask your admin to register some companies."
              : "No roles match those filters. Try broadening your search."}
          </p>
        </div>
      )}

      {!loading && !error && shown.length > 0 && (
        <>
          <p className="mt-6 text-sm text-zinc-500">
            Showing <strong className="text-zinc-900 dark:text-white">{shown.length}</strong> live {shown.length === 1 ? "role" : "roles"}
          </p>
          <div className="mt-4 grid gap-5 lg:grid-cols-2">
            {shown.map((job) => (
              <AtsJobCard key={`${job.companyName}-${job.id}`} job={job} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
