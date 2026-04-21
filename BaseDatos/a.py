import csv

def escape(val):
    if val is None or val == '':
        return 'NULL'
    return "'" + str(val).replace("'", "''") + "'"

# --- EJERCICIOS ---
lines = ["INSERT INTO EJERCICIO (id_ejercicio, nombre, grupo_muscular, musculo_principal, equipamiento, descripcion, imagen) VALUES"]
rows = []
with open('EjerciciosCompleto.csv', encoding='utf-8') as f:
    for r in csv.DictReader(f):
        rows.append(f"  ({r['id_ejercicio']}, {escape(r['nombre'])}, {escape(r['grupo_muscular'])}, {escape(r['musculo_principal'])}, {escape(r['equipamiento'])}, {escape(r['descripcion'])}, {escape(r['imagen'])})")

with open('inserts_ejercicios.sql', 'w', encoding='utf-8') as out:
    out.write('\n'.join(lines) + '\n' + ',\n'.join(rows) + ';\n')

# --- ALIMENTOS ---
# Nota: la tabla COMIDA en tu schema no tiene todos estos campos.
# Necesitarás crear una tabla ALIMENTO separada o ajustar COMIDA.
lines2 = ["INSERT INTO ALIMENTO (id_alimento, nombre, calorias_100g, proteinas_100g, carbohidratos_100g, grasas_100g, imagen) VALUES"]
rows2 = []
with open('alimentos_limpios.csv', encoding='utf-8') as f:
    for r in csv.DictReader(f):
        rows2.append(f"  ({r['id_alimento']}, {escape(r['nombre'])}, {r['calorias_100g']}, {r['proteinas_100g']}, {r['carbohidratos_100g']}, {r['grasas_100g']}, {escape(r['imagen'])})")

with open('inserts_alimentos.sql', 'w', encoding='utf-8') as out:
    out.write('\n'.join(lines2) + '\n' + ',\n'.join(rows2) + ';\n')

print("Generados: inserts_ejercicios.sql e inserts_alimentos.sql")