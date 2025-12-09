<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;
use App\Models\User;
use App\Models\Company;
use App\Models\Task;
use App\Models\Leave;
use App\Models\Attendance;

/*
|--------------------------------------------------------------------------
| 1. AUTHENTICATION (Public)
|--------------------------------------------------------------------------
*/

// Matches Android: @POST("login")
Route::post('/login', function (Request $request) {
    try {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);
    } catch (ValidationException $e) {
        return response()->json([
            'message' => 'Validation failed',
            'errors' => $e->errors()
        ], 422);
    }

    // Allow all users to login (not just admin) - exclude soft deleted users
    $user = User::where('email', $request->email)->first();

    if (! $user || ! Hash::check($request->password, $user->password)) {
        return response()->json([
            'message' => 'Invalid credentials',
            'error' => 'Invalid email or password'
        ], 401);
    }

    // Refresh user to get latest data from database
    $user->refresh();

    // Return the specific JSON format the Android App expects
    return response()->json([
        'token' => $user->createToken('mobile-app')->plainTextToken,
        'user' => [
            'id' => $user->id,
            'userId' => (string)$user->id, // String format for mobile app
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'role' => $user->role ?? 'Employee',
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]
    ], 200);
});

// Registration endpoint - matches Android: @POST("users")
Route::post('/users', function (Request $request) {
    $request->validate([
        'name' => 'required|string|max:255',
        'email' => 'required|string|email|max:255|unique:users',
        'password' => 'required|string', // No minimum length or format requirements
        'phone' => 'nullable|string',
        'gender' => 'nullable|string',
        'role' => 'nullable|string',
    ]);

    // Auto-assign company if user is HR/admin or specific email
    // For HR users, admin email (test@example.com), or salah@gmail.com, automatically assign company
    $companyCode = null;
    $role = $request->role ?? 'Employee';
    
    // Special case: salah@gmail.com gets company "123456" by default
    if ($request->email === 'salah@gmail.com') {
        // Find or create company with code "123456"
        $company = Company::firstOrCreate(
            ['code' => '123456'],
            [
                'name' => 'Company 123456',
                'description' => 'Default company for salah@gmail.com',
                'is_active' => true,
            ]
        );
        $companyCode = $company->code;
    } elseif ($request->email === 'test@example.com' || $role === 'ROLE_HR' || $role === 'HR') {
        $company = Company::where('is_active', true)->first();
        if ($company) {
            $companyCode = $company->code;
        }
    }

    // Create new user (all users can register, not just admin)
    $user = User::create([
        'name' => $request->name,
        'email' => $request->email,
        'password' => Hash::make($request->password),
        'company_code' => $companyCode,
        'waiting_company_code' => null,
    ]);

    // Return UserDto format (no token in registration, user must login)
    return response()->json([
        'id' => $user->id,
        'userId' => (string)$user->id,
        'name' => $user->name,
        'email' => $user->email,
        'companyCode' => $user->company_code,
        'waitingCompanyCode' => $user->waiting_company_code,
        'imageUrl' => $user->image_url ?? null,
        'role' => $role,
        'created_at' => $user->created_at,
        'updated_at' => $user->updated_at,
    ], 201); // 201 Created
});

// Alternative registration endpoint with token (for backward compatibility)
Route::post('/register', function (Request $request) {
    $request->validate([
        'name' => 'required|string|max:255',
        'email' => 'required|string|email|max:255|unique:users',
        'password' => 'required|string', // No minimum length or format requirements
        'phone' => 'nullable|string',
        'gender' => 'nullable|string',
        'role' => 'nullable|string',
    ]);

    // Auto-assign company if user is HR/admin or specific email
    // For HR users, admin email, or salah@gmail.com, automatically assign company
    $companyCode = null;
    
    // Special case: salah@gmail.com gets company "123456" by default
    if ($request->email === 'salah@gmail.com') {
        // Find or create company with code "123456"
        $company = Company::firstOrCreate(
            ['code' => '123456'],
            [
                'name' => 'Company 123456',
                'description' => 'Default company for salah@gmail.com',
                'is_active' => true,
            ]
        );
        $companyCode = $company->code;
    } elseif ($request->email === 'test@example.com' || ($request->role === 'ROLE_HR' || $request->role === 'HR')) {
        $company = Company::where('is_active', true)->first();
        if ($company) {
            $companyCode = $company->code;
        }
    }

    // Create new user (all users can register, not just admin)
    $user = User::create([
        'name' => $request->name,
        'email' => $request->email,
        'password' => Hash::make($request->password),
        'phone' => $request->phone ?? null,
        'gender' => $request->gender ?? null,
        'role' => $request->role ?? 'Employee',
        'company_code' => $companyCode,
        'waiting_company_code' => null,
    ]);

    // Return the same format as login for consistency
    return response()->json([
        'token' => $user->createToken('mobile-app')->plainTextToken,
        'user' => [
            'id' => $user->id,
            'userId' => (string)$user->id,
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'role' => $request->role ?? 'Employee',
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]
    ], 201); // 201 Created
});

