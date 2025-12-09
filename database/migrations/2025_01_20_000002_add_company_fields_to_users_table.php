<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            // Check if columns don't exist before adding
            if (!Schema::hasColumn('users', 'company_code')) {
                $table->string('company_code')->nullable()->index()->after('email');
            }
            
            if (!Schema::hasColumn('users', 'waiting_company_code')) {
                $table->string('waiting_company_code')->nullable()->index()->after('company_code');
            }
        });
        
        // Add foreign keys if companies table exists and columns were added
        // Using try-catch to handle cases where foreign keys might already exist
        if (Schema::hasTable('companies') && Schema::hasColumn('users', 'company_code')) {
            try {
                Schema::table('users', function (Blueprint $table) {
                    $table->foreign('company_code')
                        ->references('code')
                        ->on('companies')
                        ->onDelete('set null');
                });
            } catch (\Illuminate\Database\QueryException $e) {
                // Foreign key might already exist, ignore the error
                // PostgreSQL: "already exists", MySQL: "Duplicate key name"
                if (!str_contains($e->getMessage(), 'already exists') && 
                    !str_contains($e->getMessage(), 'Duplicate key name') &&
                    !str_contains($e->getMessage(), 'duplicate key')) {
                    throw $e;
                }
            }
        }
        
        if (Schema::hasTable('companies') && Schema::hasColumn('users', 'waiting_company_code')) {
            try {
                Schema::table('users', function (Blueprint $table) {
                    $table->foreign('waiting_company_code')
                        ->references('code')
                        ->on('companies')
                        ->onDelete('set null');
                });
            } catch (\Illuminate\Database\QueryException $e) {
                // Foreign key might already exist, ignore the error
                // PostgreSQL: "already exists", MySQL: "Duplicate key name"
                if (!str_contains($e->getMessage(), 'already exists') && 
                    !str_contains($e->getMessage(), 'Duplicate key name') &&
                    !str_contains($e->getMessage(), 'duplicate key')) {
                    throw $e;
                }
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (Schema::hasColumn('users', 'waiting_company_code')) {
                $table->dropForeign(['waiting_company_code']);
                $table->dropColumn('waiting_company_code');
            }
            
            if (Schema::hasColumn('users', 'company_code')) {
                $table->dropForeign(['company_code']);
                $table->dropColumn('company_code');
            }
        });
    }
};

