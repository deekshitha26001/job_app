"use client";

import { useState, useRef } from "react";
import { useAuth } from "@/context/AuthContext";
import { user as mockUser } from "@/lib/mock-data";
import { toast } from "sonner";
import { 
  FileText, 
  UploadCloud, 
  Trash2, 
  Loader2, 
  CheckCircle2, 
  ExternalLink 
} from "lucide-react";

export default function Profile() {
  const { user, checkAuth } = useAuth();
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const CLOUD_NAME = process.env.VITE_CLOUDINARY_CLOUD_NAME || "dqyrzh8qi";
  const UPLOAD_PRESET = process.env.VITE_CLOUDINARY_UPLOAD_PRESET || "jobportal";

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = async (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      await uploadFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      await uploadFile(e.target.files[0]);
    }
  };

  const uploadFile = async (file: File) => {
    const allowedExtensions = ["pdf", "doc", "docx"];
    const fileExtension = file.name.split(".").pop()?.toLowerCase();
    
    if (!fileExtension || !allowedExtensions.includes(fileExtension)) {
      toast.error("Invalid file format. Please upload PDF, DOC, or DOCX.");
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      toast.error("File size exceeds 10MB limit.");
      return;
    }

    setUploading(true);
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", UPLOAD_PRESET);

    try {
      const res = await fetch(`https://api.cloudinary.com/v1_1/${CLOUD_NAME}/auto/upload`, {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.error?.message || "Cloudinary upload failed");
      }

      const data = await res.json();
      const resumeUrl = data.secure_url;
      const resumeName = file.name;

      const token = localStorage.getItem("token");
      const saveRes = await fetch("http://localhost:8080/api/auth/me", {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          resumeUrl,
          resumeName,
        }),
      });

      if (!saveRes.ok) {
        throw new Error("Failed to save resume details to profile");
      }

      toast.success("Resume uploaded successfully!");
      await checkAuth();
    } catch (error: any) {
      console.error(error);
      toast.error(error.message || "An error occurred while uploading.");
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveResume = async () => {
    if (!confirm("Are you sure you want to delete your resume?")) return;
    
    try {
      const token = localStorage.getItem("token");
      const saveRes = await fetch("http://localhost:8080/api/auth/me", {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          resumeUrl: null,
          resumeName: null,
        }),
      });

      if (!saveRes.ok) {
        throw new Error("Failed to remove resume");
      }

      toast.success("Resume removed successfully.");
      await checkAuth();
    } catch (error: any) {
      console.error(error);
      toast.error(error.message || "An error occurred while removing the resume.");
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  const userDisplayName = user?.email ? user.email.split("@")[0] : mockUser.name;
  const avatarText = userDisplayName.substring(0, 2).toUpperCase();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <p className="text-sm font-medium text-violet-600 dark:text-violet-400">YOUR PROFILE</p>
        <h1 className="mt-1 text-3xl font-semibold tracking-tight text-zinc-900 dark:text-white">Show them what you do.</h1>
      </div>

      <div className="rounded-2xl border border-zinc-200 bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900 shadow-sm transition-all duration-300">
        <div className="flex items-center gap-4">
          <div className="grid h-16 w-16 place-items-center rounded-full bg-violet-100 text-xl font-bold text-violet-700 dark:bg-violet-950 dark:text-violet-300">
            {avatarText}
          </div>
          <div>
            <h2 className="text-lg font-semibold text-zinc-900 dark:text-white">{userDisplayName}</h2>
            <p className="text-sm text-zinc-500 dark:text-zinc-400">{user?.email}</p>
          </div>
        </div>

        <div className="mt-7 grid gap-4 sm:grid-cols-2">
          <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Full name
            <input 
              defaultValue={userDisplayName} 
              className="mt-1.5 h-11 w-full rounded-lg border border-zinc-200 px-3 font-normal outline-none focus:border-violet-500 bg-transparent text-zinc-900 dark:border-zinc-750 dark:text-white dark:focus:border-violet-500 transition-colors"
            />
          </label>
          <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Headline
            <input 
              defaultValue={mockUser.title} 
              className="mt-1.5 h-11 w-full rounded-lg border border-zinc-200 px-3 font-normal outline-none focus:border-violet-500 bg-transparent text-zinc-900 dark:border-zinc-750 dark:text-white dark:focus:border-violet-500 transition-colors"
            />
          </label>
        </div>

        <label className="mt-5 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
          About
          <textarea 
            defaultValue="I’m a product designer focused on making complex tools feel simple and delightful." 
            className="mt-1.5 min-h-28 w-full rounded-lg border border-zinc-200 p-3 font-normal outline-none focus:border-violet-500 bg-transparent text-zinc-900 dark:border-zinc-750 dark:text-white dark:focus:border-violet-500 transition-colors"
          />
        </label>

        <p className="mt-6 text-sm font-semibold text-zinc-900 dark:text-white">Skills</p>
        <div className="mt-2 flex flex-wrap gap-2">
          {mockUser.skills.map((s) => (
            <span key={s} className="rounded-full bg-violet-50 px-3 py-1.5 text-sm text-violet-700 dark:bg-violet-950/50 dark:text-violet-300 border border-violet-100 dark:border-violet-900">
              {s}
            </span>
          ))}
        </div>

        <button 
          onClick={() => toast.success("Profile saved")} 
          className="mt-8 rounded-lg bg-violet-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-violet-700 active:scale-95 transition-all duration-150 shadow-md shadow-violet-600/10 hover:shadow-violet-600/20"
        >
          Save changes
        </button>
      </div>

      <div className="rounded-2xl border border-zinc-200 bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900 shadow-sm transition-all duration-300">
        <h3 className="text-lg font-semibold text-zinc-900 dark:text-white">Resume</h3>
        <p className="text-sm text-zinc-500 dark:text-zinc-400 mt-1">
          Upload and maintain your resume here to easily apply for jobs.
        </p>

        <div className="mt-6">
          <input 
            type="file" 
            ref={fileInputRef} 
            onChange={handleFileChange} 
            accept=".pdf,.doc,.docx" 
            className="hidden" 
          />

          {uploading ? (
            <div className="flex flex-col items-center justify-center border-2 border-dashed border-violet-300 bg-violet-50/20 rounded-xl p-10 dark:border-violet-850 dark:bg-violet-950/10">
              <Loader2 className="animate-spin text-violet-600 dark:text-violet-400" size={36} />
              <p className="mt-3 text-sm font-medium text-violet-600 dark:text-violet-400">Uploading to Cloudinary...</p>
              <p className="mt-1 text-xs text-zinc-400">Please do not close this window</p>
            </div>
          ) : user?.resumeUrl ? (
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between border border-zinc-200 rounded-xl p-4 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 hover:border-violet-300 dark:hover:border-violet-950 transition-colors">
              <div className="flex items-center gap-3 w-full sm:w-auto">
                <div className="grid h-12 w-12 place-items-center rounded-lg bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-100 dark:border-emerald-900 text-emerald-600 dark:text-emerald-400">
                  <FileText size={24} />
                </div>
                <div className="truncate pr-4 w-[60%] sm:w-auto">
                  <p className="text-sm font-semibold text-zinc-900 dark:text-white truncate max-w-xs sm:max-w-md">
                    {user.resumeName || "Uploaded Resume"}
                  </p>
                  <div className="flex items-center gap-1.5 mt-0.5 text-xs text-emerald-600 dark:text-emerald-400 font-medium">
                    <CheckCircle2 size={13} />
                    <span>Saved to your profile</span>
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2 mt-4 sm:mt-0 w-full sm:w-auto justify-end border-t sm:border-t-0 border-zinc-100 pt-3 sm:pt-0">
                <a 
                  href={user.resumeUrl} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="flex items-center gap-1.5 rounded-lg border border-zinc-200 px-3 py-2 text-xs font-semibold hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800 transition-colors text-zinc-700 dark:text-zinc-350"
                >
                  <ExternalLink size={13} />
                  <span>View</span>
                </a>
                <button 
                  onClick={triggerFileInput}
                  className="rounded-lg border border-zinc-200 px-3 py-2 text-xs font-semibold hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800 transition-colors text-zinc-700 dark:text-zinc-350"
                >
                  Update
                </button>
                <button 
                  onClick={handleRemoveResume}
                  className="flex items-center gap-1.5 rounded-lg border border-red-200 text-red-600 px-3 py-2 text-xs font-semibold hover:bg-red-50 dark:border-red-900/30 dark:text-red-400 dark:hover:bg-red-950/20 transition-colors"
                  aria-label="Delete resume"
                >
                  <Trash2 size={13} />
                  <span>Delete</span>
                </button>
              </div>
            </div>
          ) : (
            <div 
              onDragEnter={handleDrag} 
              onDragOver={handleDrag} 
              onDragLeave={handleDrag} 
              onDrop={handleDrop}
              onClick={triggerFileInput}
              className={`flex flex-col items-center justify-center border-2 border-dashed rounded-xl p-10 cursor-pointer transition-all duration-300 ${
                dragActive 
                  ? "border-violet-500 bg-violet-50/30 dark:border-violet-400 dark:bg-violet-950/10 scale-[1.01]" 
                  : "border-zinc-200 bg-transparent hover:border-violet-400 hover:bg-zinc-50/50 dark:border-zinc-800 dark:hover:border-violet-500 dark:hover:bg-zinc-900/30"
              }`}
            >
              <div className="grid h-12 w-12 place-items-center rounded-xl bg-violet-50 text-violet-600 dark:bg-violet-950/50 dark:text-violet-400 border border-violet-100 dark:border-violet-900 group-hover:scale-110 transition-transform">
                <UploadCloud size={24} />
              </div>
              <p className="mt-4 text-sm font-semibold text-zinc-900 dark:text-white">
                Drag & drop your resume here, or <span className="text-violet-600 dark:text-violet-400 font-bold">browse</span>
              </p>
              <p className="mt-1 text-xs text-zinc-400 dark:text-zinc-500">
                Supports PDF, DOC, DOCX up to 10MB
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
