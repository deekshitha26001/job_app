"use client";

import { ExternalLink, MapPin, Layers, Building2 } from "lucide-react";

export interface AtsJob {
  id: string;
  title: string;
  location: string;
  department: string;
  url: string;
  companyName: string;
  companyLogoUrl?: string;
  atsProvider: string;
  postedAt: string;
}

const providerBadge: Record<string, string> = {
  GREENHOUSE: "bg-emerald-100 text-emerald-700",
  LEVER: "bg-orange-100 text-orange-700",
  WORKDAY: "bg-blue-100 text-blue-700",
  UNKNOWN: "bg-slate-100 text-slate-500",
};

export function AtsJobCard({ job }: { job: AtsJob }) {
  const initials = job.companyName.substring(0, 2).toUpperCase();

  return (
    <div className="flex flex-col justify-between rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm transition-all duration-200 hover:border-violet-300 hover:shadow-md dark:border-zinc-800 dark:bg-zinc-900 dark:hover:border-violet-600">
      {/* Company + provider */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          {job.companyLogoUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={job.companyLogoUrl}
              alt={job.companyName}
              className="h-10 w-10 rounded-xl object-contain bg-zinc-100 p-1"
            />
          ) : (
            <div className="grid h-10 w-10 place-items-center rounded-xl bg-violet-100 text-sm font-bold text-violet-700 dark:bg-violet-950 dark:text-violet-300">
              {initials}
            </div>
          )}
          <div>
            <p className="text-sm font-semibold text-zinc-900 dark:text-white">{job.companyName}</p>
            {job.department && (
              <p className="text-xs text-zinc-500 dark:text-zinc-400">{job.department}</p>
            )}
          </div>
        </div>
        <span className={`shrink-0 rounded-full px-2.5 py-0.5 text-xs font-semibold ${providerBadge[job.atsProvider] ?? providerBadge.UNKNOWN}`}>
          {job.atsProvider}
        </span>
      </div>

      {/* Job title */}
      <h3 className="mt-4 text-base font-semibold leading-snug text-zinc-900 dark:text-white">
        {job.title}
      </h3>

      {/* Meta */}
      <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-zinc-500 dark:text-zinc-400">
        {job.location && (
          <span className="flex items-center gap-1">
            <MapPin size={12} />
            {job.location}
          </span>
        )}
        {job.department && (
          <span className="flex items-center gap-1">
            <Layers size={12} />
            {job.department}
          </span>
        )}
        <span className="flex items-center gap-1">
          <Building2 size={12} />
          {job.companyName}
        </span>
      </div>

      {/* Apply */}
      <a
        href={job.url}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-5 flex items-center justify-center gap-2 rounded-lg bg-violet-600 py-2.5 text-sm font-bold text-white transition hover:bg-violet-700 active:scale-[0.98]"
      >
        Apply Now
        <ExternalLink size={14} />
      </a>
    </div>
  );
}
