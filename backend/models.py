from pydantic import BaseModel
from typing import Optional, List


class StatsResponse(BaseModel):
    total_employees: int

    model_config = {
        "from_attributes": True,
    }


class Sector(BaseModel):
    id: Optional[int]
    name: str
    description: Optional[str] = None

    model_config = {
        "from_attributes": True,
    }


class Tool(BaseModel):
    id: Optional[int]
    name: str
    url: Optional[str] = None
    sector_id: Optional[int] = None

    model_config = {
        "from_attributes": True,
    }


class Profile(BaseModel):
    id: Optional[int]
    user_id: Optional[str]
    full_name: Optional[str]
    email: Optional[str]
    role: Optional[str] = "employee"
    sector_id: Optional[int] = None
    grade_id: Optional[int] = None

    model_config = {
        "from_attributes": True,
    }


class Message(BaseModel):
    id: Optional[int]
    sender_id: Optional[str]
    receiver_id: Optional[str]
    content: str
    created_at: Optional[str] = None

    model_config = {
        "from_attributes": True,
    }


class UserUpdate(BaseModel):
    sector_id: Optional[int] = None
    grade_id: Optional[int] = None
    role: Optional[str] = None

    model_config = {
        "from_attributes": True,
    }
