import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  // Returns 401 to stop legacy Supabase clients without erroring out excessively
  return NextResponse.json(
    { error: 'Auth session missing' },
    { status: 401 }
  )
}
