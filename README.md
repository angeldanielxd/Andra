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
| Estudiante | `2024207009` o DNI `72458913` | `andra2026` |
| Administrador | `rmamani@institucion.edu` | `admin2026` |

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
jpackage --name Andra --input target --main-jar andra-javafx-1.0.0.jar \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --icon src/main/resources/img/logo-andra.png --type dmg
```

Windows:

```bat
jpackage --name Andra --input target --main-jar andra-javafx-1.0.0.jar ^
  --main-class org.springframework.boot.loader.launch.JarLauncher --type msi --win-shortcut --win-menu
```

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
