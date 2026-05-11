from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from jose import jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta

from main import get_db, SECRET_KEY
import models, schemas

router = APIRouter()

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
ALGORITHM   = "HS256"
TOKEN_HORAS = 24

def hashear(password: str) -> str:
    return pwd_context.hash(password)

def verificar(password: str, hashed: str) -> bool:
    return pwd_context.verify(password, hashed)

def crear_token(data: dict) -> str:
    payload = data.copy()
    payload["exp"] = datetime.utcnow() + timedelta(hours=TOKEN_HORAS)
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


# ══════════════════════════════════════════════
#  USUARIOS
# ══════════════════════════════════════════════

@router.post("/usuarios/registro", response_model=schemas.UsuarioOut, status_code=201, tags=["Usuarios"])
def registrar(datos: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    if db.query(models.Usuario).filter(models.Usuario.email == datos.email).first():
        raise HTTPException(status_code=400, detail="El email ya está registrado")
    usuario = models.Usuario(
        nombre        = datos.nombre,
        email         = datos.email,
        password_hash = hashear(datos.password)
    )
    db.add(usuario)
    db.commit()
    db.refresh(usuario)
    return usuario

@router.post("/usuarios/login", response_model=schemas.TokenOut, tags=["Usuarios"])
def login(datos: schemas.LoginSchema, db: Session = Depends(get_db)):
    usuario = db.query(models.Usuario).filter(models.Usuario.nombre == datos.nombre).first()
    if not usuario or not verificar(datos.password, usuario.password_hash):
        raise HTTPException(status_code=401, detail="Credenciales incorrectas")
    token = crear_token({"sub": str(usuario.id_usuario), "email": usuario.email})
    return {"access_token": token, "id_usuario": usuario.id_usuario}

@router.get("/usuarios/{id_usuario}", response_model=schemas.UsuarioOut, tags=["Usuarios"])
def obtener_usuario(id_usuario: int, db: Session = Depends(get_db)):
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return usuario

@router.put("/usuarios/{id_usuario}", response_model=schemas.UsuarioOut, tags=["Usuarios"])
def actualizar_usuario(id_usuario: int, datos: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    usuario.nombre        = datos.nombre
    usuario.email         = datos.email
    usuario.password_hash = hashear(datos.password)
    db.commit()
    db.refresh(usuario)
    return usuario

@router.delete("/usuarios/{id_usuario}", status_code=204, tags=["Usuarios"])
def eliminar_usuario(id_usuario: int, db: Session = Depends(get_db)):
    usuario = db.query(models.Usuario).filter(models.Usuario.id_usuario == id_usuario).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    db.delete(usuario)
    db.commit()


# ══════════════════════════════════════════════
#  MEDIDAS CORPORALES
# ══════════════════════════════════════════════

@router.post("/medidas", response_model=schemas.MedidaOut, status_code=201, tags=["Medidas"])
def crear_medida(datos: schemas.MedidaCreate, db: Session = Depends(get_db)):
    medida = models.MedidaCorporal(**datos.model_dump())
    db.add(medida)
    db.commit()
    db.refresh(medida)
    return medida

@router.get("/medidas/usuario/{id_usuario}", response_model=list[schemas.MedidaOut], tags=["Medidas"])
def historial_medidas(id_usuario: int, db: Session = Depends(get_db)):
    return db.query(models.MedidaCorporal)\
             .filter(models.MedidaCorporal.id_usuario == id_usuario)\
             .order_by(models.MedidaCorporal.fecha.asc()).all()

@router.put("/medidas/{id_medida}", response_model=schemas.MedidaOut, tags=["Medidas"])
def editar_medida(id_medida: int, datos: schemas.MedidaCreate, db: Session = Depends(get_db)):
    medida = db.query(models.MedidaCorporal).filter(models.MedidaCorporal.id_medida == id_medida).first()
    if not medida:
        raise HTTPException(status_code=404, detail="Medida no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(medida, campo, valor)
    db.commit()
    db.refresh(medida)
    return medida

@router.delete("/medidas/{id_medida}", status_code=204, tags=["Medidas"])
def eliminar_medida(id_medida: int, db: Session = Depends(get_db)):
    medida = db.query(models.MedidaCorporal).filter(models.MedidaCorporal.id_medida == id_medida).first()
    if not medida:
        raise HTTPException(status_code=404, detail="Medida no encontrada")
    db.delete(medida)
    db.commit()


# ══════════════════════════════════════════════
#  EJERCICIOS
# ══════════════════════════════════════════════

@router.post("/ejercicios", response_model=schemas.EjercicioOut, status_code=201, tags=["Ejercicios"])
def crear_ejercicio(datos: schemas.EjercicioCreate, db: Session = Depends(get_db)):
    ejercicio = models.Ejercicio(**datos.model_dump())
    db.add(ejercicio)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio

@router.get("/ejercicios", response_model=list[schemas.EjercicioOut], tags=["Ejercicios"])
def todos_ejercicios(db: Session = Depends(get_db)):
    return db.query(models.Ejercicio).order_by(models.Ejercicio.nombre).all()

@router.get("/ejercicios/grupo/{grupo}", response_model=list[schemas.EjercicioOut], tags=["Ejercicios"])
def ejercicios_por_grupo(grupo: str, db: Session = Depends(get_db)):
    return db.query(models.Ejercicio)\
             .filter(models.Ejercicio.grupo_muscular == grupo).all()

@router.put("/ejercicios/{id_ejercicio}", response_model=schemas.EjercicioOut, tags=["Ejercicios"])
def editar_ejercicio(id_ejercicio: int, datos: schemas.EjercicioCreate, db: Session = Depends(get_db)):
    ejercicio = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == id_ejercicio).first()
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    for campo, valor in datos.model_dump().items():
        setattr(ejercicio, campo, valor)
    db.commit()
    db.refresh(ejercicio)
    return ejercicio

@router.delete("/ejercicios/{id_ejercicio}", status_code=204, tags=["Ejercicios"])
def eliminar_ejercicio(id_ejercicio: int, db: Session = Depends(get_db)):
    ejercicio = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == id_ejercicio).first()
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    db.delete(ejercicio)
    db.commit()


# ══════════════════════════════════════════════
#  RUTINAS
# ══════════════════════════════════════════════

@router.post("/rutinas", response_model=schemas.RutinaOut, status_code=201, tags=["Rutinas"])
def crear_rutina(datos: schemas.RutinaCreate, db: Session = Depends(get_db)):
    rutina = models.Rutina(**datos.model_dump())
    db.add(rutina)
    db.commit()
    db.refresh(rutina)
    return rutina

@router.get("/rutinas/usuario/{id_usuario}", response_model=list[schemas.RutinaOut], tags=["Rutinas"])
def rutinas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    return db.query(models.Rutina)\
             .filter(models.Rutina.id_usuario == id_usuario).all()

@router.put("/rutinas/{id_rutina}", response_model=schemas.RutinaOut, tags=["Rutinas"])
def editar_rutina(id_rutina: int, datos: schemas.RutinaCreate, db: Session = Depends(get_db)):
    rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == id_rutina).first()
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(rutina, campo, valor)
    db.commit()
    db.refresh(rutina)
    return rutina

