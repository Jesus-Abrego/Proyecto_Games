# Proyecto_Games

Este proyecto fue desarrollado en colaboración por:  
**Jesus Abrego, Carlos Dominguez, Osvaldo Jahir y Sebastian Zarate**

---

## Descripción

**Proyecto_Games** es una aplicación Android desarrollada en **Kotlin** que reúne varios juegos clásicos en una sola plataforma, implementando buenas prácticas de desarrollo y un enfoque modular en cada juego. El objetivo es ofrecer entretenimiento y ejemplos didácticos de desarrollo móvil moderno.

## Juegos Incluidos

- Brick Breaker
- Snake
- Solitario
- Blackjack
- Sudoku
- Ruleta
- (y otros que puedas agregar)

## Características principales

- Interfaz intuitiva y amigable
- Múltiples juegos clásicos integrados
- Persistencia de datos para puntajes y sesiones
- Sistema de autenticación local (login demo)
- Uso extensivo de componentes y librerías modernas de Android
- Código organizado y fácil de mantener

## Requisitos técnicos

- **Lenguaje principal:** Kotlin (100%)
- **Versión de Gradle:** 8.13 ([wrapper](https://github.com/Jesus-Abrego/Proyecto_Games/blob/main/gradle/wrapper/gradle-wrapper.properties))
- **Android Gradle Plugin (AGP):** 8.13.0
- **Versión de Kotlin:** 2.0.21

> **Nota:** Las versiones de SDK (`minSdkVersion`, `targetSdkVersion`, `compileSdkVersion`) no fueron encontradas directamente en los resultados, por favor verifica el archivo `build.gradle` de tu módulo `app` para completarlas.

## Dependencias principales

- `androidx.core:core-ktx:1.17.0`
- `androidx.appcompat:appcompat:1.7.1`
- `com.google.android.material:material:1.13.0`
- `androidx.activity:activity:1.11.0`
- `androidx.constraintlayout:constraintlayout:2.2.1`
- **Testing:**  
  - `junit:junit:4.13.2`
  - `androidx.test.ext:junit:1.3.0`
  - `androidx.test.espresso:espresso-core:3.7.0`

El detalle completo de versiones y dependencias se encuentra en [`gradle/libs.versions.toml`](https://github.com/Jesus-Abrego/Proyecto_Games/blob/main/gradle/libs.versions.toml).

## Instalación y ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/Jesus-Abrego/Proyecto_Games.git
   ```
2. Abre el proyecto en **Android Studio Hedgehog** (o superior).
3. Permite que el IDE descargue y sincronice todas las dependencias.
4. (Opcional) Ajusta las versiones del SDK en el archivo `build.gradle` según tu entorno.
5. Ejecuta el proyecto en un emulador o dispositivo real.

## Créditos

Desarrollado por:
- **Jesus Abrego**
- **Carlos Dominguez**
- **Osvaldo Jahir**
- **Sebastian Zarate**

## Licencia

Este proyecto está bajo la licencia [MIT](LICENSE) (o la que decidan los autores).

---

¿Tienes sugerencias o encontraste algún problema?  
¡Abre un issue o contáctanos!