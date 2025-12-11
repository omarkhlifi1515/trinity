import { cookies } from 'next/headers'
import { NextResponse } from 'next/server'

export async function POST() {
    const cookieStore = cookies()

    // Get all cookies and delete auth-related ones
    const allCookies = cookieStore.getAll()
    allCookies.forEach(cookie => {
        if (cookie.name.includes('supabase') || cookie.name.includes('auth-token')) {
            cookieStore.delete(cookie.name)
        }
    })

    return NextResponse.json({
        success: true,
        message: 'All auth cookies cleared. Please log in again.'
    })
}