@router.delete("/rutinas/{id_rutina}", status_code=204, tags=["Rutinas"])
def eliminar_rutina(id_rutina: int, db: Session = Depends(get_db)):
    rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == id_rutina).first()
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    db.delete(rutina)
    db.commit()


# ══════════════════════════════════════════════
#  RUTINA_EJERCICIO  (tabla intermedia)
# ══════════════════════════════════════════════

@router.post("/rutinas/ejercicios", response_model=schemas.RutinaEjercicioOut, status_code=201, tags=["Rutina-Ejercicio"])
def añadir_ejercicio_a_rutina(datos: schemas.RutinaEjercicioCreate, db: Session = Depends(get_db)):
    relacion = models.RutinaEjercicio(**datos.model_dump())
    db.merge(relacion)
    db.commit()
    return relacion

@router.get("/rutinas/{id_rutina}/ejercicios", response_model=list[schemas.RutinaEjercicioOut], tags=["Rutina-Ejercicio"])
def ejercicios_de_rutina(id_rutina: int, db: Session = Depends(get_db)):
    return db.query(models.RutinaEjercicio)\
             .filter(models.RutinaEjercicio.id_rutina == id_rutina)\
             .order_by(models.RutinaEjercicio.orden).all()

@router.put("/rutinas/{id_rutina}/ejercicios/{id_ejercicio}", response_model=schemas.RutinaEjercicioOut, tags=["Rutina-Ejercicio"])
def editar_ejercicio_de_rutina(id_rutina: int, id_ejercicio: int,
                                datos: schemas.RutinaEjercicioCreate,
                                db: Session = Depends(get_db)):
    relacion = db.query(models.RutinaEjercicio).filter(
        models.RutinaEjercicio.id_rutina   == id_rutina,
        models.RutinaEjercicio.id_ejercicio == id_ejercicio
    ).first()
    if not relacion:
        raise HTTPException(status_code=404, detail="Relación no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(relacion, campo, valor)
    db.commit()
    db.refresh(relacion)
    return relacion

@router.delete("/rutinas/{id_rutina}/ejercicios/{id_ejercicio}", status_code=204, tags=["Rutina-Ejercicio"])
def quitar_ejercicio_de_rutina(id_rutina: int, id_ejercicio: int, db: Session = Depends(get_db)):
    relacion = db.query(models.RutinaEjercicio).filter(
        models.RutinaEjercicio.id_rutina   == id_rutina,
        models.RutinaEjercicio.id_ejercicio == id_ejercicio
    ).first()
    if not relacion:
        raise HTTPException(status_code=404, detail="Relación no encontrada")
    db.delete(relacion)
    db.commit()


# ══════════════════════════════════════════════
#  DIETAS
# ══════════════════════════════════════════════

@router.post("/dietas", response_model=schemas.DietaOut, status_code=201, tags=["Dietas"])
def crear_dieta(datos: schemas.DietaCreate, db: Session = Depends(get_db)):
    dieta = models.Dieta(**datos.model_dump())
    db.add(dieta)
    db.commit()
    db.refresh(dieta)
    return dieta

@router.get("/dietas/usuario/{id_usuario}", response_model=list[schemas.DietaOut], tags=["Dietas"])
def dietas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    return db.query(models.Dieta)\
             .filter(models.Dieta.id_usuario == id_usuario)\
             .order_by(models.Dieta.fecha_inicio.desc()).all()

@router.get("/dietas/usuario/{id_usuario}/actual", response_model=schemas.DietaOut, tags=["Dietas"])
def dieta_actual(id_usuario: int, db: Session = Depends(get_db)):
    dieta = db.query(models.Dieta)\
              .filter(models.Dieta.id_usuario == id_usuario)\
              .order_by(models.Dieta.fecha_inicio.desc()).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="No hay ninguna dieta registrada")
    return dieta

