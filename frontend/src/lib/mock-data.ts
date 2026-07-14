import { Application, Job, User } from "./types";

export const user: User = { name: "Avery Morgan", title: "Product Designer", location: "Brooklyn, NY", avatar: "AM", skills: ["Product design", "Figma", "Research", "Design systems"] };
export const jobs: Job[] = [
  { id: 1, title: "Senior Product Designer", company: { name: "Aperture", initials: "A", color: "bg-violet-600", industry: "Developer tools", size: "201–500" }, location: "New York, NY", workMode: "Hybrid", type: "Full-time", salary: "$145k – $175k", posted: "2 days ago", tags: ["Design systems", "B2B"], featured: true, description: "Help shape a calm, powerful workspace for modern product teams. You’ll lead complex workflows from insight to polished, high-craft experiences." },
  { id: 2, title: "Product Designer, Growth", company: { name: "Vercel", initials: "▲", color: "bg-zinc-950", industry: "Cloud platform", size: "501–1,000" }, location: "Remote (US)", workMode: "Remote", type: "Full-time", salary: "$135k – $165k", posted: "1 day ago", tags: ["Growth", "Experimentation"], description: "Design intuitive paths that help millions of developers discover the full power of our platform." },
  { id: 3, title: "UX Designer", company: { name: "Notion", initials: "N", color: "bg-zinc-800", industry: "Productivity", size: "501–1,000" }, location: "San Francisco, CA", workMode: "Hybrid", type: "Full-time", salary: "$125k – $158k", posted: "3 days ago", tags: ["Mobile", "Collaboration"], description: "Craft tools that make people feel more at home in their work. Partner with a multidisciplinary team on new collaboration experiences." },
  { id: 4, title: "Staff Product Designer", company: { name: "Figma", initials: "F", color: "bg-rose-500", industry: "Design software", size: "1,000+" }, location: "Remote (US)", workMode: "Remote", type: "Full-time", salary: "$170k – $210k", posted: "5 days ago", tags: ["0→1", "Platform"], description: "Set a high bar for quality and clarity while helping design teams work together in entirely new ways." },
  { id: 5, title: "Design Engineer", company: { name: "Raycast", initials: "R", color: "bg-orange-500", industry: "Developer tools", size: "51–200" }, location: "London, UK", workMode: "Hybrid", type: "Full-time", salary: "£80k – £110k", posted: "1 week ago", tags: ["React", "Motion"], description: "Bridge code and craft to create a desktop experience developers love to use every day." },
  { id: 6, title: "Product Designer", company: { name: "Arc", initials: "A", color: "bg-sky-500", industry: "Browser", size: "201–500" }, location: "New York, NY", workMode: "On-site", type: "Full-time", salary: "$120k – $150k", posted: "1 week ago", tags: ["Consumer", "Visual design"], description: "Reimagine how people experience the web with a small team obsessed with detail." }
];
export const applications: Application[] = [
  { id: 1, job: jobs[0], status: "Interview", appliedAt: "Jun 22, 2026" },
  { id: 2, job: jobs[2], status: "In review", appliedAt: "Jun 18, 2026" },
  { id: 3, job: jobs[4], status: "Applied", appliedAt: "Jun 15, 2026" }
];
