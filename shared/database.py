import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session

DB_HOST = os.environ.get("DB_HOST", "localhost")
DB_PORT = os.environ.get("DB_PORT", "5432")
DB_NAME = os.environ.get("DB_NAME", "pretrade")
DB_USER = os.environ.get("DB_USERNAME", "pretrade")
DB_PASS = os.environ.get("DB_PASSWORD", "pretrade")

DATABASE_URL = f"postgresql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

engine = create_engine(DATABASE_URL, pool_size=5, max_overflow=10)
SessionLocal = scoped_session(sessionmaker(bind=engine))


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
