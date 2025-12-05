FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       ca-certificates \
       git \
       nmap \
       wget \
       build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install sqlmap by cloning its repository
RUN git clone --depth 1 https://github.com/sqlmapproject/sqlmap.git /opt/sqlmap || true

WORKDIR /app

COPY ./c2/requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY ./c2 /app

# Create a directory for scan outputs
RUN mkdir -p /var/lib/trinity

EXPOSE 8000

# Use gunicorn to serve the Flask app. Keep a single worker so background threads live in the same process.
CMD ["gunicorn", "app:app", "--bind", "0.0.0.0:8000", "--workers", "1", "--threads", "4", "--log-level", "info"]
