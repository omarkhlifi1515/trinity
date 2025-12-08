<?php

namespace App\Filament\Widgets;

use Filament\Widgets\Widget;

class ChatBotWidget extends Widget
{
    protected string $view = 'filament.widgets.chat-bot-widget';
    protected int | string | array $columnSpan = 'full';
}