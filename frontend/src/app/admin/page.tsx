"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Building2, Briefcase, PlusCircle, ArrowRight, Zap, Globe, AlertCircle } from "lucide-react";

interface Company {
  id: string;
  name: string;
  atsProvider: string;
  atsApiUrl: string;
  active: boolean;
  createdAt: string;
}

const providerBadge: Record<string, string> = {
  GREENHOUSE: "bg-emerald-100 text-emerald-700",
  LEVER: "bg-orange-100 text-orange-700",
  WORKDAY: "bg-blue-100 text-blue-700",
  UNKNOWN: "bg-slate-100 text-slate-600",
};

export default function AdminOverview() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [jobCount, setJobCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");
    Promise.all([
      fetch("http://localhost:8080/api/admin/companies", {
        headers: { Authorization: `Bearer ${token}` },
      }).then((r) => r.json()),
      fetch("http://localhost:8080/api/jobs", {
        headers: { Authorization: `Bearer ${token}` },
      }).then((r) => r.json()),
    ])
      .then(([cos, jobs]) => {
        setCompanies(Array.isArray(cos) ? cos : []);
        setJobCount(Array.isArray(jobs) ? jobs.length : 0);
      })
      .catch(() => {
        setCompanies([]);
        setJobCount(0);
      })
      .finally(() => setLoading(false));
  }, []);

  const providerCounts = companies.reduce<Record<string, number>>((acc, c) => {
    acc[c.atsProvider] = (acc[c.atsProvider] || 0) + 1;
    return acc;
  }, {});

  const stats = [
    {
      label: "Registered Companies",
      value: loading ? "—" : companies.length,
      icon: Building2,
      color: "text-indigo-600",
      bg: "bg-indigo-50",
    },
    {
      label: "Live Jobs Available",
      value: loading ? "—" : jobCount ?? "—",
      icon: Briefcase,
      color: "text-emerald-600",
      bg: "bg-emerald-50",
    },
    {
      label: "ATS Providers",
      value: loading ? "—" : Object.keys(providerCounts).filter((k) => k !== "UNKNOWN").length,
      icon: Zap,
      color: "text-orange-600",
      bg: "bg-orange-50",
    },
    {
      label: "Active Integrations",
      value: loading ? "—" : companies.filter((c) => c.active).length,
      icon: Globe,
      color: "text-violet-600",
      bg: "bg-violet-50",
    },
  ];

  return (
    <div className="max-w-6xl">
      <div>
        <p className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
          Admin Dashboard
        </p>
        <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">
          Platform Overview
        </h1>
        <p className="mt-2 text-sm text-slate-500">
          Manage ATS integrations and monitor live job feeds from registered companies.
        </p>
      </div>

      {/* Stats */}
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map(({ label, value, icon: Icon, color, bg }) => (
          <div
            key={label}
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <div className={`inline-grid h-10 w-10 place-items-center rounded-xl ${bg}`}>
              <Icon size={20} className={color} />
            </div>
            <p className="mt-4 text-2xl font-bold text-slate-900">{String(value)}</p>
            <p className="mt-0.5 text-sm text-slate-500">{label}</p>
          </div>
        ))}
      </div>

      {/* Quick actions */}
      <div className="mt-8 grid gap-4 sm:grid-cols-2">
        <Link
          href="/admin/companies/add"
          className="group flex items-center justify-between rounded-2xl border border-indigo-100 bg-indigo-50 p-5 transition hover:border-indigo-200 hover:bg-indigo-100"
        >
          <div className="flex items-center gap-4">
            <div className="grid h-11 w-11 place-items-center rounded-xl bg-indigo-600 text-white">
              <PlusCircle size={22} />
            </div>
            <div>
              <p className="font-semibold text-slate-900">Add Company</p>
              <p className="text-sm text-slate-500">Register a single ATS URL or bulk import</p>
            </div>
          </div>
          <ArrowRight size={18} className="text-indigo-400 transition group-hover:translate-x-1" />
        </Link>

        <Link
          href="/admin/companies"
          className="group flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-5 transition hover:border-slate-300"
        >
          <div className="flex items-center gap-4">
            <div className="grid h-11 w-11 place-items-center rounded-xl bg-slate-100 text-slate-600">
              <Building2 size={22} />
            </div>
            <div>
              <p className="font-semibold text-slate-900">View Companies</p>
              <p className="text-sm text-slate-500">Manage all registered ATS companies</p>
            </div>
          </div>
          <ArrowRight size={18} className="text-slate-400 transition group-hover:translate-x-1" />
        </Link>
      </div>

      {/* Recent companies */}
      <div className="mt-10">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900">Recently Added</h2>
          <Link href="/admin/companies" className="text-sm font-medium text-indigo-600 hover:underline">
            View all →
          </Link>
        </div>

        <div className="mt-4 overflow-hidden rounded-2xl border border-slate-200 bg-white">
          {loading ? (
            <div className="py-16 text-center text-sm text-slate-400">Loading companies…</div>
          ) : companies.length === 0 ? (
            <div className="flex flex-col items-center justify-center gap-3 py-16 text-slate-400">
              <AlertCircle size={32} />
              <p className="text-sm font-medium">No companies registered yet.</p>
              <Link
                href="/admin/companies/add"
                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
              >
                Add your first company
              </Link>
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead className="border-b border-slate-100 bg-slate-50">
                <tr>
                  <th className="px-5 py-3 text-left font-medium text-slate-500">Company</th>
                  <th className="px-5 py-3 text-left font-medium text-slate-500">Provider</th>
                  <th className="px-5 py-3 text-left font-medium text-slate-500 hidden sm:table-cell">ATS URL</th>
                  <th className="px-5 py-3 text-left font-medium text-slate-500">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {companies.slice(0, 5).map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50">
                    <td className="px-5 py-3 font-medium text-slate-900">{c.name}</td>
                    <td className="px-5 py-3">
                      <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${providerBadge[c.atsProvider] ?? providerBadge.UNKNOWN}`}>
                        {c.atsProvider}
                      </span>
                    </td>
                    <td className="hidden max-w-xs truncate px-5 py-3 text-slate-500 sm:table-cell">
                      {c.atsApiUrl}
                    </td>
                    <td className="px-5 py-3">
                      <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${c.active ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
                        {c.active ? "Active" : "Inactive"}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
