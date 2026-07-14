export type JobType = "Full-time" | "Contract" | "Part-time";
export type WorkMode = "Remote" | "Hybrid" | "On-site";
export type ApplicationStatus = "Applied" | "In review" | "Interview" | "Offer";

export interface Company { name: string; initials: string; color: string; industry: string; size: string; }
export interface Job { id: number; title: string; company: Company; location: string; workMode: WorkMode; type: JobType; salary: string; posted: string; description: string; tags: string[]; featured?: boolean; }
export interface Application { id: number; job: Job; status: ApplicationStatus; appliedAt: string; }
export interface User { name: string; title: string; location: string; avatar: string; skills: string[]; }
