# Andra — tus trámites más simples

Aplicación de escritorio de trámites para instituciones educativas, hecha en **Java 21 + JavaFX 21 + Spring Boot 3 + SQLite** con la misma arquitectura que `andra-spring`.

Equipo: Soto Macedo Daniel Angel · Mendoza Mamani Jair Edison · Puma Saldivar Brayan Nestor · Saldaña Valeriano Carlos Josue.

## Requisitos

- JDK 21 o superior (probado con JDK 26).
- Maven 3.9 (o IntelliJ IDEA, que ya lo trae).

## Ejecutar

```bash
mvn javafx:run
```

En IntelliJ: abre la carpeta `andra-javafx` y ejecuta `pe.andra.Lanzador`.

La base `andra.db` (SQLite) se crea sola en la carpeta del proyecto y se siembra desde `datos/catalogo.json` y `datos/datos-demo.json`.

Cuentas de demostración:

| Rol | Usuario | Contraseña |
|---|---|---|
| Estudiante | `20240157` o DNI `72458913` | `andra2026` |
| Administrador | `rmartinez@institucion.edu` | `admin2026` |

## Pruebas

```bash
mvn test
mvn test -Dtest=CapturasTest -Dcapturas=true
```

La segunda guarda capturas de todas las pantallas en `target/capturas` (opcional `-Drutas=inicio,pay`).

## Empaquetado con jpackage

```bash
mvn clean package -DskipTests
```

macOS:

```bash
jpackage --name Andra --input target --main-jar andra-javafx-1.1.0.jar \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --icon src/main/resources/img/logo-andra.png --type dmg
```

Windows (se ejecuta en Windows, con el JDK 21 o superior):

1. Compila en Windows con `mvn -DskipTests package` (Maven elige solo las librerías de JavaFX para Windows). Desde macOS también se puede, con `mvn -DskipTests -Djavafx.platform=win package`.
2. Haz doble clic en `empaquetado\windows\crear-exe.bat`.

El script deja en `empaquetado\windows\salida\`:
- `Andra\Andra.exe`: la versión portable.
- `Andra-1.1.0-windows-portable.zip`: la misma versión, comprimida.
- `Andra-1.1.0.exe`: un instalador, solo si está instalado WiX Toolset 3.14.

Los pasos completos están en `empaquetado/windows/LEEME.txt`. Si cambias la versión en `pom.xml`, cámbiala también en `crear-exe.bat`.

## Estructura

```
src/main/java/pe/andra/
├── AndraApplication · AplicacionFx · Lanzador
├── config/       DatosInstitucion, JsonRecursos
├── controller/   MainController (cabecera, navegación por rol, popovers) y una vista por sección
├── exception/    NegocioException
├── model/        entidades JPA y enums
├── repository/   Spring Data JPA (SQLite)
├── seeder/       CatalogoSeeder, EstadoDemoSeeder, SembradorDemo, DatabaseSeeder
├── service/      reglas de negocio (pagos, carné, seguimiento, ocupación, análisis, idiomas…)
├── ui/           componentes (VidrioDasm, TarjetaGaleria, Filtros, VistaFactura, RastreadorCjsv…)
└── util/         fechas, días hábiles, validación, TarjetaJemm (Luhn), montos
src/main/resources/
├── css/theme.css · view/*.fxml · img/ · fuentes/ · datos/*.json · i18n/traducciones.json
```
