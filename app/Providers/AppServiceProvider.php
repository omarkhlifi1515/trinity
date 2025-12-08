<?php

namespace App\Providers;

use App\Observers\MessageObserver;
use App\Observers\TaskObserver;
use Filament\Facades\Filament;
use Illuminate\Support\Facades\URL;
use Illuminate\Support\ServiceProvider;
use App\Models\{Task, Message};
use Illuminate\Support\Facades\DB;
use Illuminate\Database\Eloquent\Model;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        // Override Google Gemini (OpenAI-compatible) client binding to handle SSL issues in development
        // This must run after OpenAI service provider, so we use boot() instead
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        //
        $this->configureCommands();
        $this->configureModels();
        $this->configureUrl();
        Task::observe(TaskObserver::class);
        Message::observe(MessageObserver::class);

        // Register global Filament widgets (floating chat)
        if (class_exists(Filament::class)) {
            Filament::registerWidgets([
                \App\Filament\Widgets\ChatBotWidget::class,
            ]);
        }

    }
    private function configureCommands(): void
    {
        DB::prohibitDestructiveCommands(
            $this->app->environment('production')
        );
    }
    private function configureModels(): void
    {
        //
        Model::shouldBeStrict();
        Model::unguard();
    }
    public function configureUrl(): void
    {
        if ($this->app->environment('production')) {

            URL::forceScheme('https');
        }
    }
}
