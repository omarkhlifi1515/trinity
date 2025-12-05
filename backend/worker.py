import os
import logging
from dotenv import load_dotenv
import google.generativeai as genai

from database import get_client_singleton
from crud import insert_row

load_dotenv()

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
GOOGLE_MODEL = os.getenv("GOOGLE_MODEL", "gemini-pro")

if not GOOGLE_API_KEY:
    logging.warning("GOOGLE_API_KEY not set; analysis worker will fail until configured.")
else:
    try:
        genai.configure(api_key=GOOGLE_API_KEY)
    except Exception:
        logging.exception("Failed to configure google-generativeai client in worker")

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

        prompt = system_prompt + "\n\nInput: " + text

        # Try a couple of call patterns depending on the installed google-generativeai version
        try:
            resp = genai.generate_text(model=GOOGLE_MODEL, prompt=prompt, max_output_tokens=800, temperature=0.0)
        except Exception:
            resp = genai.generate(model=GOOGLE_MODEL, prompt=prompt)

        # Conservative extraction of the returned text
        analysis_text = None
        try:
            if isinstance(resp, dict) and "candidates" in resp and resp["candidates"]:
                cand = resp["candidates"][0]
                analysis_text = cand.get("content") or cand.get("output") or cand.get("text")
        except Exception:
            logging.debug("Worker: failed dict-like extraction", exc_info=True)

        if not analysis_text:
            # Try attribute style
            try:
                cands = getattr(resp, "candidates", None)
                if cands:
                    c0 = cands[0]
                    for attr in ("content", "output", "text"):
                        if hasattr(c0, attr):
                            analysis_text = getattr(c0, attr)
                            break
            except Exception:
                logging.debug("Worker: failed object-like extraction", exc_info=True)

        if not analysis_text:
            analysis_text = str(resp)

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