@router.put("/dietas/{id_dieta}", response_model=schemas.DietaOut, tags=["Dietas"])
def editar_dieta(id_dieta: int, datos: schemas.DietaCreate, db: Session = Depends(get_db)):
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == id_dieta).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="Dieta no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(dieta, campo, valor)
    db.commit()
    db.refresh(dieta)
    return dieta

@router.delete("/dietas/{id_dieta}", status_code=204, tags=["Dietas"])
def eliminar_dieta(id_dieta: int, db: Session = Depends(get_db)):
    dieta = db.query(models.Dieta).filter(models.Dieta.id_dieta == id_dieta).first()
    if not dieta:
        raise HTTPException(status_code=404, detail="Dieta no encontrada")
    db.delete(dieta)
    db.commit()


# ══════════════════════════════════════════════
#  COMIDAS
# ══════════════════════════════════════════════

@router.post("/comidas", response_model=schemas.ComidaOut, status_code=201, tags=["Comidas"])
def crear_comida(datos: schemas.ComidaCreate, db: Session = Depends(get_db)):
    comida = models.Comida(**datos.model_dump())
    db.add(comida)
    db.commit()
    db.refresh(comida)
    return comida

@router.get("/comidas/usuario/{id_usuario}", response_model=list[schemas.ComidaOut], tags=["Comidas"])
def comidas_usuario(id_usuario: int, db: Session = Depends(get_db)):
    return db.query(models.Comida)\
             .filter(models.Comida.id_usuario == id_usuario)\
             .order_by(models.Comida.nombre).all()

