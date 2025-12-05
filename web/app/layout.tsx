import './globals.css'
import React from 'react'

export const metadata = {
  title: 'Trinity Dashboard',
  description: 'Trinity command system',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
