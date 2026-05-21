# Pagos de administración – Modo prueba y Mercado Pago

## Por qué falla «Pago en línea»

El botón de la app llama al backend:

`POST https://conjuntoback.onrender.com/api/pagos/checkout/administracion`

Body:

```json
{
  "monto": 150000,
  "concepto": "Pago administracion",
  "periodo": "2026-05"
}
```

El servidor debe:

1. Leer el usuario del **JWT** (`Authorization: Bearer ...`).
2. **Guardar** el pago en la base de datos (estado pendiente).
3. Crear una **preferencia** en Mercado Pago (credenciales TEST).
4. Responder con `initPoint` o `sandbox_init_point` o `checkoutUrl`.

Si falla el paso 2, aparece el mensaje:

> No se puede registrar el pago en la base de datos...

Eso **no se arregla solo en la app Android**; hay que revisar el backend y la base de datos.

---

## Qué información necesitamos para diagnosticar

Copia y envía esto (puede ser captura de Logcat):

| Dato | Cómo obtenerlo |
|------|----------------|
| Filtro Logcat `PagosCheckout` | Línea `Request body: {...}` |
| Código HTTP | Línea `Response HTTP 500` (o 400, 409) |
| Cuerpo del error | Texto JSON después de `body=` |
| Usuario de prueba | Usuario con el que inicias sesión (residente) |
| ¿Tiene torre/apartamento? | En perfil del residente |
| Lista «Mis pagos registrados» | Periodo y estado en pantalla Pagos |

Del **backend** (Render / quien administre el servidor):

| Variable / dato | Para qué sirve |
|-----------------|----------------|
| `MERCADOPAGO_ACCESS_TOKEN` **de prueba** | Crear checkout en sandbox |
| `MERCADOPAGO_PUBLIC_KEY` test | Integración MP |
| URL de retorno | `https://conjuntoback.onrender.com/pagos/resultado?ref={ref}` |
| Logs del servidor al hacer POST checkout | Error SQL o MP real |
| ¿Existe pago duplicado mismo periodo? | Restricción única en BD |

---

## Flujo real con Mercado Pago TEST (dinero ficticio)

1. En [Mercado Pago Developers](https://www.mercadopago.com.co/developers) crear aplicación **Test**.
2. Copiar **Access Token TEST** y **Public Key TEST** al backend en Render.
3. En el backend, usar modo **sandbox** al crear la preferencia.
4. En la app: **Pago en línea** → debe abrir URL `sandbox.mercadopago.com.co`.
5. Pagar con [tarjetas de prueba MP](https://www.mercadopago.com.co/developers/es/docs/checkout-api/integration-test/test-cards):
   - Aprobada: `5031 7557 3453 0604` (Visa) + CVV 123 + vencimiento futuro.
6. Al terminar, MP redirige a `/pagos/resultado?ref=...` y la app llama `POST /api/pagos/confirmar`.

---

## Simulación en la app (sin Mercado Pago)

En **Pagos** → sección **«Simular pago en línea»**:

- No abre pasarela.
- Muestra el mismo resultado visual (aprobado / rechazado / pendiente).
- Genera referencia `SIM-ADMIN-{usuario}-{periodo}-{timestamp}`.
- Intenta `POST /api/pagos/confirmar`; si el backend no la reconoce, igual se ve el resultado en la app.

Sirve para **demostrar el flujo en clase** aunque el servidor falle al registrar el checkout.

---

## Checklist backend (quien tenga el código del servidor)

- [ ] JWT del residente incluye `id` de usuario existente en tabla `usuario`.
- [ ] Residente tiene `torre` y `apartamento` si la entidad `Pago` los exige.
- [ ] No hay otro pago **PENDIENTE** del mismo `periodo` bloqueando un nuevo INSERT.
- [ ] Credenciales Mercado Pago TEST configuradas en Render.
- [ ] Respuesta JSON incluye `sandbox_init_point` en modo prueba.
- [ ] Webhook `/api/pagos/mercadopago/webhook` configurado (opcional para auto-confirmar).

---

## Contacto técnico

Si compartes el log `PagosCheckout` completo de un intento fallido, se puede indicar si el problema es BD, JWT, duplicado de periodo o Mercado Pago sin configurar.
