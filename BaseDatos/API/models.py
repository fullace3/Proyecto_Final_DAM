from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from main import Base
from datetime import datetime

class Usuario(Base):
    __tablename__ = "USUARIO"

    id_usuario    = Column(Integer, primary_key=True, index=True)
    nombre        = Column(String(100), nullable=False)
    email         = Column(String(100), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    fecha_registro = Column(DateTime, default=datetime.utcnow)

    rutinas = relationship("Rutina", back_populates="usuario", cascade="all, delete")
    dietas  = relationship("Dieta",  back_populates="usuario", cascade="all, delete")
    medidas = relationship("MedidaCorporal", back_populates="usuario", cascade="all, delete")

class Rutina(Base):
    __tablename__ = "RUTINA"

    id_rutina   = Column(Integer, primary_key=True, index=True)
    nombre      = Column(String(100), nullable=False)
    descripcion = Column(String(500))
    id_usuario  = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))

    usuario    = relationship("Usuario", back_populates="rutinas")
    ejercicios = relationship("RutinaEjercicio", back_populates="rutina", cascade="all, delete")

class Ejercicio(Base):
    __tablename__ = "EJERCICIO"

    id_ejercicio      = Column(Integer, primary_key=True, index=True)
    nombre            = Column(String(255), nullable=False)
    grupo_muscular    = Column(String(100))
    musculo_principal = Column(String(100))
    equipamiento      = Column(String(100))
    descripcion       = Column(String(250))
    imagen            = Column(String(255))

class RutinaEjercicio(Base):
    __tablename__ = "RUTINA_EJERCICIO"

    id_rutina    = Column(Integer, ForeignKey("RUTINA.id_rutina",       ondelete="CASCADE"), primary_key=True)
    id_ejercicio = Column(Integer, ForeignKey("EJERCICIO.id_ejercicio", ondelete="CASCADE"), primary_key=True)
    series       = Column(Integer, default=3)
    repeticiones = Column(Integer, default=10)
    orden        = Column(Integer)

    rutina    = relationship("Rutina",    back_populates="ejercicios")
    ejercicio = relationship("Ejercicio")

class Dieta(Base):
    __tablename__ = "DIETA"

    id_dieta          = Column(Integer, primary_key=True, index=True)
    nombre            = Column(String(100), nullable=False)
    descripcion       = Column(String(500))
    objetivo_calorico = Column(Integer, nullable=False)
    proteinas_g       = Column(Float)
    carbohidratos_g   = Column(Float)
    grasas_g          = Column(Float)
    fecha_inicio      = Column(DateTime, nullable=False)
    fecha_fin         = Column(DateTime)
    id_usuario        = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))

    usuario = relationship("Usuario", back_populates="dietas")

class Comida(Base):
    __tablename__ = "COMIDA"

    id_comida          = Column(Integer, primary_key=True, index=True)
    nombre             = Column(String(100), nullable=False)
    calorias_100g      = Column(Integer, nullable=False)
    proteinas_100g     = Column(Integer, nullable=False)
    carbohidratos_100g = Column(Integer, nullable=False)
    grasas_100g        = Column(Integer, nullable=False)
    id_usuario         = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"), nullable=True)
    id_dieta           = Column(Integer, ForeignKey("DIETA.id_dieta",     ondelete="SET NULL"), nullable=True)
    imagen             = Column(String(255))

class MedidaCorporal(Base):
    __tablename__ = "MEDIDA_CORPORAL"

    id_medida          = Column(Integer, primary_key=True, index=True)
    id_usuario         = Column(Integer, ForeignKey("USUARIO.id_usuario", ondelete="CASCADE"))
    fecha              = Column(DateTime, nullable=False, default=datetime.utcnow) 
    peso_kg            = Column(Float, nullable=False)
    altura_cm          = Column(Float)
    pecho_cm           = Column(Float)
    pierna_cm          = Column(Float)
    brazo_cm           = Column(Float)
    grasa_corporal_pct = Column(Float)

    usuario = relationship("Usuario", back_populates="medidas")