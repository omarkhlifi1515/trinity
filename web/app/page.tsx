import { redirect } from 'next/navigation'

export default function Home() {
  // Redirect visitors landing on `/` to the dashboard.
  redirect('/dashboard')
}
