"use client";
import { Bookmark, MapPin, Sparkles } from "lucide-react";
import { Job } from "@/lib/types";
import { toast } from "sonner";
export function JobCard({ job, onOpen }: { job: Job; onOpen?: (job: Job) => void }) { return <article className="group rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-xl hover:shadow-violet-100 dark:border-zinc-800 dark:bg-zinc-900 dark:hover:shadow-black/20">
  <div className="flex items-start justify-between gap-3"><div className="flex gap-3"><div className={`grid h-11 w-11 shrink-0 place-items-center rounded-xl text-lg font-bold text-white ${job.company.color}`}>{job.company.initials}</div><div><p className="text-sm font-medium text-zinc-500">{job.company.name}</p><h3 className="mt-0.5 text-base font-semibold tracking-tight">{job.title}</h3></div></div><button aria-label="Save job" onClick={() => toast.success("Job saved to your list")} className="rounded-lg p-2 text-zinc-400 transition hover:bg-violet-50 hover:text-violet-600"><Bookmark size={18}/></button></div>
  <div className="mt-5 flex flex-wrap gap-x-4 gap-y-2 text-sm text-zinc-500"><span className="flex items-center gap-1"><MapPin size={15}/>{job.location}</span><span>{job.salary}</span><span>{job.type}</span></div>
  <div className="mt-4 flex flex-wrap gap-2">{job.tags.map(tag => <span key={tag} className="rounded-md bg-zinc-100 px-2 py-1 text-xs font-medium text-zinc-600 dark:bg-zinc-800 dark:text-zinc-300">{tag}</span>)}</div>
  <div className="mt-5 flex items-center justify-between border-t border-zinc-100 pt-4 dark:border-zinc-800"><span className="text-xs text-zinc-400">{job.posted}</span><button onClick={() => onOpen ? onOpen(job) : toast.success("Application started!")} className="inline-flex items-center gap-1.5 rounded-lg bg-violet-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-violet-700 active:scale-95"><Sparkles size={14}/>Apply</button></div>
</article>; }
