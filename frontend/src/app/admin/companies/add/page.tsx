"use client";

import { useState, useRef, useCallback } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import {
  PlusCircle,
  UploadCloud,
  FileSpreadsheet,
  Loader2,
  CheckCircle2,
  AlertCircle,
  Zap,
  Trash2,
} from "lucide-react";
import * as XLSX from "xlsx";

type Tab = "single" | "excel";

interface ParsedRow {
  name: string;
  atsApiUrl: string;
  careerPageUrl: string;
  logoUrl: string;
}

function detectProvider(url: string): string {
  const lower = url.toLowerCase();
  if (lower.includes("greenhouse.io")) return "GREENHOUSE";
  if (lower.includes("lever.co")) return "LEVER";
  if (lower.includes("workday.com") || lower.includes("myworkdayjobs.com")) return "WORKDAY";
  return "UNKNOWN";
}

const providerBadge: Record<string, string> = {
  GREENHOUSE: "bg-emerald-100 text-emerald-700",
  LEVER: "bg-orange-100 text-orange-700",
  WORKDAY: "bg-blue-100 text-blue-700",
  UNKNOWN: "bg-slate-100 text-slate-500",
};

export default function AddCompanyPage() {
  const router = useRouter();
  const [tab, setTab] = useState<Tab>("single");

  // Single form state
  const [singleForm, setSingleForm] = useState({
    name: "",
    atsApiUrl: "",
    careerPageUrl: "",
    logoUrl: "",
  });
  const [submitting, setSingleSubmitting] = useState(false);

  // Excel import state
  const [parsedRows, setParsedRows] = useState<ParsedRow[]>([]);
  const [dragActive, setDragActive] = useState(false);
  const [fileName, setFileName] = useState<string | null>(null);
  const [importing, setImporting] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const detectedProvider = detectProvider(singleForm.atsApiUrl);

  // ---- Single company submit ----
  const handleSingleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!singleForm.name.trim() || !singleForm.atsApiUrl.trim()) {
      toast.error("Company name and ATS API URL are required.");
      return;
    }
    setSingleSubmitting(true);
    const token = localStorage.getItem("token");
    try {
      const res = await fetch("http://localhost:8080/api/admin/companies", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(singleForm),
      });
      if (!res.ok) throw new Error();
      toast.success(`"${singleForm.name}" registered successfully!`);
      router.push("/admin/companies");
    } catch {
      toast.error("Failed to add company. Please check the details.");
    } finally {
      setSingleSubmitting(false);
    }
  };

  // ---- Excel parsing ----
  const parseExcel = useCallback((file: File) => {
    setFileName(file.name);
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: "array" });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        const rows = XLSX.utils.sheet_to_json<string[]>(sheet, { header: 1 }) as string[][];

        // Skip header row (row 0), map remaining
        const parsed: ParsedRow[] = rows
          .slice(1)
          .filter((row) => row[0] && row[1]) // must have name + ATS URL
          .map((row) => ({
            name: String(row[0] || "").trim(),
            atsApiUrl: String(row[1] || "").trim(),
            careerPageUrl: String(row[2] || "").trim(),
            logoUrl: String(row[3] || "").trim(),
          }));

        setParsedRows(parsed);
        if (parsed.length === 0) {
          toast.error("No valid rows found. Ensure the sheet has Company Name and ATS API URL columns.");
        } else {
          toast.success(`Parsed ${parsed.length} companies from the sheet.`);
        }
      } catch {
        toast.error("Failed to parse the Excel file. Please check the format.");
      }
    };
    reader.readAsArrayBuffer(file);
  }, []);

  const handleFileDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);
    if (e.dataTransfer.files[0]) parseExcel(e.dataTransfer.files[0]);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) parseExcel(e.target.files[0]);
  };

  const removeRow = (i: number) => setParsedRows((prev) => prev.filter((_, idx) => idx !== i));

  // ---- Bulk import submit ----
  const handleBulkImport = async () => {
    if (parsedRows.length === 0) return;
    setImporting(true);
    const token = localStorage.getItem("token");
    try {
      const res = await fetch("http://localhost:8080/api/admin/companies/bulk", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(parsedRows),
      });
      if (!res.ok) throw new Error();
      toast.success(`Successfully imported ${parsedRows.length} companies!`);
      router.push("/admin/companies");
    } catch {
      toast.error("Bulk import failed. Please try again.");
    } finally {
      setImporting(false);
    }
  };

  return (
    <div className="max-w-3xl">
      <div>
        <p className="text-xs font-semibold uppercase tracking-widest text-indigo-600">
          ATS Integrations
        </p>
        <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">Add Company</h1>
        <p className="mt-2 text-sm text-slate-500">
          Register a single company or bulk import from an Excel sheet.
        </p>
      </div>

      {/* Tab switcher */}
      <div className="mt-8 flex gap-1 rounded-xl bg-slate-100 p-1">
        {(["single", "excel"] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`flex-1 rounded-lg py-2 text-sm font-semibold transition-all ${
              tab === t
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-500 hover:text-slate-700"
            }`}
          >
            {t === "single" ? (
              <span className="flex items-center justify-center gap-2">
                <PlusCircle size={16} /> Single Company
              </span>
            ) : (
              <span className="flex items-center justify-center gap-2">
                <FileSpreadsheet size={16} /> Bulk Excel Import
              </span>
            )}
          </button>
        ))}
      </div>

      {/* ── SINGLE COMPANY FORM ── */}
      {tab === "single" && (
        <form
          onSubmit={handleSingleSubmit}
          className="mt-6 space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <div>
            <label className="block text-sm font-medium text-slate-700">
              Company Name <span className="text-red-500">*</span>
            </label>
            <input
              value={singleForm.name}
              onChange={(e) => setSingleForm((p) => ({ ...p, name: e.target.value }))}
              placeholder="e.g. Postman, Figma, Razorpay"
              className="mt-1.5 h-11 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700">
              ATS API URL <span className="text-red-500">*</span>
            </label>
            <div className="mt-1.5 flex gap-2">
              <input
                value={singleForm.atsApiUrl}
                onChange={(e) => setSingleForm((p) => ({ ...p, atsApiUrl: e.target.value }))}
                placeholder="https://boards-api.greenhouse.io/v1/boards/company/jobs"
                className="h-11 flex-1 rounded-lg border border-slate-200 px-3 font-mono text-xs outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition"
              />
              {singleForm.atsApiUrl && (
                <div className={`flex items-center gap-1.5 rounded-lg px-3 text-xs font-semibold ${providerBadge[detectedProvider]}`}>
                  <Zap size={12} />
                  {detectedProvider}
                </div>
              )}
            </div>
            <p className="mt-1.5 text-xs text-slate-400">
              Supported: Greenhouse <code>boards-api.greenhouse.io</code> · Lever <code>api.lever.co</code> · Workday
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Career Page URL <span className="text-slate-400 font-normal">(optional)</span>
              </label>
              <input
                value={singleForm.careerPageUrl}
                onChange={(e) => setSingleForm((p) => ({ ...p, careerPageUrl: e.target.value }))}
                placeholder="https://company.com/careers"
                className="mt-1.5 h-11 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Logo URL <span className="text-slate-400 font-normal">(optional)</span>
              </label>
              <input
                value={singleForm.logoUrl}
                onChange={(e) => setSingleForm((p) => ({ ...p, logoUrl: e.target.value }))}
                placeholder="https://example.com/logo.png"
                className="mt-1.5 h-11 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100 transition"
              />
            </div>
          </div>

          {/* Preview row */}
          {singleForm.name && singleForm.atsApiUrl && (
            <div className="flex items-center gap-3 rounded-xl border border-indigo-100 bg-indigo-50 p-4">
              <CheckCircle2 size={18} className="text-indigo-500 shrink-0" />
              <div>
                <p className="text-sm font-semibold text-slate-900">{singleForm.name}</p>
                <p className="text-xs text-slate-500">
                  Provider: <span className={`font-semibold ${detectedProvider === "UNKNOWN" ? "text-slate-500" : "text-indigo-600"}`}>{detectedProvider}</span>
                </p>
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 text-sm font-bold text-white shadow-sm hover:bg-indigo-700 disabled:opacity-60 active:scale-[0.99] transition-all"
          >
            {submitting ? <Loader2 size={17} className="animate-spin" /> : <PlusCircle size={17} />}
            Register Company
          </button>
        </form>
      )}

      {/* ── EXCEL IMPORT ── */}
      {tab === "excel" && (
        <div className="mt-6 space-y-5">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-sm font-semibold text-slate-900">Expected Excel Format</h3>
            <div className="mt-3 overflow-x-auto rounded-lg border border-slate-200">
              <table className="w-full text-xs">
                <thead className="bg-yellow-300">
                  <tr>
                    {["Company Name", "ATS API URL", "Career Page URL", "Logo URL"].map((h) => (
                      <th key={h} className="px-4 py-2 text-left font-bold text-slate-800">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    ["Postman", "https://boards-api.greenhouse.io/v1/boards/postman/jobs", "", ""],
                    ["Figma", "https://boards-api.greenhouse.io/v1/boards/figma/jobs", "https://figma.com/careers", ""],
                  ].map((row, i) => (
                    <tr key={i} className="hover:bg-slate-50">
                      {row.map((cell, j) => (
                        <td key={j} className={`px-4 py-2 ${j === 1 ? "text-blue-600" : ""}`}>{cell || <span className="text-slate-300">—</span>}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Drop zone */}
          <input
            ref={fileInputRef}
            type="file"
            accept=".xlsx,.xls,.csv"
            className="hidden"
            onChange={handleFileChange}
          />
          <div
            onDragEnter={(e) => { e.preventDefault(); setDragActive(true); }}
            onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
            onDragLeave={() => setDragActive(false)}
            onDrop={handleFileDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed p-12 transition-all ${
              dragActive
                ? "border-indigo-500 bg-indigo-50 scale-[1.01]"
                : "border-slate-200 bg-slate-50 hover:border-indigo-400 hover:bg-indigo-50/40"
            }`}
          >
            <UploadCloud size={32} className={dragActive ? "text-indigo-500" : "text-slate-400"} />
            <p className="mt-3 text-sm font-semibold text-slate-700">
              {fileName ? fileName : "Drop your Excel file here, or browse"}
            </p>
            <p className="mt-1 text-xs text-slate-400">Supports .xlsx, .xls, .csv</p>
          </div>

          {/* Preview table */}
          {parsedRows.length > 0 && (
            <div className="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
              <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-5 py-3.5">
                <div className="flex items-center gap-2">
                  <CheckCircle2 size={16} className="text-emerald-500" />
                  <span className="text-sm font-semibold text-slate-900">
                    {parsedRows.length} companies parsed — review before importing
                  </span>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="border-b border-slate-100 bg-slate-50">
                    <tr>
                      <th className="px-5 py-3 text-left font-medium text-slate-500">Company</th>
                      <th className="px-5 py-3 text-left font-medium text-slate-500">Provider</th>
                      <th className="hidden px-5 py-3 text-left font-medium text-slate-500 md:table-cell">ATS URL</th>
                      <th className="px-5 py-3"></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {parsedRows.map((row, i) => (
                      <tr key={i} className="hover:bg-slate-50">
                        <td className="px-5 py-3 font-medium text-slate-900">{row.name || <span className="text-red-500 flex items-center gap-1"><AlertCircle size={13}/>Missing</span>}</td>
                        <td className="px-5 py-3">
                          <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${providerBadge[detectProvider(row.atsApiUrl)]}`}>
                            {detectProvider(row.atsApiUrl)}
                          </span>
                        </td>
                        <td className="hidden max-w-xs truncate px-5 py-3 font-mono text-xs text-slate-500 md:table-cell">
                          {row.atsApiUrl}
                        </td>
                        <td className="px-5 py-3 text-right">
                          <button
                            onClick={() => removeRow(i)}
                            className="rounded p-1 text-slate-400 hover:bg-red-50 hover:text-red-500 transition"
                            aria-label="Remove row"
                          >
                            <Trash2 size={14} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="border-t border-slate-100 p-4">
                <button
                  onClick={handleBulkImport}
                  disabled={importing || parsedRows.length === 0}
                  className="flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 py-3 text-sm font-bold text-white hover:bg-indigo-700 disabled:opacity-60 active:scale-[0.99] transition-all"
                >
                  {importing ? (
                    <Loader2 size={17} className="animate-spin" />
                  ) : (
                    <UploadCloud size={17} />
                  )}
                  Import {parsedRows.length} Companies
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
