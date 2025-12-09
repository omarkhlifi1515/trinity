<?php

namespace App\Filament\Resources\Companies\CompanyResource\RelationManagers;

use Filament\Forms;
use Filament\Forms\Form;
use Filament\Resources\RelationManagers\RelationManager;
use Filament\Tables;
use Filament\Tables\Table;
use Filament\Actions\Action;
use Filament\Actions\BulkActionGroup;
use App\Models\User;
use Filament\Notifications\Notification;

class WaitlistUsersRelationManager extends RelationManager
{
    protected static string $relationship = 'waitlistUsers';

    protected static ?string $title = 'Waitlist Users';

    protected static ?string $label = 'Waitlist User';

    protected static ?string $pluralLabel = 'Waitlist Users';

    public function table(Table $table): Table
    {
        return $table
            ->recordTitleAttribute('name')
            ->columns([
                Tables\Columns\TextColumn::make('id')
                    ->label('ID')
                    ->sortable(),
                    
                Tables\Columns\TextColumn::make('name')
                    ->label('Name')
                    ->searchable()
                    ->sortable(),
                    
                Tables\Columns\TextColumn::make('email')
                    ->label('Email')
                    ->searchable()
                    ->sortable(),
                    
                Tables\Columns\TextColumn::make('created_at')
                    ->label('Request Date')
                    ->dateTime()
                    ->sortable(),
            ])
            ->filters([
                //
            ])
            ->headerActions([
                //
            ])
            ->actions([
                Action::make('accept')
                    ->label('Accept')
                    ->icon('heroicon-o-check-circle')
                    ->color('success')
                    ->requiresConfirmation()
                    ->modalHeading('Accept User')
                    ->modalDescription('Are you sure you want to accept this user into the company?')
                    ->action(function (User $record) {
                        try {
                            // Get the company code from the owner record
                            $companyCode = $this->ownerRecord->code;
                            
                            if (empty($companyCode)) {
                                throw new \Exception('Company code is empty');
                            }
                            
                            // Verify the company exists
                            $company = \App\Models\Company::where('code', $companyCode)->first();
                            if (!$company) {
                                throw new \Exception("Company with code '{$companyCode}' not found");
                            }
                            
                            // Use DB facade to update directly to bypass any model issues
                            $updated = \DB::table('users')
                                ->where('id', $record->id)
                                ->update([
                                    'company_code' => $companyCode,
                                    'waiting_company_code' => null,
                                    'updated_at' => now(),
                                ]);
                            
                            if ($updated === 0) {
                                throw new \Exception('No rows were updated. User may not exist.');
                            }
                            
                            // Refresh the record to ensure we have the latest data
                            $record->refresh();
                            
                            // Verify the update was successful
                            if ($record->company_code !== $companyCode) {
                                throw new \Exception("Company code mismatch after update. Expected: {$companyCode}, Got: " . ($record->company_code ?? 'NULL'));
                            }
                            
                            Notification::make()
                                ->success()
                                ->title('User Accepted')
                                ->body("{$record->name} has been added to the company.")
                                ->send();
                        } catch (\Exception $e) {
                            \Log::error('Failed to accept user to company', [
                                'user_id' => $record->id,
                                'user_email' => $record->email,
                                'company_code' => $this->ownerRecord->code ?? 'N/A',
                                'error' => $e->getMessage(),
                                'trace' => $e->getTraceAsString(),
                            ]);
                            
                            Notification::make()
                                ->danger()
                                ->title('Error')
                                ->body('Failed to accept user: ' . $e->getMessage())
                                ->send();
                            
                            throw $e;
                        }
                    }),
                    
                Action::make('reject')
                    ->label('Reject')
                    ->icon('heroicon-o-x-circle')
                    ->color('danger')
                    ->requiresConfirmation()
                    ->modalHeading('Reject User')
                    ->modalDescription('Are you sure you want to reject this user? They will be removed from the waitlist.')
                    ->action(function (User $record) {
                        // Remove from waitlist
                        $record->waiting_company_code = null;
                        $record->save();
                        
                        Notification::make()
                            ->success()
                            ->title('User Rejected')
                            ->body("{$record->name} has been removed from the waitlist.")
                            ->send();
                    }),
            ])
            ->bulkActions([
                BulkActionGroup::make([
                    //
                ]),
            ])
            ->emptyStateHeading('No users on waitlist')
            ->emptyStateDescription('Users who request to join this company will appear here.');
    }
}

