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
        Schema::create('companies', function (Blueprint $table) {
            $table->id();
            $table->string('code')->unique()->index(); // Company code that users will use to join
            $table->string('name'); // Company name
            $table->text('description')->nullable(); // Optional company description
            $table->foreignId('created_by')->nullable()->constrained('users')->onDelete('set null'); // Admin who created it
            $table->boolean('is_active')->default(true); // Whether the company code is active
            $table->timestamps();
            $table->softDeletes();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('companies');
    }
};

