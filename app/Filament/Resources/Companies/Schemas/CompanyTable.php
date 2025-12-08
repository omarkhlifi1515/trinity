<?php

namespace App\Filament\Resources\Companies\Schemas;

use Filament\Tables\Table;
use App\Models\Company;
use Filament\Tables\Filters\{Filter, SelectFilter};
use Filament\Tables\Columns\{TextColumn, ToggleColumn, IconColumn};
use Illuminate\Database\Eloquent\Builder;
use Filament\Actions\{ActionGroup, EditAction, ViewAction, DeleteAction, BulkActionGroup, DeleteBulkAction};

class CompanyTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->query(
                Company::query()
                    ->with(['creator'])
                    ->latest()
            )
            ->filters([
                Filter::make('is_active')
                    ->label('Active Companies')
                    ->query(fn(Builder $query): Builder => $query->where('is_active', true))
                    ->default(false),
                Filter::make('is_inactive')
                    ->label('Inactive Companies')
                    ->query(fn(Builder $query): Builder => $query->where('is_active', false))
                    ->default(false),
            ])
            ->columns([
                TextColumn::make('code')
                    ->label('Company Code')
                    ->searchable()
                    ->sortable()
                    ->copyable()
                    ->copyMessage('Company code copied!')
                    ->copyMessageDuration(1500)
                    ->badge()
                    ->color('primary')
                    ->weight('bold'),
                
                TextColumn::make('name')
                    ->label('Company Name')
                    ->searchable()
                    ->sortable(),
                
                TextColumn::make('description')
                    ->label('Description')
                    ->limit(50)
                    ->tooltip(fn ($record) => $record->description)
                    ->toggleable(isToggledHiddenByDefault: true),
                
                TextColumn::make('creator.name')
                    ->label('Created By')
                    ->searchable()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
                
                TextColumn::make('users_count')
                    ->label('Members')
                    ->counts('users')
                    ->sortable(),
                
                ToggleColumn::make('is_active')
                    ->label('Active')
                    ->toggleable(isToggledHiddenByDefault: false)
                    ->sortable(),
                
                TextColumn::make('created_at')
                    ->label('Created')
                    ->dateTime()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
                
                TextColumn::make('updated_at')
                    ->label('Updated')
                    ->dateTime()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->recordActions([
                ActionGroup::make([
                    ViewAction::make(),
                    EditAction::make(),
                    DeleteAction::make(),
                ])
            ])
            ->toolbarActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ]);
    }
}