@router.get("/comidas/usuario/{id_usuario}/buscar", response_model=list[schemas.ComidaOut], tags=["Comidas"])
def buscar_comida(id_usuario: int, nombre: str, db: Session = Depends(get_db)):
    return db.query(models.Comida).filter(
        models.Comida.id_usuario == id_usuario,
        models.Comida.nombre.ilike(f"%{nombre}%")
    ).all()

@router.put("/comidas/{id_comida}", response_model=schemas.ComidaOut, tags=["Comidas"])
def editar_comida(id_comida: int, datos: schemas.ComidaCreate, db: Session = Depends(get_db)):
    comida = db.query(models.Comida).filter(models.Comida.id_comida == id_comida).first()
    if not comida:
        raise HTTPException(status_code=404, detail="Comida no encontrada")
    for campo, valor in datos.model_dump().items():
        setattr(comida, campo, valor)
    db.commit()
    db.refresh(comida)
    return comida

@router.delete("/comidas/{id_comida}", status_code=204, tags=["Comidas"])
def eliminar_comida(id_comida: int, db: Session = Depends(get_db)):
    comida = db.query(models.Comida).filter(models.Comida.id_comida == id_comida).first()
    if not comida:
        raise HTTPException(status_code=404, detail="Comida no encontrada")
    db.delete(comida)
    db.commit()

# ══════════════════════════════════════════════
#  HISTORIAL DE ENTRENAMIENTO
# ══════════════════════════════════════════════

@router.post("/historial", response_model=schemas.HistorialOut, status_code=201, tags=["Historial"])
def registrar_historial(datos: schemas.HistorialCreate, db: Session = Depends(get_db)):
    registro = models.HistorialEntrenamiento(**datos.model_dump())
    db.add(registro)
    db.commit()
    db.refresh(registro)
    return registro

@router.get("/progreso/volumen/{id_usuario}", tags=["Progreso"])
def obtener_volumen_progreso(id_usuario: int, db: Session = Depends(get_db)):
    # Traemos todos los registros del usuario ordenados por fecha
    logs = db.query(models.HistorialEntrenamiento)\
             .filter(models.HistorialEntrenamiento.id_usuario == id_usuario)\
             .order_by(models.HistorialEntrenamiento.fecha.asc()).all()
    
    progreso = []
    for log in logs:
        volumen = log.peso_kg * log.repeticiones * log.series
        progreso.append({
            "fecha": log.fecha.strftime("%Y-%m-%d"),
            "volumen": volumen
        })
    return progreso

@router.get("/historial/usuario/{id_usuario}", tags=["Historial"])
def historial_usuario(id_usuario: int, db: Session = Depends(get_db)):
    logs = db.query(
        models.HistorialEntrenamiento,
        models.Ejercicio.nombre
    ).join(
        models.Ejercicio,
        models.HistorialEntrenamiento.id_ejercicio == models.Ejercicio.id_ejercicio
    ).filter(
        models.HistorialEntrenamiento.id_usuario == id_usuario
    ).order_by(
        models.HistorialEntrenamiento.fecha.desc()
    ).all()

    return [
        {
            "id_registro":      log.HistorialEntrenamiento.id_registro,
            "id_ejercicio":     log.HistorialEntrenamiento.id_ejercicio,
            "nombre_ejercicio": log.nombre,
            "peso_kg":          log.HistorialEntrenamiento.peso_kg,
            "repeticiones":     log.HistorialEntrenamiento.repeticiones,
            "series":           log.HistorialEntrenamiento.series,
            "duracion_minutos": log.HistorialEntrenamiento.duracion_minutos,
            "fecha":            log.HistorialEntrenamiento.fecha.isoformat()
        }
        for log in logs
    ]