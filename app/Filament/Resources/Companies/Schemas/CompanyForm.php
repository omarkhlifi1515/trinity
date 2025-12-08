<?php

namespace App\Filament\Resources\Companies\Schemas;

use Filament\Forms\Components\{TextInput, Textarea, Toggle};
use Filament\Schemas\Schema;
use Filament\Schemas\Components\{Section, Grid};
use Illuminate\Support\Str;

class CompanyForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Section::make('Company Information')
                    ->schema([
                        Grid::make(2)->schema([
                            TextInput::make('code')
                                ->required()
                                ->unique(ignoreRecord: true)
                                ->maxLength(50)
                                ->label('Company Code')
                                ->placeholder('Enter unique company code (e.g., ABC123)')
                                ->helperText('This code will be used by employees to join the company')
                                ->columnSpan(1)
                                ->dehydrated()
                                ->afterStateUpdated(function ($state, callable $set) {
                                    // Convert to uppercase and remove spaces
                                    $normalized = Str::upper(Str::replace(' ', '', $state));
                                    if ($state !== $normalized) {
                                        $set('code', $normalized);
                                    }
                                }),
                            
                            TextInput::make('name')
                                ->required()
                                ->maxLength(255)
                                ->label('Company Name')
                                ->placeholder('Enter company name')
                                ->columnSpan(1),
                        ]),
                        
                        Textarea::make('description')
                            ->label('Description')
                            ->placeholder('Enter company description (optional)')
                            ->rows(3)
                            ->columnSpanFull(),
                            
                        Toggle::make('is_active')
                            ->label('Active')
                            ->default(true)
                            ->helperText('Inactive company codes cannot be used by employees to join')
                            ->columnSpanFull(),
                    ])
                    ->columnSpanFull(),
            ]);
    }
}

