package com.example.app.Model

data class SeccionTerminos(val titulo: String, val parrafos: List<String>)

data class EstadoTerminos(
    val versionVigente: String? = null,
    val aceptado: Boolean? = null,
    val fechaAceptacion: String? = null
)

data class AceptarTerminosRequest(
    val version: String,
    val aceptaTerminos: Boolean,
    val autorizaTratamientoDatos: Boolean,
    val aceptaAvisoIA: Boolean,
    val dispositivo: String?
)

object TerminosCondiciones {

    const val VERSION = "1.0"
    const val VIGENCIA = "27 de noviembre de 2026"
    const val CONJUNTO = "Conjunto Residencial Hacienda San Rafael"

    const val AVISO_IA_CORTO = "El asistente responde solo con la información a la que tiene acceso tu rol y es de uso exclusivo para asuntos del $CONJUNTO. Sus respuestas son orientativas."

    val AVISO_IA: List<String> = listOf(
        "Las respuestas del asistente se basan únicamente en la información a la que tiene acceso tu rol: el residente consulta solo sus propios datos, el celador la información operativa y el administrador la información general del conjunto.",
        "Su uso es exclusivo para asuntos del $CONJUNTO: pagos, paquetería, reservas, quejas, accesos, mascotas, vehículos, parqueaderos y normas de convivencia. No lo utilices para temas ajenos al conjunto.",
        "Las respuestas se generan de forma automática, son orientativas y pueden contener errores. No reemplazan las decisiones de la administración, el reglamento de propiedad horizontal ni las decisiones de la asamblea.",
        "No escribas datos sensibles (salud, creencias, datos financieros) ni información de terceros que no sea necesaria. Tus conversaciones quedan en tu historial y puedes eliminarlas cuando quieras."
    )

    fun rolNormalizado(rol: String?): String {
        val r = rol.orEmpty().uppercase()
        return when {
            r.contains("ADMIN") -> "ADMINISTRADOR"
            r.contains("CELADOR") -> "CELADOR"
            else -> "RESIDENTE"
        }
    }

    fun nombreRol(rol: String?): String = when (rolNormalizado(rol)) {
        "ADMINISTRADOR" -> "Administrador"
        "CELADOR" -> "Celador"
        else -> "Residente"
    }

    fun secciones(rol: String?): List<SeccionTerminos> = listOf(
        SeccionTerminos(
            "1. Objeto y aceptación",
            listOf(
                "Estos términos regulan el uso de la aplicación móvil de gestión y control del $CONJUNTO, ubicado en Zipaquirá, Cundinamarca, que centraliza los procesos administrativos, operativos y de comunicación del conjunto en el marco del régimen de propiedad horizontal (Ley 675 de 2001).",
                "Al marcar las casillas y pulsar \"Aceptar y continuar\", declaras que leíste, entendiste y aceptas estos términos. Esta aceptación electrónica tiene la misma validez que una firma, conforme a la Ley 527 de 1999. Si no los aceptas, no podrás usar la aplicación."
            )
        ),
        SeccionTerminos(
            "2. Usuarios, roles y cuentas",
            listOf(
                "La aplicación tiene tres roles: administrador, celador y residente. Cada rol accede solo a los módulos y a la información que le corresponden según sus responsabilidades dentro del conjunto.",
                "Las cuentas son creadas por la administración. Tu usuario y contraseña son personales e intransferibles. Usa una contraseña de mínimo 8 caracteres, no la compartas, cierra sesión en dispositivos compartidos e informa de inmediato a la administración cualquier uso no autorizado de tu cuenta.",
                "Debes registrar información veraz y mantenerla actualizada. La administración puede suspender o desactivar cuentas que incumplan estos términos o el reglamento del conjunto."
            )
        ),
        seccionUsoAdecuado(rol),
        SeccionTerminos(
            "4. Conductas no permitidas",
            listOf(
                "Suplantar a otra persona o usar la cuenta de otro usuario.",
                "Intentar acceder a información o funciones de otros roles, vulnerar la seguridad de la plataforma o alterar su funcionamiento.",
                "Publicar o enviar contenido ofensivo, discriminatorio, amenazante, falso o contrario a la ley y a la convivencia.",
                "Usar la aplicación o la información que contiene con fines comerciales, políticos o ajenos a la gestión del conjunto, o compartir con terceros datos de residentes, visitantes o funcionarios."
            )
        ),
        SeccionTerminos(
            "5. Tratamiento de datos personales",
            listOf(
                "Responsable: el $CONJUNTO - Propiedad Horizontal, a través de su administración, trata tus datos conforme a la Ley 1581 de 2012 y al Decreto 1377 de 2013 (compilado en el Decreto 1074 de 2015). Puedes contactar al responsable en la oficina de administración del conjunto o mediante la sección de mensajes de la aplicación.",
                "Datos que se tratan: nombre, documento, usuario, correo, teléfono, torre y apartamento; registros de pagos de administración, reservas, quejas, paquetería, accesos y visitantes autorizados, mascotas, vehículos y parqueaderos; mensajes, notificaciones y consultas al asistente de inteligencia artificial.",
                "Finalidades: gestionar la administración y la convivencia del conjunto; controlar accesos y seguridad; comunicar novedades y notificaciones; registrar y verificar pagos; atender quejas y solicitudes; gestionar reservas, mascotas, vehículos y la asignación de parqueaderos; y responder las consultas del asistente de inteligencia artificial.",
                "La aplicación no solicita datos sensibles ni está dirigida a menores de edad. Cuando registras datos de terceros, como visitantes, declaras contar con su autorización y entregar solo lo necesario.",
                "Tus derechos como titular (artículo 8 de la Ley 1581 de 2012): conocer, actualizar y rectificar tus datos; solicitar prueba de esta autorización; ser informado sobre el uso dado a tus datos; presentar quejas ante la Superintendencia de Industria y Comercio; revocar la autorización o solicitar la supresión cuando no exista un deber legal o contractual de conservarlos; y acceder gratuitamente a tus datos.",
                "Las consultas se atienden en un máximo de 10 días hábiles y los reclamos en un máximo de 15 días hábiles, según los artículos 14 y 15 de la Ley 1581 de 2012.",
                "Seguridad: las contraseñas se guardan cifradas, el acceso se controla con sesión segura y permisos por rol, y cada usuario solo ve la información que le corresponde. Los proveedores de alojamiento, de pagos en línea y, cuando está habilitado, de inteligencia artificial, actúan como encargados y reciben solo la información necesaria para prestar su servicio.",
                "Tus datos se conservan mientras exista tu relación con el conjunto y durante los plazos legales aplicables. Al marcar la casilla de autorización, otorgas tu autorización previa, expresa e informada para este tratamiento."
            )
        ),
        SeccionTerminos("6. Asistente de inteligencia artificial", AVISO_IA + listOf(
            "Este aviso sigue los lineamientos de la Superintendencia de Industria y Comercio sobre el tratamiento de datos personales en sistemas de inteligencia artificial (Circular Externa 002 de 2024)."
        )),
        SeccionTerminos(
            "7. Propiedad intelectual",
            listOf(
                "La aplicación, su código fuente, diseño y contenidos son obras protegidas por la Ley 23 de 1982 sobre derechos de autor, desarrolladas por Luisa Fernanda Romero Suarez y Omar Santiago Rodríguez Puerto. No está permitido copiarlas, modificarlas, distribuirlas ni usarlas con fines distintos a la gestión del conjunto."
            )
        ),
        SeccionTerminos(
            "8. Disponibilidad y responsabilidad",
            listOf(
                "La aplicación es una herramienta de apoyo para la gestión del conjunto. Su disponibilidad puede verse afectada por mantenimientos, fallas de conexión o del dispositivo. Las decisiones administrativas se rigen por el reglamento de propiedad horizontal, el manual de convivencia y las decisiones de la asamblea y del consejo de administración.",
                "Cada usuario responde por el uso que haga de su cuenta y por la veracidad de la información que registra."
            )
        ),
        SeccionTerminos(
            "9. Cambios a estos términos",
            listOf(
                "La administración puede actualizar estos términos. Cuando cambien, la aplicación te mostrará la nueva versión y deberás aceptarla para seguir usándola. Versión $VERSION, vigente desde el $VIGENCIA. Estos términos se rigen por las leyes de la República de Colombia."
            )
        )
    )

