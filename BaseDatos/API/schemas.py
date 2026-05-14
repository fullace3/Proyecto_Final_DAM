from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime

# ══════════════════════════════════════════════
#  USUARIO
# ══════════════════════════════════════════════

class UsuarioCreate(BaseModel):
    nombre: str
    email: EmailStr
    password: str 

class UsuarioOut(BaseModel):
    id_usuario: int
    nombre: str
    email: str
    fecha_registro: datetime

    class Config:
        from_attributes = True

class LoginSchema(BaseModel):
    nombre: str
    password: str

class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    id_usuario: int

# ══════════════════════════════════════════════
#  MEDIDA CORPORAL
# ══════════════════════════════════════════════

class MedidaCreate(BaseModel):
    id_usuario: int
    peso_kg: float
    altura_cm: Optional[float] = None
    pecho_cm: Optional[float] = None
    pierna_cm: Optional[float] = None
    brazo_cm: Optional[float] = None
    grasa_corporal_pct: Optional[float] = None
    edad: Optional[int] = None
    sexo: Optional[str] = None

class MedidaOut(MedidaCreate):
    id_medida: int
    fecha: datetime

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  EJERCICIO
# ══════════════════════════════════════════════

class EjercicioCreate(BaseModel):
    nombre: str
    grupo_muscular: Optional[str] = None
    musculo_principal: Optional[str] = None
    equipamiento: Optional[str] = None
    descripcion: Optional[str] = None
    imagen: Optional[str] = None

class EjercicioOut(EjercicioCreate):
    id_ejercicio: int

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  RUTINA
# ══════════════════════════════════════════════

class RutinaCreate(BaseModel):
    nombre: str
    id_usuario: int

class RutinaOut(RutinaCreate):
    id_rutina: int

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  RUTINA_EJERCICIO  (tabla intermedia)
# ══════════════════════════════════════════════

class RutinaEjercicioCreate(BaseModel):
    id_rutina: int
    id_ejercicio: int
    series: int = 3
    repeticiones: int = 10
    orden: int

class RutinaEjercicioOut(RutinaEjercicioCreate):

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  DIETA
# ══════════════════════════════════════════════

class DietaCreate(BaseModel):
    nombre: str
    descripcion: Optional[str] = None
    objetivo_calorico: int
    proteinas_g: float
    carbohidratos_g: float
    grasas_g: float
    fecha_inicio: datetime
    fecha_fin: Optional[datetime] = None
    activo: bool = False
    id_usuario: int

class DietaOut(DietaCreate):
    id_dieta: int
    activo: bool

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  COMIDA
# ══════════════════════════════════════════════

class ComidaCreate(BaseModel):
    nombre: str
    calorias_100g: int
    proteinas_100g: int
    carbohidratos_100g: int
    grasas_100g: int
    dia: Optional[str] = None
    id_usuario: int
    imagen: Optional[str] = None

class ComidaOut(ComidaCreate):
    id_comida: int
    dia: Optional[str]

    class Config:
        from_attributes = True

# ══════════════════════════════════════════════
#  HISTORIAL DE ENTRENAMIENTO
# ══════════════════════════════════════════════

class HistorialCreate(BaseModel):
    id_usuario: int
    id_ejercicio: int
    id_rutina: int
    peso_kg: float
    repeticiones: int
    series: int
    duracion_minutos: int = 0

class HistorialOut(HistorialCreate):
    id_registro: int
    fecha: datetime

    class Config:
        from_attributes = True