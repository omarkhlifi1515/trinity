from pydantic import BaseModel
from typing import Optional, Any, Dict


class ChatRequest(BaseModel):
    """Request schema for chat messages sent by clients.

    - user_id: identifier for the user (int or string depending on your auth)
    - message: the user's message to Trinity AI
    """
    user_id: Optional[int]
    message: str


class StatsResponse(BaseModel):
    """Response schema for dashboard statistics."""
    total_employees: int

    class Config:
        orm_mode = True


class ProfileModel(BaseModel):
    id: Optional[int]
    full_name: str
    email: Optional[str]
    role: Optional[str]
    extra: Optional[Dict[str, Any]] = None


class SectorModel(BaseModel):
    id: Optional[int]
    name: str
    description: Optional[str] = None


class GradeModel(BaseModel):
    id: Optional[int]
    name: str
    level: Optional[int] = None


class ChatLogModel(BaseModel):
    id: Optional[int]
    user_id: Optional[int]
    role: str
    message: str
    created_at: Optional[str] = None


class AIAnalysisModel(BaseModel):
    id: Optional[int]
    user_id: Optional[int]
    input_text: str
    analysis: str
    meta: Optional[Dict[str, Any]] = None
    created_at: Optional[str] = None
