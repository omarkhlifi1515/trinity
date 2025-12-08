<?php

namespace App\Models;
use Illuminate\Database\Eloquent\Model;

class Message extends Model
{

    protected $table = 'messages';

    protected $fillable = [
        'topic_id',
        'sender_id',
        "sender_type",
        'content',
        'read_at'
    ];
    protected $with = []; // Removed eager loading to prevent issues with AI sender
    
    public function sender()
    {
        // Always return a relationship instance - Eloquent requires this
        // For AI type, the relationship will be null but the method must return a relationship
        return $this->morphTo('sender', 'sender_type', 'sender_id');
    }


    public function topic()
    {
        return $this->belongsTo(Topic::class);
    }

}