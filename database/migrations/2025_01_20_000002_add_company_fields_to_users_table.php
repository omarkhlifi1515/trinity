<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
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
                $table->foreign('company_code')
                    ->references('code')
                    ->on('companies')
                    ->onDelete('set null');
            }
            
            if (!Schema::hasColumn('users', 'waiting_company_code')) {
                $table->string('waiting_company_code')->nullable()->index()->after('company_code');
                $table->foreign('waiting_company_code')
                    ->references('code')
                    ->on('companies')
                    ->onDelete('set null');
            }
        });
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

