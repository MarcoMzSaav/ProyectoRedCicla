const express = require('express');
const router = express.Router();
const db = require('../db');

//crear nuevo empleado

router.post('/empleados', async (req, res) => {
  const {
    nombre,
    rut,
    correo,
    telefono,
    clave,
    rol,
    licencia
  } = req.body;

  await db.query(`
    INSERT INTO empleados (
      nombre_completo,
      rut,
      correo,
      telefono,
      clave_acceso,
      rol,
      licencia_conducir
    )
    VALUES ($1,$2,$3,$4,$5,$6,$7)
  `, [nombre, rut, correo, telefono, clave, rol, licencia]);

  res.json({ ok: true });
});

//crear zona 
router.post('/zonas', async (req, res) => {
  const { nombre } = req.body;

  await db.query(
    'INSERT INTO zonas (nombre) VALUES ($1)',
    [nombre]
  );

  res.json({ ok: true });
});

//crear punto de reciclaje

router.post('/puntos', async (req, res) => {
  const {
    zona_id,
    direccion,
    latitud,
    longitud
  } = req.body;

  await db.query(`
    INSERT INTO puntos_reciclaje (
      zona_id,
      direccion,
      latitud,
      longitud,
      estado
    )
    VALUES ($1,$2,$3,$4,true)
  `, [zona_id, direccion, latitud, longitud]);

  res.json({ ok: true });
});

//crear contenedor

router.post('/contenedores', async (req, res) => {
  const { punto_id, capacidad } = req.body;

  await db.query(`
    INSERT INTO contenedores (
      punto_id,
      capacidad,
      estado
    )
    VALUES ($1,$2,true)
  `, [punto_id, capacidad]);

  res.json({ ok: true });
});

//crear ruta

router.post('/rutas', async (req, res) => {
  const { zona_id, nombre } = req.body;

  await db.query(`
    INSERT INTO rutas (zona_id, nombre)
    VALUES ($1,$2)
  `, [zona_id, nombre]);

  res.json({ ok: true });
});

//asignar punto a ruta

router.put('/asignar-punto', async (req, res) => {
  const { punto_id, ruta_id } = req.body;

  await db.query(`
    UPDATE puntos_reciclaje
    SET ruta_id = $1
    WHERE id = $2
  `, [ruta_id, punto_id]);

  res.json({ ok: true });
});

//crear ruta activa

router.post('/rutas-activas', async (req, res) => {
  const {
    ruta_id,
    camion_id,
    conductor_id,
    ayudante_id
  } = req.body;

  await db.query(`
    INSERT INTO rutas_activas (
      ruta_id,
      camion_id,
      conductor_id,
      ayudante_id,
      fecha_inicio
    )
    VALUES ($1,$2,$3,$4,NOW())
  `, [ruta_id, camion_id, conductor_id, ayudante_id]);

  res.json({ ok: true });
});

//reporte por zona

router.get('/reportes/zona/:id', async (req, res) => {
  const zonaId = req.params.id;

  const result = await db.query(`
    SELECT 
      z.nombre,
      SUM(rr.cantidad_retirada) AS total
    FROM registros_retiro rr
    JOIN rutas_activas ra ON rr.ruta_activa_id = ra.id
    JOIN rutas r ON ra.ruta_id = r.id
    JOIN zonas z ON r.zona_id = z.id
    WHERE z.id = $1
    GROUP BY z.nombre
  `, [zonaId]);

  res.json(result.rows);
});



module.exports = router;