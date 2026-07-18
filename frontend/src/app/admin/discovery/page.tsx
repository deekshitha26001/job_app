"use client";

import { useEffect, useState, useCallback } from "react";
import {
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
  Search,
  RefreshCw,
  Zap,
} from "lucide-react";

interface PendingCompany {
  id: string;
  companyName: string;
  officialWebsite: string;
  careerPageUrl: string | null;
  detectedAts: string;
  atsConfidence: string;
  matchedPattern: string | null;
  detectionMethod: string | null;
  industry: string;
  country: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt: string;
}

interface DiscoverySummary {
  companiesFound: number;
  submitted: number;
  skipped: number;
  failed: number;
}

type StatusFilter = "PENDING" | "APPROVED" | "REJECTED" | "ALL";

const atsBadge: Record<string, string> = {
  GREENHOUSE: "bg-emerald-100 text-emerald-700",
  LEVER: "bg-orange-100 text-orange-700",
  WORKDAY: "bg-blue-100 text-blue-700",
  ASHBY: "bg-violet-100 text-violet-700",
  SMARTRECRUITERS: "bg-pink-100 text-pink-700",
  UNKNOWN: "bg-slate-100 text-slate-500",
};

const confidenceBadge: Record<string, string> = {
  HIGH: "bg-emerald-100 text-emerald-700",
  MEDIUM: "bg-amber-100 text-amber-700",
  LOW: "bg-red-100 text-red-600",
};

const statusBadge: Record<string, string> = {
  PENDING: "bg-amber-100 text-amber-700",
  APPROVED: "bg-emerald-100 text-emerald-700",
  REJECTED: "bg-red-100 text-red-600",
};

const statusIcon: Record<string, React.ReactNode> = {
  PENDING: <Clock size={12} />,
  APPROVED: <CheckCircle size={12} />,
  REJECTED: <XCircle size={12} />,
};