/*
|--------------------------------------------------------------------------
| 2. PROTECTED ROUTES (Requires Login)
|--------------------------------------------------------------------------
*/
Route::middleware('auth:sanctum')->group(function () {

    // --- USER PROFILE ---
    Route::get('/users', function (Request $request) {
        $user = $request->user();
        
        // Refresh user from database to get latest data
        $user->refresh();
        
        // Return user with company code fields
        return response()->json([
            'id' => $user->id,
            'userId' => (string)$user->id,
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]);
    });
    
    // --- COMPANY CODE MANAGEMENT (Company Code = Department Name) ---
    
    // Update company code (join department with approval required)
    // Note: Company code is actually the department name
    Route::patch('/users/{companyCode}', function (Request $request, $companyCode) {
        $user = $request->user();
        
        // Validate company code (department name) exists and is active
        // Check if it's a valid department name or company code
        $departmentCode = strtoupper($companyCode);
        $company = Company::where('code', $departmentCode)
            ->where('is_active', true)
            ->first();
        
        // If not found as company code, try to create/find by department name
        if (!$company) {
            // Check if it's a valid department name from enum
            $validDepartments = ['HR', 'ENGINEERING', 'SALES', 'MARKETING', 'FINANCE', 'OPERATIONS', 'ADMINISTRATION', 'SUPPORT', 'OTHERS'];
            if (in_array($departmentCode, $validDepartments)) {
                // Find or create company with department name as code
                $company = Company::firstOrCreate(
                    ['code' => $departmentCode],
                    [
                        'name' => $departmentCode,
                        'description' => "Department: $departmentCode",
                        'is_active' => true,
                        'created_by' => $user->id
                    ]
                );
            } else {
                return response()->json([
                    'message' => 'Department name not found. Valid departments: ' . implode(', ', $validDepartments),
                    'error' => 'Invalid department name'
                ], 404);
            }
        }
        
        // Check if user already belongs to a department
        if ($user->company_code) {
            return response()->json([
                'message' => 'User already belongs to a department',
                'error' => 'Already in department'
            ], 400);
        }
        
        // Check if user is already on waitlist for this department
        if ($user->waiting_company_code === $departmentCode) {
            return response()->json([
                'message' => 'Already on waitlist for this department',
                'error' => 'Already on waitlist'
            ], 400);
        }
        
        // Add user to waitlist (HR needs to approve)
        $user->waiting_company_code = $departmentCode;
        $user->save();
        
        // Return updated user
        return response()->json([
            'id' => $user->id,
            'userId' => (string)$user->id,
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ], 202); // 202 Accepted - request is pending approval
    });
    
    // Leave company
    Route::patch('/users/leave-company', function (Request $request) {
        $user = $request->user();
        
        if (!$user->company_code) {
            return response()->json([
                'message' => 'User does not belong to any company',
                'error' => 'Not in company'
            ], 400);
        }
        
        $user->company_code = null;
        $user->save();
        
        return response()->json([
            'id' => $user->id,
            'userId' => (string)$user->id,
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]);
    });
    
    // Remove from waitlist
    Route::patch('/users/remove-wait-company', function (Request $request) {
        $user = $request->user();
        
        if (!$user->waiting_company_code) {
            return response()->json([
                'message' => 'User is not on any waitlist',
                'error' => 'Not on waitlist'
            ], 400);
        }
        
        $user->waiting_company_code = null;
        $user->save();
        
        return response()->json([
            'id' => $user->id,
            'userId' => (string)$user->id,
            'name' => $user->name,
            'email' => $user->email,
            'companyCode' => $user->company_code,
            'waitingCompanyCode' => $user->waiting_company_code,
            'imageUrl' => $user->image_url ?? null,
            'created_at' => $user->created_at,
            'updated_at' => $user->updated_at,
        ]);
    });

    // --- TASKS ---
    
    // Matches Android: @GET("tasks/userTasks")
    Route::get('/tasks/userTasks', function (Request $request) {
        // Fetch tasks assigned to the logged-in user
        // Note: Assuming 'assignee_id' exists based on migration 2025_10_17
        $tasks = Task::where('assignee_id', $request->user()->id)
                     ->orWhere('assignee_id', null) // Optional: Show unassigned tasks
                     ->orderBy('created_at', 'desc')
                     ->get();
                     
        return response()->json($tasks);
    });

    // Matches Android: @POST("tasks")
    Route::post('/tasks', function (Request $request) {
        $validated = $request->validate([
            'title' => 'required|string',
            'description' => 'nullable|string',
            'priority' => 'required|string',
        ]);

        $task = Task::create([
            'name' => $validated['title'], // Map 'title' to 'name'
            'description' => $validated['description'],
            'priority' => $validated['priority'],
            'status' => 'pending',
            'assignee_id' => $request->user()->id, // Self-assign for now
            'created_by' => $request->user()->id,
        ]);

        return response()->json($task);
    });

    // --- LEAVES ---

    // Matches Android: @GET("leaves")
    Route::get('/leaves', function (Request $request) {
        return Leave::where('employee_id', $request->user()->id)->get();
    });

    // Matches Android: @POST("leaves")
    Route::post('/leaves', function (Request $request) {
        $validated = $request->validate([
            'leave_type' => 'required|string',
            'start_date' => 'required|date',
            'end_date' => 'required|date',
            'reason' => 'required|string',
        ]);

        $leave = Leave::create([
            'employee_id' => $request->user()->id,
            'leave_type' => $validated['leave_type'],
            'start_date' => $validated['start_date'],
            'end_date' => $validated['end_date'],
            'reason' => $validated['reason'],
            'status' => 'pending',
        ]);

        return response()->json($leave);
    });

    // --- AI CHAT ---
    // Old chatbot routes removed - using Filament ChatGPT bot package now
});