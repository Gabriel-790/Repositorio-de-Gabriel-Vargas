package mutantes;

/** Parámetros ajustables de la partida. */
public final class Config {
    private Config() {}

    /** Límites del tamaño de cada equipo. */
    public static final int MIN_MUTANTES_POR_EQUIPO = 3;
    public static final int MAX_MUTANTES_POR_EQUIPO = 11;
    public static final int MUTANTES_POR_EQUIPO_DEFECTO = 6;

    /** El tablero es cuadrado y crece con el tamaño de los equipos: lado = mutantes por equipo + MARGEN_TABLERO. */
    public static final int MARGEN_TABLERO = 8;

    public static final String SIMBOLO_EQUIPO_1 = "A";
    public static final String SIMBOLO_EQUIPO_2 = "B";

    /** Duración base del tick de cada mutante en ms (se puede cambiar con -Dmutantes.tickMs=NNN). */
    public static final long TICK_MS = Long.getLong("mutantes.tickMs", 150L);

    /** Probabilidad de decidir atacar en cada tick; el resto de las veces (15 %) defiende. */
    public static final double PROB_ATACAR = 0.85;

    /** Probabilidad de que un patrón de movimiento dé un paso aleatorio (evita recorridos cíclicos). */
    public static final double PROB_PASO_ALEATORIO = 0.10;

    /**
     * Comprueba que el tamaño del equipo esté entre el mínimo y el máximo.
     *
     * @return el mismo valor, si es válido
     * @throws IllegalArgumentException si está fuera de rango
     */
    public static int validarMutantesPorEquipo(int mutantesPorEquipo) {
        if (mutantesPorEquipo < MIN_MUTANTES_POR_EQUIPO || mutantesPorEquipo > MAX_MUTANTES_POR_EQUIPO) {
            throw new IllegalArgumentException("Cada equipo debe tener entre " + MIN_MUTANTES_POR_EQUIPO
                    + " y " + MAX_MUTANTES_POR_EQUIPO + " mutantes (recibido: " + mutantesPorEquipo + ")");
        }
        return mutantesPorEquipo;
    }

    /** Lado (en casillas) del tablero cuadrado para un tamaño de equipo dado. */
    public static int ladoTablero(int mutantesPorEquipo) {
        return validarMutantesPorEquipo(mutantesPorEquipo) + MARGEN_TABLERO;
    }
}
