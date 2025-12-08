<?php

namespace App\Bots;

/**
 * AI Bot class for polymorphic relationships
 * This is a placeholder class (not a Model) for the chatbot's AI responses
 * 
 * We don't extend Model to prevent Eloquent from trying to query a database table.
 * The models (Message, Topic) handle this type specially in their morphTo methods.
 */
class AI
{
    // This is just a class identifier for polymorphic relationships
    // It doesn't need any methods or properties
}

