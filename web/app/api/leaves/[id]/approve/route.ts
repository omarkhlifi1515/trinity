import { NextRequest, NextResponse } from 'next/server'
import { getCurrentUser } from '@/lib/auth/local-auth'
import { approveLeave, rejectLeave } from '@/lib/storage/supabase-storage'

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const user = await getCurrentUser()
    if (!user) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })
    }

    // Check if user can approve leaves (admin or department head)
    const canApprove = user.role === 'admin' || user.role === 'department_head'
    if (!canApprove) {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
    }

    const { action } = await request.json()
    const leaveId = params.id

    if (action === 'approve') {
      await approveLeave(leaveId, user.id)
      return NextResponse.json({ success: true, message: 'Leave approved' })
    } else if (action === 'reject') {
      await rejectLeave(leaveId, user.id)
      return NextResponse.json({ success: true, message: 'Leave rejected' })
    }

    return NextResponse.json({ error: 'Invalid action' }, { status: 400 })
  } catch (error: any) {
    console.error('Error approving leave:', error)
    return NextResponse.json({ error: error.message }, { status: 500 })
  }
}

