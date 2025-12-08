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
        // 1. Threads (Conversation container)
        Schema::create('ai_threads', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('subject')->nullable();
            $table->timestamps();
        });

        // 2. Messages (The actual chat)
        Schema::create('ai_messages', function (Blueprint $table) {
            $table->id();
            // Connect to ai_threads table
            $table->foreignId('thread_id')->constrained('ai_threads')->cascadeOnDelete();
            $table->enum('role', ['user', 'assistant']);
            $table->text('content');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('ai_messages');
        Schema::dropIfExists('ai_threads');
    }
};