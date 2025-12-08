<?php

namespace App\Observers;

use App\Filament\Resources\Messages\MessageResource;
use App\Models\{Message, Employee, User};
use Filament\Notifications\Notification;
use Filament\Actions\Action;

class MessageObserver
{
    /**
     * Handle the Message "created" event.
     */
    public function created(Message $message): void
    {
        // Skip notifications for AI chatbot messages
        if ($message->sender_type === 'App\\Bots\\AI' || $message->topic->receiver_type === 'App\\Bots\\AI') {
            return;
        }

        $topic = $message->topic;
        $recipient = null;
        
        // Only process if sender is a real model (not AI)
        if ($message->sender && $topic->creator) {
            if ($message->sender->is($topic->creator)) {
                $recipient = $topic->receiver;
            } elseif ($topic->receiver && $message->sender->is($topic->receiver)) {
                $recipient = $topic->creator;
            }
        }

        // Only send notification if recipient is a real user/employee (not AI)
        if (!$recipient || !($recipient instanceof User || $recipient instanceof Employee)) {
            return;
        }

        $url = MessageResource::getUrl('view', ['record' => $topic]);

        if ($recipient instanceof User && $message->sender instanceof Employee) {
            $url = str_replace('/portal', '', $url);
        } elseif ($recipient instanceof Employee && $message->sender instanceof User) {
            $parsed = parse_url(MessageResource::getUrl('view', ['record' => $topic]));
            $url = url('/portal' . $parsed['path']);
        }

        Notification::make()
            ->title('New message')
            ->body("{$topic->subject}")
            ->actions([
                Action::make('view')
                    ->url($url)
                    ->markAsRead()
                    ->close()
                    ->label('View Conversation'),
            ])
            ->info()
            ->sendToDatabase($recipient);
    }

    /**
     * Handle the Message "updated" event.
     */
    public function updated(Message $message): void
    {
        //
    }

    /**
     * Handle the Message "deleted" event.
     */
    public function deleted(Message $message): void
    {
        //
    }

    /**
     * Handle the Message "restored" event.
     */
    public function restored(Message $message): void
    {
        //
    }

    /**
     * Handle the Message "force deleted" event.
     */
    public function forceDeleted(Message $message): void
    {
        //
    }
}
