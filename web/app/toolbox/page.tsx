"use client"
import React, { useState } from 'react'
import Sidebar from '../../components/Sidebar'

// Data Structure for the Toolbox (keeps the page self-contained for now)
const TOOLBOX_DATA = [
  {
    category: 'HR Tools',
    icon: '👥',
    color: 'from-pink-500 to-rose-500',
    sections: [
      { name: 'HR Management', tools: ['BambooHR', 'Gusto', 'Zoho People'] },
      { name: 'Hiring & ATS', tools: ['Workable', 'Greenhouse', 'LinkedIn Talent'] },
      { name: 'Training & LMS', tools: ['TalentLMS', 'Coursera', 'Lessonly'] },
      { name: 'Payroll', tools: ['ADP', 'Rippling', 'Paychex'] },
      { name: 'Engagement', tools: ['CultureAmp', 'Officevibe'] },
    ],
  },
  {
    category: 'IT & Tech',
    icon: '🖥️',
    color: 'from-blue-500 to-cyan-500',
    sections: [
      { name: 'Ticketing', tools: ['Jira Service', 'Zendesk', 'Freshservice'] },
      { name: 'Monitoring', tools: ['Datadog', 'New Relic', 'Sentry'] },
      { name: 'Security', tools: ['1Password', 'Okta', 'Cloudflare'] },
      { name: 'Dev Tools', tools: ['GitHub / GitLab', 'VS Code', 'Docker'] },
      { name: 'Automation', tools: ['Zapier', 'Make', 'n8n'] },
    ],
  },
  {
    category: 'Finance',
    icon: '💰',
    color: 'from-emerald-500 to-green-500',
    sections: [
      { name: 'Accounting', tools: ['QuickBooks', 'Xero', 'FreshBooks'] },
      { name: 'Budgeting', tools: ['Anaplan', 'Float', 'Mosaic'] },
      { name: 'Expenses', tools: ['Expensify', 'Concur', 'Ramp'] },
      { name: 'Billing', tools: ['Stripe', 'Square', 'Chargebee'] },
    ],
  },
  {
    category: 'Operations',
    icon: '⚙️',
    color: 'from-orange-500 to-amber-500',
    sections: [
      { name: 'Project Mgmt', tools: ['Asana', 'ClickUp', 'Monday.com'] },
      { name: 'Scheduling', tools: ['When I Work', 'Deputy'] },
      { name: 'Contracts', tools: ['DocuSign', 'PandaDoc', 'Adobe Sign'] },
      { name: 'Inventory', tools: ['Sortly', 'AssetTiger', 'Zoho Inv.'] },
    ],
  },
  {
    category: 'Sales & Mktg',
    icon: '💼',
    color: 'from-violet-500 to-purple-500',
    sections: [
      { name: 'CRM', tools: ['HubSpot', 'Salesforce', 'Pipedrive'] },
      { name: 'Email', tools: ['Mailchimp', 'Sendinblue', 'ConvertKit'] },
      { name: 'Social', tools: ['Hootsuite', 'Buffer', 'Sprout'] },
      { name: 'Support', tools: ['Intercom', 'LiveChat', 'Drift'] },
    ],
  },
  {
    category: 'Productivity',
    icon: '🧠',
    color: 'from-indigo-500 to-blue-600',
    sections: [
      { name: 'Collaboration', tools: ['Notion', 'Confluence', 'Google Work.'] },
      { name: 'Communication', tools: ['Slack', 'Teams', 'Discord'] },
      { name: 'Files', tools: ['Drive', 'Dropbox', 'OneDrive'] },
      { name: 'PDF & AI', tools: ['Adobe', 'ChatGPT', 'Jasper'] },
    ],
  },
]

export default function ToolboxPage() {
  const [activeTab, setActiveTab] = useState('All')

  const categories = ['All', ...TOOLBOX_DATA.map((c) => c.category)]

  const filteredData =
    activeTab === 'All' ? TOOLBOX_DATA : TOOLBOX_DATA.filter((d) => d.category === activeTab)

  return (
    <div className="flex bg-[#050505] min-h-screen font-sans">
      <Sidebar />

      <main className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Header Area */}
        <header className="px-8 py-6 border-b border-slate-800 bg-[#050505]/80 backdrop-blur-md z-10">
          <h1 className="text-2xl font-semibold text-slate-100 flex items-center gap-3">
            <span className="text-3xl">🧰</span>
            Company Toolbox
          </h1>
          <p className="text-slate-500 text-sm mt-1">Access all your integrated enterprise tools in one place.</p>

          {/* Category Tabs */}
          <div className="flex gap-2 mt-6 overflow-x-auto pb-2 scrollbar-hide">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setActiveTab(cat)}
                className={`px-4 py-1.5 rounded-full text-xs font-medium transition-all whitespace-nowrap border ${
                  activeTab === cat
                    ? 'bg-slate-100 text-black border-slate-100 shadow-md shadow-slate-500/20'
                    : 'bg-slate-900 text-slate-400 border-slate-800 hover:border-slate-600 hover:text-slate-200'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </header>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-8 pb-20">
          <div className="max-w-7xl mx-auto space-y-12">
            {filteredData.map((group, idx) => (
              <div
                key={idx}
                className="animate-in fade-in slide-in-from-bottom-4 duration-500"
                style={{ animationDelay: `${idx * 100}ms` }}
              >
                <div className="flex items-center gap-3 mb-6">
                  <div
                    className={`w-10 h-10 rounded-xl bg-gradient-to-br ${group.color} flex items-center justify-center text-xl shadow-lg`}
                  >
                    {group.icon}
                  </div>
                  <h2 className="text-xl font-bold text-slate-200">{group.category}</h2>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                  {group.sections.map((section, sIdx) => (
                    <div
                      key={sIdx}
                      className="bg-[#0f1117] border border-slate-800/60 rounded-xl p-5 hover:border-slate-700 transition-colors group"
                    >
                      <h3 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4 border-b border-slate-800 pb-2">
                        {section.name}
                      </h3>
                      <div className="space-y-2">
                        {section.tools.map((tool) => (
                          <div
                            key={tool}
                            className="flex items-center justify-between text-slate-300 hover:text-white cursor-pointer p-2 hover:bg-slate-800/50 rounded-lg transition-all group-item"
                          >
                            <span className="text-sm font-medium">{tool}</span>
                            <span className="text-xs text-slate-600 group-item-hover:text-cyan-400">↗</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  )
}