export default function CompanyDiscoveryPage() {
  const [companies, setCompanies] = useState<PendingCompany[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<StatusFilter>("PENDING");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [bulkLoading, setBulkLoading] = useState(false);
  const [discovering, setDiscovering] = useState(false);
  const [discoverySummary, setDiscoverySummary] = useState<DiscoverySummary | null>(null);
  const [discoveryError, setDiscoveryError] = useState<string | null>(null);

  const token = () => localStorage.getItem("token");

  const fetchCompanies = useCallback(async () => {
    setLoading(true);
    setSelected(new Set());
    try {
      const url =
        filter === "ALL"
          ? "http://localhost:8080/api/review"
          : filter === "PENDING"
          ? "http://localhost:8080/api/review/pending"
          : `http://localhost:8080/api/review/status/${filter}`;

      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token()}` },
      });
      const data = await res.json();
      setCompanies(Array.isArray(data) ? data : []);
    } catch {
      setCompanies([]);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    fetchCompanies();
  }, [fetchCompanies]);

  const handleRunDiscovery = async () => {
    setDiscovering(true);
    setDiscoverySummary(null);
    setDiscoveryError(null);
    try {
      const res = await fetch("http://localhost:8080/api/discovery/run", {
        method: "POST",
        headers: { Authorization: `Bearer ${token()}` },
      });
      if (!res.ok) throw new Error(`Server returned ${res.status}`);
      const summary: DiscoverySummary = await res.json();
      setDiscoverySummary(summary);
      // Auto-refresh queue after discovery
      await fetchCompanies();
    } catch (e: unknown) {
      setDiscoveryError(e instanceof Error ? e.message : "Discovery failed");
    } finally {
      setDiscovering(false);
    }
  };

  const handleApprove = async (id: string) => {
    setActionLoading(id + "_approve");
    try {
      await fetch(`http://localhost:8080/api/review/${id}/approve`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token()}` },
      });
      await fetchCompanies();
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (id: string) => {
    setActionLoading(id + "_reject");
    try {
      await fetch(`http://localhost:8080/api/review/${id}/reject`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token()}` },
      });
      await fetchCompanies();
    } finally {
      setActionLoading(null);
    }
  };

  const handleBulkApprove = async () => {
    setBulkLoading(true);
    try {
      await fetch("http://localhost:8080/api/review/bulk-approve", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token()}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(Array.from(selected)),
      });
      await fetchCompanies();
    } finally {
      setBulkLoading(false);
    }
  };

  const handleBulkReject = async () => {
    setBulkLoading(true);
    try {
      await fetch("http://localhost:8080/api/review/bulk-reject", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token()}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(Array.from(selected)),
      });
      await fetchCompanies();
    } finally {
      setBulkLoading(false);
    }
  };

  const toggleSelect = (id: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    const pendingIds = companies
      .filter((c) => c.status === "PENDING")
      .map((c) => c.id);
    if (pendingIds.every((id) => selected.has(id))) {
      setSelected(new Set());
    } else {
      setSelected(new Set(pendingIds));
    }
  };

  const pendingCompanies = companies.filter((c) => c.status === "PENDING");
  const allPendingSelected =
    pendingCompanies.length > 0 &&
    pendingCompanies.every((c) => selected.has(c.id));

  const tabs: StatusFilter[] = ["PENDING", "APPROVED", "REJECTED", "ALL"];

  return (
    <div className="max-w-7xl">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
            Discovery Pipeline
          </p>
          <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">
            Company Discovery
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            Run the discovery pipeline to find companies, then approve or reject
            them for the registry.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            id="run-discovery-btn"
            onClick={handleRunDiscovery}
            disabled={discovering}
            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {discovering ? (
              <RefreshCw size={14} className="animate-spin" />
            ) : (
              <Zap size={14} />
            )}
            {discovering ? "Discovering…" : "Discover Companies"}
          </button>
          <button
            id="refresh-btn"
            onClick={fetchCompanies}
            disabled={loading}
            className="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 shadow-sm hover:bg-slate-50 disabled:opacity-60"
          >
            <RefreshCw size={14} className={loading ? "animate-spin" : ""} />
            Refresh
          </button>
        </div>
      </div>

      {/* Discovery Summary Banner */}
      {discoverySummary && (
        <div className="mt-4 flex items-center gap-6 rounded-xl border border-emerald-200 bg-emerald-50 px-5 py-3 text-sm">
          <CheckCircle size={18} className="shrink-0 text-emerald-600" />
          <div className="flex gap-6">
            <span>
              <span className="font-bold text-slate-900">{discoverySummary.companiesFound}</span>
              <span className="ml-1 text-slate-600">found</span>
            </span>
            <span>
              <span className="font-bold text-emerald-700">{discoverySummary.submitted}</span>
              <span className="ml-1 text-slate-600">submitted</span>
            </span>
            <span>
              <span className="font-bold text-slate-500">{discoverySummary.skipped}</span>
              <span className="ml-1 text-slate-600">skipped</span>
            </span>
            <span>
              <span className="font-bold text-red-500">{discoverySummary.failed}</span>
              <span className="ml-1 text-slate-600">failed</span>
            </span>
          </div>
          <button
            onClick={() => setDiscoverySummary(null)}
            className="ml-auto text-slate-400 hover:text-slate-700"
          >
            <XCircle size={15} />
          </button>
        </div>
      )}

      {/* Discovery Error Banner */}
      {discoveryError && (
        <div className="mt-4 flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 px-5 py-3 text-sm text-red-700">
          <AlertCircle size={16} className="shrink-0" />
          <span>{discoveryError}</span>
          <button
            onClick={() => setDiscoveryError(null)}
            className="ml-auto text-red-400 hover:text-red-700"
          >
            <XCircle size={15} />
          </button>
        </div>
      )}

      {/* Status tabs */}
      <div className="mt-6 flex gap-1 rounded-xl border border-slate-200 bg-slate-100 p-1 w-fit">
        {tabs.map((tab) => (
          <button
            key={tab}
            id={`tab-${tab.toLowerCase()}`}
            onClick={() => setFilter(tab)}
            className={`rounded-lg px-4 py-1.5 text-xs font-semibold transition-all ${
              filter === tab
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-500 hover:text-slate-800"
            }`}
          >
            {tab.charAt(0) + tab.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {/* Bulk actions (only when items selected) */}
      {selected.size > 0 && (
        <div className="mt-4 flex items-center gap-3 rounded-xl border border-indigo-100 bg-indigo-50 px-5 py-3">
          <span className="text-sm font-medium text-indigo-700">
            {selected.size} selected
          </span>
          <button
            id="bulk-approve-btn"
            onClick={handleBulkApprove}
            disabled={bulkLoading}
            className="flex items-center gap-1.5 rounded-lg bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            <CheckCircle size={13} />
            Approve Selected
          </button>
          <button
            id="bulk-reject-btn"
            onClick={handleBulkReject}
            disabled={bulkLoading}
            className="flex items-center gap-1.5 rounded-lg bg-red-500 px-4 py-1.5 text-xs font-semibold text-white hover:bg-red-600 disabled:opacity-60"
          >
            <XCircle size={13} />
            Reject Selected
          </button>
        </div>
      )}

      {/* Table */}
      <div className="mt-4 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        {loading ? (
          <div className="flex items-center justify-center gap-2 py-20 text-sm text-slate-400">
            <RefreshCw size={16} className="animate-spin" /> Loading…
          </div>
        ) : companies.length === 0 ? (
          <div className="flex flex-col items-center justify-center gap-3 py-20 text-slate-400">
            <Search size={32} />
            <p className="text-sm font-medium">No companies in this queue.</p>
            <p className="text-xs">
              Run the Company Discovery pipeline to populate this list.
            </p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b border-slate-100 bg-slate-50">
              <tr>
                {filter === "PENDING" || filter === "ALL" ? (
                  <th className="w-10 px-4 py-3">
                    <input
                      type="checkbox"
                      id="select-all-checkbox"
                      checked={allPendingSelected}
                      onChange={toggleSelectAll}
                      className="h-4 w-4 rounded border-slate-300 accent-indigo-600"
                    />
                  </th>
                ) : (
                  <th className="w-10" />
                )}
                <th className="px-4 py-3 text-left font-medium text-slate-500">
                  Company
                </th>
                <th className="hidden px-4 py-3 text-left font-medium text-slate-500 md:table-cell">
                  Career Page
                </th>
                <th className="px-4 py-3 text-left font-medium text-slate-500">
                  Detected ATS
                </th>
                <th className="px-4 py-3 text-left font-medium text-slate-500">
                  Confidence
                </th>
                <th className="px-4 py-3 text-left font-medium text-slate-500">
                  Status
                </th>
                <th className="px-4 py-3 text-left font-medium text-slate-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {companies.map((c) => (
                <tr
                  key={c.id}
                  className={`transition hover:bg-slate-50 ${
                    selected.has(c.id) ? "bg-indigo-50/50" : ""
                  }`}
                >
                  <td className="px-4 py-3">
                    {c.status === "PENDING" && (
                      <input
                        type="checkbox"
                        id={`select-${c.id}`}
                        checked={selected.has(c.id)}
                        onChange={() => toggleSelect(c.id)}
                        className="h-4 w-4 rounded border-slate-300 accent-indigo-600"
                      />
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="font-semibold text-slate-900">
                      {c.companyName}
                    </div>
                    {c.officialWebsite && (
                      <a
                        href={c.officialWebsite}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-xs text-indigo-500 hover:underline"
                      >
                        {c.officialWebsite}
                      </a>
                    )}
                  </td>
                  <td className="hidden max-w-xs px-4 py-3 md:table-cell">
                    {c.careerPageUrl ? (
                      <a
                        href={c.careerPageUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="truncate text-xs text-slate-500 hover:text-indigo-600 hover:underline"
                      >
                        {c.careerPageUrl}
                      </a>
                    ) : (
                      <span className="flex items-center gap-1 text-xs text-slate-400">
                        <AlertCircle size={12} /> Not found
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                        atsBadge[c.detectedAts] ?? atsBadge.UNKNOWN
                      }`}
                    >
                      {c.detectedAts}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                        confidenceBadge[c.atsConfidence] ?? confidenceBadge.LOW
                      }`}
                    >
                      {c.atsConfidence}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`flex w-fit items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                        statusBadge[c.status]
                      }`}
                    >
                      {statusIcon[c.status]}
                      {c.status.charAt(0) + c.status.slice(1).toLowerCase()}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {c.status === "PENDING" ? (
                      <div className="flex items-center gap-2">
                        <button
                          id={`approve-${c.id}`}
                          onClick={() => handleApprove(c.id)}
                          disabled={actionLoading !== null}
                          className="flex items-center gap-1 rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
                        >
                          <CheckCircle size={12} />
                          Approve
                        </button>
                        <button
                          id={`reject-${c.id}`}
                          onClick={() => handleReject(c.id)}
                          disabled={actionLoading !== null}
                          className="flex items-center gap-1 rounded-lg border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-600 hover:bg-red-100 disabled:opacity-60"
                        >
                          <XCircle size={12} />
                          Reject
                        </button>
                      </div>
                    ) : (
                      <span className="text-xs text-slate-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