    private fun seccionUsoAdecuado(rol: String?): SeccionTerminos = when (rolNormalizado(rol)) {
        "ADMINISTRADOR" -> SeccionTerminos(
            "3. Uso adecuado de la plataforma: Administrador",
            listOf(
                "Crea y gestiona las cuentas de usuarios con datos verídicos y asigna a cada persona el rol que le corresponde.",
                "Publica novedades, comunicados y notificaciones con información oficial del conjunto, en lenguaje respetuoso.",
                "Registra y valida los pagos de administración con base en soportes reales y atiende las quejas de forma oportuna e imparcial.",
                "Gestiona reservas, mascotas, vehículos y la asignación de parqueaderos conforme al reglamento, sin asignar un mismo parqueadero a dos vehículos.",
                "En Gestión IA, aprueba como conocimiento solo información general del conjunto, nunca datos personales.",
                "Usa la información de residentes, celadores y visitantes solo para la gestión del conjunto y no la exportes ni la compartas con terceros no autorizados."
            )
        )
        "CELADOR" -> SeccionTerminos(
            "3. Uso adecuado de la plataforma: Celador",
            listOf(
                "Registra los accesos peatonales y vehiculares con datos reales, valida los códigos QR de los visitantes autorizados y registra la hora de entrada y salida.",
                "Registra la paquetería recibida y entrégala únicamente al residente destinatario o a quien él autorice.",
                "Consulta los datos de residentes y vehículos solo para el control de ingreso y la operación del conjunto.",
                "Usa las notificaciones y los mensajes solo para asuntos operativos y de seguridad del conjunto.",
                "No compartas datos de residentes, como nombres, apartamentos, teléfonos o placas, con visitantes ni con terceros."
            )
        )
        else -> SeccionTerminos(
            "3. Uso adecuado de la plataforma: Residente",
            listOf(
                "Mantén actualizados los datos de tu perfil y registra solo tus propios vehículos y mascotas.",
                "Autoriza únicamente a visitantes reales. El código QR generado es para el visitante autorizado y no debe usarse con otro fin.",
                "Realiza reservas de zonas comunes respetando los horarios y las reglas del conjunto, y usa los espacios de forma responsable.",
                "Presenta quejas y publicaciones de forma veraz y respetuosa, sin exponer datos personales de otros residentes.",
                "Realiza los pagos de administración por los medios habilitados en la aplicación o en la administración."
            )
        )
    }
}
