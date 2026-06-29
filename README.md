# 🃏 x50zo — Cincuentazo

> Juego de cartas en JavaFX donde jugadores humanos y máquinas compiten por sobrevivir sin que la suma de la mesa supere 50.

---

## 📖 Tabla de contenidos

- [Descripción](#descripción)
- [Reglas del juego](#reglas-del-juego)
- [Funcionalidades](#funcionalidades)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Estructuras de datos](#estructuras-de-datos)
- [Concurrencia](#concurrencia)
- [Manejo de excepciones](#manejo-de-excepciones)
- [Pruebas unitarias](#pruebas-unitarias)
- [Cómo ejecutar](#cómo-ejecutar)
- [Autores](#autores)

---

## Descripción

**Cincuentazo** (x50zo) es un juego de cartas tipo póker desarrollado como Mini Proyecto #3 para la asignatura *750014C Fundamentos de Programación Orientada a Eventos* de la Universidad del Valle.

El jugador humano compite contra 1, 2 o 3 jugadores máquina. Cada jugador tiene una mano de 4 cartas y debe jugar una por turno sin que la suma acumulada de la mesa supere 50. El jugador que no pueda jugar ninguna carta queda eliminado. Gana el último jugador en pie.

---

## Reglas del juego

- **Regla principal:** La suma acumulada de la mesa nunca debe superar 50.
- **Valores de las cartas:**
    - 2–8 y 10 → suman su valor numérico
    - 9 → suma 0 (ni suma ni resta)
    - J, Q, K → restan 10
    - A → suma 1 o 10, según convenga para no exceder 50
- **Cada turno:** Jugar una carta y tomar una del mazo para mantener siempre 4 cartas en la mano.
- **Eliminación:** El jugador sin carta jugable queda eliminado; sus cartas van al fondo del mazo.
- **Condición de victoria:** El último jugador en juego gana.
- **Mazo agotado:** Cuando el mazo se vacía, todas las cartas de la mesa excepto la última jugada se barajan y vuelven al mazo. La suma no cambia.

---

## Funcionalidades

- ✅ Selección de 1, 2 o 3 jugadores máquina desde el menú de inicio
- ✅ Cartas del humano boca arriba; cartas de la máquina boca abajo
- ✅ La máquina espera 1.4 segundos antes de jugar (hilo en segundo plano)
- ✅ Efectos de hover y clic con el ratón para jugar cartas
- ✅ Control por teclado: teclas 1–4 para seleccionar carta, Enter para jugar, Escape para deseleccionar
- ✅ Animación de carta jugada con transición de desvanecimiento y deslizamiento
- ✅ Contador de suma de mesa y contador de cartas del mazo en tiempo real
- ✅ Barajado automático del mazo cuando se agota
- ✅ Mensajes de victoria, derrota y eliminación con redirección automática al menú

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/trifasico/x50zo/
│   │   ├── Main.java
│   │   ├── controller/
│   │   │   ├── GameController.java       ← controlador principal del juego
│   │   │   ├── MenuController.java       ← controlador del menú de inicio
│   │   │   ├── MachinePlayThread.java    ← hilo para el turno de la máquina
│   │   │   └── MachineThinkingTask.java  ← Task JavaFX alternativo
│   │   ├── model/
│   │   │   ├── Card.java                 ← record inmutable de carta
│   │   │   ├── Rank.java                 ← enum de rangos con lógica de valor
│   │   │   ├── Deck.java                 ← mazo respaldado por LinkedList
│   │   │   ├── TablePile.java            ← pila de mesa respaldada por ArrayDeque
│   │   │   ├── TurnManager.java          ← coordinador central del juego
│   │   │   ├── GameState.java            ← snapshot inmutable del estado
│   │   │   ├── AppInitializer.java
│   │   │   ├── players/
│   │   │   │   ├── IPlayer.java          ← interfaz contrato del jugador
│   │   │   │   ├── PlayerAdapter.java    ← adaptadora abstracta (mano ArrayList)
│   │   │   │   ├── HumanPlayer.java      ← jugador humano
│   │   │   │   ├── MachinePlayer.java    ← jugador IA
│   │   │   │   └── MachineStrategy.java  ← estrategia greedy de selección
│   │   │   └── listeners/
│   │   │       ├── IGameEventListener.java ← interfaz de eventos del juego
│   │   │       └── GameEventAdapter.java   ← adaptadora con métodos por defecto
│   │   ├── exceptions/
│   │   │   ├── GameException.java
│   │   │   ├── EmptyDeckException.java
│   │   │   ├── InvalidPlayException.java
│   │   │   ├── GameRuleViolationException.java
│   │   │   ├── InvalidPlayerCountException.java
│   │   │   └── NoPlayableCardException.java
│   │   └── view/
│   │       ├── GameView.java             ← renderizado de UI y animaciones
│   │       └── SceneManager.java         ← singleton para cambio de escenas
│   └── resources/
│       ├── fxml/
│       │   ├── menu-view.fxml
│       │   └── game-view.fxml
│       ├── css/styles.css
│       ├── fonts/Lemon-Days.otf
│       └── sprites/                      ← 52 imágenes de cartas + back.png
└── test/
    └── java/com/trifasico/x50zo/model/
        ├── CardTest.java
        ├── DeckTest.java
        ├── TablePileTest.java
        └── TurnManagerTest.java
```

---

## Tecnologías

| Herramienta | Versión |
|-------------|---------|
| Java | SE 17 |
| JavaFX | 21.0.6 |
| Maven | 3.x |
| JUnit Jupiter | 5.12.1 |
| IntelliJ IDEA | — |
| Scene Builder | — |

---

## Arquitectura

El proyecto sigue estrictamente la arquitectura **MVC (Modelo-Vista-Controlador)**:

- **Modelo** — `Card`, `Deck`, `TablePile`, `TurnManager`, `players/`, `listeners/`, `exceptions/`. Sin importaciones de JavaFX en ninguna clase del modelo.
- **Vista** — `GameView.java` maneja todo el renderizado de UI, animaciones y actualizaciones de layout. `SceneManager` gestiona el cambio de escenas.
- **Controlador** — `GameController` escucha eventos del usuario (ratón, teclado) y delega toda la lógica al `TurnManager` mediante la interfaz `IGameEventListener`.

El controlador nunca muta el modelo directamente — llama métodos del `TurnManager` y reacciona a eventos devueltos a través del `GameEventAdapter`.

---

## Estructuras de datos

Se usan cuatro estructuras de datos distintas, cada una elegida por su patrón de acceso específico:

| Estructura | Dónde | Por qué |
|------------|-------|---------|
| `LinkedList<Card>` | `Deck` | O(1) agregar al fondo, O(1) sacar del frente — ideal para cola |
| `ArrayList<Card>` | `PlayerAdapter` | O(1) acceso aleatorio por índice — el humano selecciona carta por posición |
| `ArrayDeque<IPlayer>` | `TurnManager` | Cola circular de turnos — O(1) rotar jugador actual al fondo |
| `ArrayDeque<Card>` | `TablePile` | O(1) push al frente y peek del frente — acceso a la carta superior |

---

## Concurrencia

Dos hilos manejan la concurrencia del juego:

**`MachinePlayThread`** — un `Thread` daemon que duerme 1.4 segundos simulando que la máquina piensa, luego despacha la jugada al hilo de JavaFX mediante `Platform.runLater()`. Esto mantiene la UI responsiva mientras la máquina decide.

**`MachineThinkingTask`** — un `Task<Void>` de JavaFX que se integra con la API de concurrencia de JavaFX (`setOnFailed`, `setOnSucceeded`) para un control más fino del ciclo de vida del hilo.

Ambos hilos nunca tocan nodos de JavaFX directamente — todas las mutaciones de UI ocurren en el hilo de la aplicación vía `Platform.runLater`.

---

## Manejo de excepciones

Jerarquía propia de excepciones:

```
Exception (marcadas)
└── GameException                      ← clase base propia
    ├── EmptyDeckException             ← mazo y pila agotados
    ├── InvalidPlayException           ← carta excedería la suma de 50
    └── GameRuleViolationException     ← violación general de reglas

RuntimeException (no marcadas)
├── InvalidPlayerCountException        ← cantidad de máquinas fuera de [1, 3]
└── NoPlayableCardException            ← jugador sin carta jugable → eliminación
```

Las excepciones marcadas se usan cuando el llamador debe recuperarse explícitamente (mostrar feedback, reciclar mazo). Las no marcadas señalan condiciones de flujo del juego que se propagan naturalmente sin que todos los métodos las declaren.

---

## Pruebas unitarias

4 clases de prueba, 74 pruebas, todas pasando ✅

| Clase | Qué prueba |
|-------|-----------|
| `CardTest` | Valores de cada rango, `isPlayable`, lógica del As |
| `DeckTest` | Inicialización, `draw`, `addToBottom`, `reshuffleFromPile` |
| `TablePileTest` | `push`, `peek`, suma acumulada, `collectForReshuffle` |
| `TurnManagerTest` | Setup, flujo de turnos, eliminación, condición de victoria |

Ejecutar todas las pruebas:
```bash
mvn test
```

---

## Cómo ejecutar

**Requisitos:** Java 17+, Maven 3.x

```bash
# Clonar el repositorio
git clone https://github.com/trifasico/x50zo.git
cd x50zo

# Ejecutar la aplicación
mvn clean javafx:run
```

---

## Autores

- **Yostin Ramirez - 2519674**
- **Lesly Zapata - 2516574**
- **Joseph Terreros - 2521011**

Universidad del Valle — 750014C Fundamentos de Programación Orientada a Eventos, 2026