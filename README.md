# Dólar BO

App Android que consulta el tipo de cambio del dólar en Bolivia (oficial BCB y Binance P2P)
desde https://bo.dolarapi.com y guarda el histórico en una BD SQLite local (`dolar.db`).

- Consulta al abrir (o volver a) la app, y con el botón "Actualizar".
- Consulta automática diaria a las 19:00 (WorkManager, con reintentos si no hay internet).
- Solo guarda un registro nuevo cuando el valor cambió, para mantener la BD pequeña.

## Opción A: compilar en GitHub (sin instalar nada)
1. Crea un repositorio en GitHub y sube esta carpeta (incluida la carpeta oculta `.github`).
2. Ve a **Actions → Build APK** (se ejecuta con cada push, o con "Run workflow").
3. Al terminar, descarga el artefacto **DolarBO-apk** → `app-debug.apk`.

## Opción B: Android Studio
1. File → Open → selecciona esta carpeta. Acepta crear/usar el Gradle wrapper si lo pide.
2. Build → Build APK(s). El APK queda en `app/build/outputs/apk/debug/`.

## Instalar
Pasa el APK al teléfono y ábrelo; Android pedirá permitir "instalar apps de origen desconocido".

## Tabla
    historial(id, tipo, compra, venta, fecha_api, fecha_consulta, origen)  -- origen: 'app' | 'auto'
# dolarbo
# dolarbo
