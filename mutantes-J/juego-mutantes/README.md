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

## Tamaño de los equipos y del tablero

- Cada equipo puede tener de 3 a 11 mutantes. En la ventana se elige con el selector "Mutantes por equipo"
  y se aplica al pulsar "Nueva partida"
- El tablero es cuadrado y crece con el tamaño de los equipos: lado = mutantes por equipo + 8
  (11 x 11 con 3 mutantes, 14 x 14 con 6 y 19 x 19 con 11)
- La ventana reduce el tamaño de las casillas en los tableros grandes para que quepan en pantalla.


## Cómo se aplican las reglas

- Cada mutante corre en su propio hilo (`HiloMutante`). En cada tick decide al azar (85 % atacar, 15 % defender), actúa y se mueve según su patrón, también mientras defiende.
- Todas las acciones (mover, atacar, defender) pasan por `GameLayer` con el lock del tablero tomado. Así si hay una casilla libre se mueven a ella y si hay un enemigo a la par golpea, dos mutantes nunca comparten casilla.
- La UI no toca los hilos de juego.


## Superpoderes

Los superpoderes solo cambian el texto: **no afectan el daño ni la defensa**. Cada mutante tiene uno y, cuando ataca, el mensaje del superpoder es el que aparece en el log y en la ventana. EJEMPLO:

    A1 (DPS) lanza una Bola de fuego sobre B3 (Tanque): -3 de energía

Un superpoder es solo un nombre y una frase (`Superpower`). La lista de superpoderes está en
`mutantes-J/modelo/Superpoderes.java`. Para agregar uno nuevo:

    new Superpower("TK(cosa)"-"hace algo"),

- El nombre se ve al pasar el ratón sobre el mutante.

Reparto: al crear cada equipo se baraja el catálogo y se entrega en orden, así todos los mutantes reciben uno y no se repite ninguno dentro del equipo. Hay un total de 11 poderes, uno por cada mutante (si hay 11 mutantes en el campo)