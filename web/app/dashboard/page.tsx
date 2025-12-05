"use client";

import React, { useEffect, useState } from "react";
import AppSidebar from "../../components/AppSidebar";
import { apiGet } from "../../lib/api";

export default function DashboardPage() {
  const [user, setUser] = useState<{ id: string; role: string; department?: string } | null>(null);
  const [news, setNews] = useState<any[]>([]);

  useEffect(() => {
    // Lightweight: read user from localStorage (demo). In prod, use Supabase auth.
    const stored = (typeof window !== "undefined" && localStorage.getItem("trinity_user")) || null;
    if (stored) setUser(JSON.parse(stored));

    async function fetchNews() {
      try {
        // Use apiGet to call backend /department/news with auth
        const data = await apiGet("/department/news");
        setNews(data.news || []);
      } catch (e) {
        console.error(e);
      }
    }
    fetchNews();
  }, []);
        import { supabase } from "../../lib/supabase";
        import { useRouter } from "next/navigation";

        export default function DashboardPage() {
          const router = useRouter();
          const [user, setUser] = useState<any>(null);
          const [news, setNews] = useState<any[]>([]);
          const [loading, setLoading] = useState(true);

          useEffect(() => {
            async function init() {
              // 1. Check Session
              const { data } = await supabase.auth.getSession();
              if (!data.session) {
                router.push("/login");
                return;
              }
              // 2. Set User (Using token details or fetching profile)
              setUser({
                id: data.session.user.id,
                email: data.session.user.email,
                role: "employee" // Default, later fetch from 'profiles' table
              });
              // 3. Fetch News
              try {
                const newsData = await apiGet("/department/news");
                setNews(newsData.news || []);
              } catch (e) {
                console.error("Failed to fetch news:", e);
              } finally {
                setLoading(false);
              }
            }
            init();
          }, [router]);

          if (loading) return <div className="min-h-screen bg-[#020617] text-white flex items-center justify-center">Loading Workspace...</div>;

          return (
            <div className="flex min-h-screen bg-[#020617] text-slate-100">
              <AppSidebar user={user} />
              <main className="flex-1 p-8">
                <header className="mb-8">
                  <h1 className="text-3xl font-bold text-white tracking-tight">Department News</h1>
                  <p className="text-slate-400 mt-2">Latest updates from your team.</p>
                </header>
                <div className="grid gap-4">
                  {news.length === 0 && (
                    <div className="p-8 border border-dashed border-slate-800 rounded-xl text-center text-slate-500">
                      No news updates available.
                    </div>
                  )}
                  {news.map((n) => (
                    <article key={n.id} className="p-6 bg-[#0f172a] border border-slate-800 rounded-xl hover:border-slate-700 transition-colors">
                      <h3 className="text-xl font-semibold text-cyan-400 mb-2">{n.title || "Update"}</h3>
                      <p className="text-slate-300 leading-relaxed">{n.body}</p>
                      <div className="mt-4 flex items-center text-xs text-slate-500 font-mono">
                        <span>{new Date(n.created_at).toLocaleDateString()}</span>
                        <span className="mx-2">•</span>
                        <span className="uppercase tracking-wider">{n.department}</span>
                      </div>
                    </article>
                  ))}
                </div>
              </main>
            </div>
          );
