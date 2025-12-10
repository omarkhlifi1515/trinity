import { NextRequest, NextResponse } from 'next/server'
import { readData, writeData } from '@/lib/storage/data-storage'

const STORAGE_KEYS: Record<string, string> = {
  employees: 'TRINITY_EMPLOYEES_BIN_ID',
  tasks: 'TRINITY_TASKS_BIN_ID',
  leaves: 'TRINITY_LEAVES_BIN_ID',
  messages: 'TRINITY_MESSAGES_BIN_ID',
  departments: 'TRINITY_DEPARTMENTS_BIN_ID',
  attendance: 'TRINITY_ATTENDANCE_BIN_ID',
}

export async function GET(
  request: NextRequest,
  { params }: { params: { type: string } }
) {
  try {
    const { searchParams } = new URL(request.url)
    const binId = searchParams.get('binId')
    
    if (!binId) {
      return NextResponse.json({ error: 'Bin ID required' }, { status: 400 })
    }

    const apiKey = process.env.JSONBIN_API_KEY?.trim()
    if (!apiKey) {
      return NextResponse.json({ error: 'API key not configured' }, { status: 500 })
    }

    const response = await fetch(`https://api.jsonbin.io/v3/b/${binId}/latest`, {
      method: 'GET',
      headers: {
        'X-Master-Key': apiKey,
        'X-Bin-Meta': 'false',
      },
      cache: 'no-store',
    })

    if (!response.ok) {
      if (response.status === 404) {
        return NextResponse.json([])
      }
      return NextResponse.json({ error: 'Failed to fetch data' }, { status: response.status })
    }

    const data = await response.json()
    return NextResponse.json(Array.isArray(data) ? data : [])
  } catch (error: any) {
    console.error('Error fetching data:', error)
    return NextResponse.json({ error: error.message }, { status: 500 })
  }
}

