"use client";

import React from "react";
import Link from "next/link";

type Role = "employee" | "chef" | "admin" | string;

export default function AppSidebar({ user }: { user: { role: Role } | null }) {
  const role = user?.role || "employee";

  const links: { key: string; href: string; label: string }[] = [
    { key: "chat", href: "/chat", label: "Chat" },
    { key: "profile", href: "/profile", label: "My Profile" },
    { key: "news", href: "/dashboard", label: "News" },
  ];

  if (role === "employee" || role === "chef" || role === "admin") {
    links.push({ key: "presence", href: "/presence", label: "Mark Presence" });
    links.push({ key: "tasks", href: "/tasks", label: "My Tasks" });
    links.push({ key: "payslips", href: "/payslips", label: "My Payslips" });
  }

  if (role === "chef" || role === "admin") {
    links.push({ key: "team", href: "/team", label: "Team Management" });
    links.push({ key: "assign", href: "/assign", label: "Assign Task" });
    links.push({ key: "upload", href: "/upload", label: "Upload Payslip" });
  }

  if (role === "admin") {
    links.push({ key: "users", href: "/admin/users", label: "User Management" });
    links.push({ key: "deptconfig", href: "/admin/departments", label: "Department Config" });
  }

  return (
    <aside className="w-64 bg-white border-r min-h-screen p-4">
      <div className="mb-6">
        <h2 className="text-lg font-semibold">Trinity</h2>
        <p className="text-sm text-muted-foreground">Role: {role}</p>
      </div>
      <nav className="flex flex-col space-y-1">
        {links.map((l) => (
          <Link key={l.key} href={l.href} className="px-3 py-2 rounded hover:bg-gray-100">
            {l.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
