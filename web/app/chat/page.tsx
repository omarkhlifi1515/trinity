import { redirect } from 'next/navigation'

export default function ChatPlaceholder() {
  // Redirect legacy /chat to the new /messages UI
  redirect('/messages')
}
