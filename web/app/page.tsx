import { redirect } from 'next/navigation'

export default function Home() {
  // Redirects the root URL (/) to the Dashboard
  redirect('/dashboard')
}
