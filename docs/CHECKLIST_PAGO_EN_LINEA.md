# Checklist: pago en línea TEST + admin «al día»

## Causa real (confirmada con logs Render)

```
ERROR: violates check constraint "pago_administracion_metodo_pago_check"
Detail: Failing row contains (..., EN_LINEA, ..., sandbox.mercadopago.com.co/...)
```

- **Mercado Pago TEST sí funciona** (el token `TEST-7403...` en Render es correcto; ya se generó la URL sandbox).
- **Falla el INSERT** porque el backend guarda `metodo_pago = EN_LINEA` y PostgreSQL solo permite otros valores (p. ej. `MERCADO_PAGO`, `EFECTIVO`).

**Arreglo en ConjuntoBack (Java):** al crear checkout en línea, usar `metodoPago = "MERCADO_PAGO"` en lugar de `EN_LINEA`.

La app ahora envía también `"metodoPago": "MERCADO_PAGO"` en el JSON por si el backend lo lee del body.

---

## 1. Qué revisar en Render (variables Mercado Pago TEST)

En el servicio **conjuntoback** → **Environment**:

| Variable | Qué debe ser |
|----------|----------------|
| `MERCADOPAGO_ACCESS_TOKEN` | Token que empieza por **`TEST-`** (credencial de prueba, no producción) |
| `MERCADOPAGO_PUBLIC_KEY` | Public key de prueba (`TEST-...`) |
| Opcional | `MERCADOPAGO_WEBHOOK_SECRET`, URL de notificaciones |

Cómo obtenerlas:

1. [developers.mercadopago.com.co](https://www.mercadopago.com.co/developers) → Tu aplicación → **Credenciales de prueba**.
2. Copiar **Access Token** y **Public Key** de la sección **Test**.
3. Pegar en Render y **redeploy** del backend.

Si el token es de **producción** (`APP_USR-...` sin entorno test) o está vacío, el checkout puede fallar después de arreglar la BD.

La app Android **no** guarda el Access Token; solo el servidor lo usa.

---

## 2. Qué revisar en la base de datos (causa del error actual)

En los **logs de Render** al hacer clic en «Pago en línea», busca el stack trace SQL. Causas típicas:

### A) Pago duplicado del mismo mes

Si ya existe un registro `PENDIENTE` para el residente y periodo `2026-05`, un `INSERT` nuevo falla (restricción única).

**Qué hacer:**

- En la BD, borrar pagos `PENDIENTE` viejos de prueba de ese residente, **o**
- En el backend: si ya existe pago del periodo, devolver el `checkoutUrl` existente en lugar de insertar otro.

### B) Usuario del JWT no existe o sin torre/apartamento

El backend obtiene el usuario del token. Si `id` no existe en tabla `usuario`, o faltan columnas obligatorias (`torre`, `apartamento`), falla el `INSERT`.

**Qué hacer:**

- Iniciar sesión con un residente que en BD tenga **torre y apartamento**.
- En la app ya se valida esto antes de llamar al checkout.

### C) Campos obligatorios en tabla `pago` / `pago_administracion`

Al crear el pago, el backend debe rellenar al menos:

| Campo | Ejemplo |
|-------|---------|
| `monto` | `150000` |
| `concepto` | `Pago administracion` |
| `periodo` | `2026-05` |
| `estado_pago` | `PENDIENTE` |
| `metodo_pago` | **`MERCADO_PAGO`** (no `EN_LINEA`; la BD lo rechaza) |
| `usuario_id` | id del JWT |
| `referencia_externa` | UUID único antes de ir a MP |

Si algún `NOT NULL` queda vacío, aparece el error de «registrar en base de datos».

---

## 3. Flujo correcto (modo prueba)

```text
1. Residente → Pago en línea
2. POST /api/pagos/checkout/administracion  (JWT + body monto/periodo)
3. Backend: INSERT pago PENDIENTE + crear preferencia MP TEST
4. Respuesta: sandbox_init_point o init_point
5. App abre sandbox.mercadopago.com.co
6. Tarjeta prueba APRO → pago aprobado
7. Redirección: .../pagos/resultado?ref=REFERENCIA
8. App: POST /api/pagos/confirmar { "referenciaExterna": "ref" }
9. Backend: estado_pago = APROBADO, recalcular administración AL_DIA
10. Admin → Pagos → Actualizar → residente en «Usuarios al día»
```

---

## 4. Por qué el admin muestra «en mora»

La pantalla admin usa `GET /api/pagos/admin/estado-pagos`.

Un residente aparece **al día** si el JSON trae alguno de estos:

- `"estaAlDia": true` o `"alDia": true`
- `"estadoAdministracion": "AL_DIA"` o `"PAGADO_EN_MORA"`
- `"pagoPeriodoActual": { "estadoPago": "APROBADO" }` (la app también lo interpreta)

Si pagó en MP pero el backend **no actualizó** el estado al confirmar (`/api/pagos/confirmar`) o no recalcula en `estado-pagos`, seguirá en mora.

**Revisar en backend:**

- `confirmarPago` debe buscar el pago por `referenciaExterna` y poner `estadoPago = APROBADO`.
- `listarEstadoPagosAdmin` debe marcar `estaAlDia` / `estadoAdministracion` según pago del periodo actual.

---

## 5. Prueba rápida con Swagger

URL: `https://conjuntoback.onrender.com/swagger-ui/index.html`

1. `POST /api/usuarios/login` → copiar `token`.
2. Authorize con `Bearer {token}`.
3. `POST /api/pagos/checkout/administracion` con body:
   ```json
   { "monto": 150000, "concepto": "Pago administracion", "periodo": "2026-05" }
   ```
4. Si responde **500** con mensaje de BD → el problema es servidor/BD (no la app).
5. Si responde **200** con `sandbox_init_point` → la app debería abrir MP correctamente.

---

## 6. Tarjeta de prueba (Colombia)

- Número: `5031 7557 3453 0604`
- CVV: `123`
- Vencimiento: cualquier fecha futura
- Titular: `APRO`

---

## 7. Qué enviar para soporte técnico

1. Captura de Logcat filtro **`PagosCheckout`** (request + HTTP code + body).
2. Usuario residente (login) y si tiene torre/apartamento.
3. Logs de Render del mismo momento (error SQL completo).
4. Confirmación: ¿`MERCADOPAGO_ACCESS_TOKEN` en Render empieza por `TEST-`?

Sin el log SQL del backend no se puede corregir solo desde la app Android.
