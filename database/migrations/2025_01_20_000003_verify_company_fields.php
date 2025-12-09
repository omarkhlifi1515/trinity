<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // Verify columns exist and are nullable
        if (Schema::hasTable('users')) {
            // Check if company_code column exists and is nullable
            if (Schema::hasColumn('users', 'company_code')) {
                // Ensure it's nullable (in case it wasn't before)
                Schema::table('users', function (Blueprint $table) {
                    $table->string('company_code')->nullable()->change();
                });
            } else {
                // Create it if it doesn't exist
                Schema::table('users', function (Blueprint $table) {
                    $table->string('company_code')->nullable()->index()->after('email');
                });
            }
            
            // Check if waiting_company_code column exists and is nullable
            if (Schema::hasColumn('users', 'waiting_company_code')) {
                // Ensure it's nullable
                Schema::table('users', function (Blueprint $table) {
                    $table->string('waiting_company_code')->nullable()->change();
                });
            } else {
                // Create it if it doesn't exist
                Schema::table('users', function (Blueprint $table) {
                    $table->string('waiting_company_code')->nullable()->index()->after('company_code');
                });
            }
            
            // Try to add foreign keys if companies table exists
            // Note: Foreign keys are optional and might cause issues if company codes don't match exactly
            // We'll skip foreign keys for now to avoid constraint issues
            // The application logic will handle referential integrity
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // Don't drop columns in down migration - let the original migration handle it
    }
};

