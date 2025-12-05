import './globals.css'
import React from 'react'

export const metadata = {
  title: 'Trinity - Enterprise Hub',
  description: 'Trinity Enterprise Hub',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        {children}
      </body>
    </html>
  )
}
