import os
import logging
from dotenv import load_dotenv
import openai

from database import get_client_singleton
from crud import insert_row

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")

if not OPENAI_API_KEY:
    logging.warning("OPENAI_API_KEY not set; analysis worker will fail until configured.")
else:
    openai.api_key = OPENAI_API_KEY

supabase = get_client_singleton()


def process_analysis(job_payload: dict):
    """Job function executed by RQ worker.

    Expects job_payload to contain at least:
      - 'text': the content to analyze
      - optionally 'user_id' and 'meta'

    The function generates an analysis via OpenAI and inserts a row into `ai_analysis`.
    """
    try:
        text = job_payload.get("text") if isinstance(job_payload, dict) else str(job_payload)
        user_id = job_payload.get("user_id") if isinstance(job_payload, dict) else None
        meta = job_payload.get("meta") if isinstance(job_payload, dict) else None

        if not text:
            logging.error("process_analysis called without text")
            return None

        system_prompt = (
            "You are Trinity AI Analyst. Produce a concise structured analysis of the input, "
            "highlighting entities, suggested actions, and a short summary."
        )

        response = openai.ChatCompletion.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": text},
            ],
            max_tokens=800,
            temperature=0.0,
        )

        analysis_text = response["choices"][0]["message"]["content"].strip()

        # Insert analysis into Supabase `ai_analysis` table. Expected columns: user_id, input_text, analysis, meta
        payload = {
            "user_id": user_id,
            "input_text": text,
            "analysis": analysis_text,
            "meta": meta,
        }

        inserted = insert_row(os.getenv("AI_ANALYSIS_TABLE", "ai_analysis"), payload)
        logging.info("Inserted ai_analysis row: %s", inserted)
        return inserted

    except Exception:
        logging.exception("process_analysis failed")
        return None
