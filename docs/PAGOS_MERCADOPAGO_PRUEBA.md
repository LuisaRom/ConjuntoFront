# Pago en línea – Mercado Pago TEST

La app **no guarda credenciales** de Mercado Pago. Solo llama al backend; el servidor usa las variables TEST (`ACCESS_TOKEN`, etc.) y devuelve la URL de checkout.

## Flujo al pulsar «Pago en línea»

1. `POST /api/pagos/checkout/administracion` con JWT y body:
   ```json
   { "monto": 150000, "concepto": "Pago administracion", "periodo": "2026-05" }
   ```
2. Si falla, prueba rutas: `mercado-pago/crear`, `mercado-pago`, `mercadopago`, etc.
3. La app abre **`sandbox.mercadopago.com.co`** (modo prueba).
4. Tras pagar, MP redirige a `/pagos/resultado?ref=...` y la app confirma con `POST /api/pagos/confirmar`.

## Tarjetas de prueba (Colombia)

| Resultado | Visa | CVV | Vencimiento |
|-----------|------|-----|-------------|
| Aprobado | 5031 7557 3453 0604 | 123 | Futuro |
| Rechazado | 4000 0000 0000 0002 | 123 | Futuro |

Titular de prueba: `APRO` (aprobado) o `OTHE` (rechazado).

## Variables en Render (backend)

- `MERCADOPAGO_ACCESS_TOKEN` → token **TEST** (no producción)
- URL de retorno: `https://conjuntoback.onrender.com/pagos/resultado`

## Si sigue el error de base de datos

El backend no está creando la preferencia MP; revisar logs en Render al hacer POST checkout. La app reintenta otras rutas MP automáticamente.
