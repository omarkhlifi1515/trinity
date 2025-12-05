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

  return (
    <div className="flex">
      <AppSidebar user={user} />
      <main className="flex-1 p-8">
        <h1 className="text-2xl font-bold mb-4">Department News</h1>
        <div className="space-y-4">
          {news.length === 0 && <div className="text-sm text-muted-foreground">No news yet.</div>}
          {news.map((n) => (
            <article key={n.id} className="p-4 border rounded">
              <h3 className="font-semibold">{n.title || "Untitled"}</h3>
              <p className="text-sm">{n.body}</p>
              <div className="text-xs text-gray-500">{n.created_at}</div>
            </article>
          ))}
        </div>
      </main>
    </div>
  );
}
