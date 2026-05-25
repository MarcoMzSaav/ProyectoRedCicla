const express = require('express');
const app = express();

app.use(express.json());

// rutas
app.use('/mobile', require('./routes/mobile'));

app.listen(3000, () => {
  console.log('Servidor corriendo en http://localhost:3000');
});


app.use('/admin', require('./routes/admin'));
