<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Carbon\Carbon;

class Topic extends Model
{
    protected $table = "topics";
    protected $fillable = [
        "subject",
        "creator_id",
        "creator_type",
        "receiver_id",
        "receiver_type"
    ];

    public function creator()
    {
        return $this->morphTo();
    }

    public function receiver()
    {
        // Always return a relationship instance - Eloquent requires this
        // For AI type, the relationship will be null but the method must return a relationship
        return $this->morphTo('receiver', 'receiver_type', 'receiver_id');
    }

    public function message()
    {
        return $this->hasMany(Message::class);
    }


    // public function creator()
    // {
    //     return $this->morphTo(Employee::class, ownerKey: 'User');
    // }
}