# Juego de mutantes por equipos (Java)

Implementación de la spec v6: dos equipos de mutantes, un hilo por mutante, tablero en cuadrícula
e interfaz gráfica en Swing.

## Requisitos
Java 17 o superior (JDK para compilar).

## Compilar y ejecutar

Linux / macOS:

    find src -name "*.java" > fuentes.txt
    javac -encoding UTF-8 -d out @fuentes.txt
    java -cp out mutantes.Main

Windows (cmd):

    dir /s /b src\*.java > fuentes.txt
    javac -encoding UTF-8 -d out @fuentes.txt
    java -cp out mutantes.Main

Opciones:

    java -cp out mutantes.Main --mutantes 8            tamaño inicial de los equipos (3 a 11, por defecto 6)
    java -cp out mutantes.Main --consola               juega sin ventana y escribe los eventos en la terminal
    java -cp out mutantes.Main --consola --verbose     incluye también los movimientos
    java -Dmutantes.tickMs=60 -cp out mutantes.Main    acelera o frena la partida (por defecto 150 ms por tick)

## Tamaño de los equipos y del tablero

- Cada equipo puede tener de 3 a 11 mutantes. En la ventana se elige con el selector "Mutantes por equipo"
  y se aplica al pulsar "Nueva partida"; también se puede fijar al arrancar con `--mutantes N`.
- El tablero es cuadrado y crece con el tamaño de los equipos: lado = mutantes por equipo + 8
  (11 x 11 con 3 mutantes, 14 x 14 con 6 y 19 x 19 con 11). Ajusta `MARGEN_TABLERO` en `Config.java` para cambiarlo.
- La ventana reduce el tamaño de las casillas en los tableros grandes para que quepan en pantalla.

## Estructura

    src/mutantes/
      Main.java, Config.java          punto de entrada y parámetros (tamaños, probabilidades, tick)
      modelo/                         Mutante (abstracta), Tanque, DPS, Equipo, Superpower, Pattern y sus patrones
      game/                           GameLayer (reglas), Battlefield, Casilla, Observer, GameEvent
      control/                        ControlLayer (arranca/detiene) y HiloMutante (un hilo por mutante)
      ui/                             UILayer (ventana Swing, observa a GameLayer) y PanelCampo (dibujo)

## Cómo se aplican las reglas

- Cada mutante corre en su propio hilo (`HiloMutante`). En cada tick decide al azar (85 % atacar, 15 % defender),
  actúa y se mueve según su patrón, también mientras defiende.
- Todas las acciones (mover, atacar, defender) pasan por `GameLayer` con el lock del tablero tomado. Así "casilla libre,
  ocúpala" y "enemigo a la par, golpea" son atómicos, y dos mutantes nunca comparten casilla.
- `Mutante.recibirGolpe` es atómico: solo el golpe que lleva la energía a 0 cuenta la baja.
- La UI no toca los hilos de juego: el Observer solo encola eventos y un `Timer` de Swing los consume.
  El tablero se dibuja desde una foto consistente (`Battlefield.instantanea()`).

## Decisiones que la spec dejaba abiertas (cámbialas en Config.java o en las clases indicadas)

- Los equipos alternan Tanque y DPS; el equipo A empieza a la izquierda y el B a la derecha.
- Patrones de movimiento incluidos: aleatorio, horizontal (recorre la fila y baja o sube una fila al llegar a la pared)
  y diagonal (rebota en las paredes). Con un 10 % de probabilidad dan un paso aleatorio para no quedar en ciclos.
- Si la casilla elegida está ocupada o fuera del borde, el mutante se queda quieto ese tick y el patrón cambia de sentido.

## Superpoderes

Los superpoderes solo cambian el texto: **no afectan el daño ni la defensa**. Cada mutante tiene uno y, cuando ataca,
el mensaje del superpoder es el que aparece en el log y en la ventana:

    A1 (DPS) lanza una Bola de fuego sobre B3 (Tanque): -3 de energía

Un superpoder es solo un nombre y una frase (`Superpower`). El catálogo está en
`src/mutantes/modelo/Superpoderes.java`. Para agregar uno nuevo, añade una línea:

    new Superpower("Rayo láser", "dispara un Rayo láser"),

- El nombre se ve al pasar el ratón sobre el mutante.
- La frase reemplaza a "ataca a" en el mensaje: "<atacante> <frase> sobre <objetivo>".

Reparto: al crear cada equipo se baraja el catálogo y se entrega en orden, así todos los mutantes reciben uno y no se
repite ninguno dentro del equipo. El catálogo trae 11 poderes, tantos como el tamaño máximo de un equipo; si lo dejas
con menos poderes que mutantes, se vuelve a empezar por el principio de la baraja.
