import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    openai_model: str = "gpt-4o"
    postgres_url: str = os.getenv(
        "DATABASE_URL",
        "postgresql://postgres:postgres@localhost:5432/frauddetection"
    )
    payment_service_url: str = os.getenv(
        "PAYMENT_SERVICE_URL",
        "http://localhost:8081"
    )
    fraud_service_url: str = os.getenv(
        "FRAUD_SERVICE_URL",
        "http://localhost:8082"
    )

settings = Settings()