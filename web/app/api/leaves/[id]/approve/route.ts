import { NextRequest, NextResponse } from 'next/server'
import { approveLeave, rejectLeave } from '@/lib/storage/supabase-storage'
import { getUserProfile } from '@/lib/firebase/users'
import { canApproveLeaves } from '@/lib/auth/roles'

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    // Parse request body once
    const body = await request.json();
    const { action, userId } = body;

    // Validate inputs
    if (!userId) {
      return NextResponse.json({ error: 'User ID required' }, { status: 400 });
    }

    if (!action || (action !== 'approve' && action !== 'reject')) {
      return NextResponse.json({ error: 'Invalid action' }, { status: 400 });
    }

    // Get user profile
    const profile = await getUserProfile(userId);
    if (!profile) {
      return NextResponse.json({ error: 'User not found' }, { status: 404 });
    }

    // Check if user can approve leaves
    const user = {
      id: userId,
      email: profile.email,
      role: profile.role,
      department: profile.department,
    };

    if (!canApproveLeaves(user)) {
      return NextResponse.json({
        error: 'You do not have permission to approve leaves'
      }, { status: 403 });
    }

    const leaveId = params.id;

    // Approve or reject the leave
    if (action === 'approve') {
      const success = await approveLeave(leaveId, userId);
      if (success) {
        return NextResponse.json({ success: true, message: 'Leave approved' });
      }
    } else {
      const success = await rejectLeave(leaveId, userId);
      if (success) {
        return NextResponse.json({ success: true, message: 'Leave rejected' });
      }
    }

    return NextResponse.json({ error: 'Failed to update leave status' }, { status: 500 });
  } catch (error: any) {
    console.error('Error approving leave:', error);
    return NextResponse.json({ error: error.message || 'Internal server error' }, { status: 500 });
  }
}
