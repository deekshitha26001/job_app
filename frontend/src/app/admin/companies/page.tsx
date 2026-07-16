"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { PlusCircle, Trash2, Building2, ExternalLink, AlertCircle, Loader2 } from "lucide-react";
import { toast } from "sonner";

interface Company {
  id: string;
  name: string;
  atsProvider: string;
  atsApiUrl: string;
  careerPageUrl: string;
  logoUrl: string;
  active: boolean;
  createdAt: string;
}

const providerBadge: Record<string, string> = {
  GREENHOUSE: "bg-emerald-100 text-emerald-700 border-emerald-200",
  LEVER: "bg-orange-100 text-orange-700 border-orange-200",
  WORKDAY: "bg-blue-100 text-blue-700 border-blue-200",
  UNKNOWN: "bg-slate-100 text-slate-600 border-slate-200",
};

const providerLabel: Record<string, string> = {
  GREENHOUSE: "Greenhouse",
  LEVER: "Lever",
  WORKDAY: "Workday",
  UNKNOWN: "Unknown",
};

export default function CompaniesPage() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchCompanies = async () => {
    const token = localStorage.getItem("token");
    try {
      const res = await fetch("http://localhost:8080/api/admin/companies", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setCompanies(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load companies.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCompanies(); }, []);

  const handleDelete = async (id: string, name: string) => {
    if (!confirm(`Remove "${name}" from the platform?`)) return;
    setDeletingId(id);
    const token = localStorage.getItem("token");
    try {
      const res = await fetch(`http://localhost:8080/api/admin/companies/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error();
      toast.success(`"${name}" removed successfully.`);
      setCompanies((prev) => prev.filter((c) => c.id !== id));
    } catch {
      toast.error("Failed to delete company.");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="max-w-6xl">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
            ATS Integrations
          </p>
          <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">Companies</h1>
          <p className="mt-2 text-sm text-slate-500">
            All companies registered on the platform with their ATS API configurations.
          </p>
        </div>
        <Link
          href="/admin/companies/add"
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 active:scale-95 transition-all"
        >
          <PlusCircle size={17} />
          Add Company
        </Link>
      </div>

      {/* Stats strip */}
      <div className="mt-6 flex gap-6 text-sm text-slate-500">
        <span><strong className="text-slate-900">{companies.length}</strong> total</span>
        <span><strong className="text-slate-900">{companies.filter((c) => c.atsProvider === "GREENHOUSE").length}</strong> Greenhouse</span>
        <span><strong className="text-slate-900">{companies.filter((c) => c.atsProvider === "LEVER").length}</strong> Lever</span>
        <span><strong className="text-slate-900">{companies.filter((c) => c.atsProvider === "WORKDAY").length}</strong> Workday</span>
      </div>

      {/* Table */}
      <div className="mt-6 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        {loading ? (
          <div className="flex items-center justify-center gap-3 py-20 text-slate-400">
            <Loader2 className="animate-spin" size={22} />
            <span className="text-sm">Loading companies…</span>
          </div>
        ) : companies.length === 0 ? (
          <div className="flex flex-col items-center justify-center gap-3 py-20 text-slate-400">
            <Building2 size={36} />
            <p className="text-sm font-medium">No companies registered yet.</p>
            <Link
              href="/admin/companies/add"
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
            >
              Register your first company
            </Link>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b border-slate-100 bg-slate-50">
              <tr>
                <th className="px-5 py-3.5 text-left font-medium text-slate-500">Company</th>
                <th className="px-5 py-3.5 text-left font-medium text-slate-500">Provider</th>
                <th className="hidden px-5 py-3.5 text-left font-medium text-slate-500 lg:table-cell">ATS API URL</th>
                <th className="px-5 py-3.5 text-left font-medium text-slate-500">Status</th>
                <th className="px-5 py-3.5 text-right font-medium text-slate-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {companies.map((company) => {
                const initials = company.name.substring(0, 2).toUpperCase();
                return (
                  <tr key={company.id} className="group hover:bg-slate-50 transition-colors">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        {company.logoUrl ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img src={company.logoUrl} alt={company.name} className="h-8 w-8 rounded-lg object-contain bg-slate-100 p-0.5" />
                        ) : (
                          <div className="grid h-8 w-8 place-items-center rounded-lg bg-indigo-100 text-xs font-bold text-indigo-700">
                            {initials}
                          </div>
                        )}
                        <div>
                          <p className="font-semibold text-slate-900">{company.name}</p>
                          {company.careerPageUrl && (
                            <a
                              href={company.careerPageUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-xs text-slate-400 hover:text-indigo-500 flex items-center gap-0.5"
                            >
                              Careers page <ExternalLink size={10} />
                            </a>
                          )}
                        </div>
                      </div>
                    </td>

                    <td className="px-5 py-4">
                      <span className={`inline-block rounded-full border px-2.5 py-0.5 text-xs font-semibold ${providerBadge[company.atsProvider] ?? providerBadge.UNKNOWN}`}>
                        {providerLabel[company.atsProvider] ?? company.atsProvider}
                      </span>
                    </td>

                    <td className="hidden max-w-[260px] px-5 py-4 lg:table-cell">
                      <a
                        href={company.atsApiUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="block truncate text-slate-500 hover:text-indigo-500 text-xs font-mono"
                      >
                        {company.atsApiUrl}
                      </a>
                    </td>

                    <td className="px-5 py-4">
                      <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${company.active ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
                        {company.active ? "Active" : "Inactive"}
                      </span>
                    </td>

                    <td className="px-5 py-4 text-right">
                      <button
                        onClick={() => handleDelete(company.id, company.name)}
                        disabled={deletingId === company.id}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-red-200 px-2.5 py-1.5 text-xs font-medium text-red-600 transition hover:bg-red-50 disabled:opacity-50"
                        aria-label={`Delete ${company.name}`}
                      >
                        {deletingId === company.id ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                        Delete
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
