const express = require('express');
const router = express.Router();
const db = require('../db');

// LOGIN
router.post('/login', async (req, res) => {
  const { rut, clave } = req.body;

  const result = await db.query(
    'SELECT * FROM empleados WHERE rut = $1 AND clave_acceso = $2',
    [rut, clave]
  );

  if (result.rows.length === 0) {
    return res.status(401).json({ error: 'Credenciales incorrectas' });
  }

  const user = result.rows[0];

  if (user.rol !== 'CONDUCTOR' && user.rol !== 'AYUDANTE') {
    return res.status(403).json({ error: 'No autorizado' });
  }

  res.json(user);
});

module.exports = router;

// OBTENER RUTA ACTIVA
router.get('/ruta-activa/:empleadoId', async (req, res) => {
  const empleadoId = req.params.empleadoId;

  const result = await db.query(`
    SELECT * FROM rutas_activas
    WHERE (conductor_id = $1 OR ayudante_id = $1)
    AND fecha_fin IS NULL
  `, [empleadoId]);

  if (result.rows.length === 0) {
    return res.json(null);
  }

  res.json(result.rows[0]);
});

// OBTENER PUNTOS DE UNA RUTA ACTIVA
router.get('/puntos/:rutaActivaId', async (req, res) => {
  const rutaActivaId = req.params.rutaActivaId;

  const result = await db.query(`
    SELECT p.*
    FROM puntos_reciclaje p
    JOIN rutas_activas ra ON ra.ruta_id = p.ruta_id
    WHERE ra.id = $1
  `, [rutaActivaId]);

  res.json(result.rows);
});

//registrarbretiro
router.post('/retiro', async (req, res) => {
  const {
    ruta_activa_id,
    punto_id,
    cantidad,
    imgAntes,
    imgDespues
  } = req.body;

  await db.query(`
    INSERT INTO registros_retiro (
      ruta_activa_id,
      punto_id,
      fecha_hora,
      cantidad_retirada,
      ruta_img_antes,
      ruta_img_despues
    )
    VALUES ($1, $2, NOW(), $3, $4, $5)
  `, [
    ruta_activa_id,
    punto_id,
    cantidad,
    imgAntes,
    imgDespues
  ]);

  res.json({ ok: true });
});
