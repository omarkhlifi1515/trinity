<?php

namespace App\Filament\Resources\Companies\Pages;

use App\Filament\Resources\Companies\CompanyResource;
use Filament\Resources\Pages\CreateRecord;
use Illuminate\Support\Facades\Auth;

class CreateCompany extends CreateRecord
{
    protected static string $resource = CompanyResource::class;
    
    protected function mutateFormDataBeforeCreate(array $data): array
    {
        // Automatically set the creator to the current admin user
        $data['created_by'] = Auth::id();
        
        return $data;
    }
}

