from fastapi import FastAPI
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv
import os


load_dotenv()
DATABASE_URL = "mysql+pymysql://usuario:password@localhost:3306/SmartFit.sql"
engine = create_engine(DATABASE_URL)
SECRET_KEY   = os.getenv("SECRET_KEY")

engine         = create_engine(DATABASE_URL)
SessionLocal   = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base           = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

from routers import router

app = FastAPI(title="SmartFit API")
app.include_router(router)