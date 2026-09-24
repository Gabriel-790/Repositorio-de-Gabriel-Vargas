package mutantes.game;

/** Copia inmutable del estado de un mutante, segura para dibujar desde otro hilo. */
public record VistaMutante(String nombre, String tipo, String simbolo, int equipo,
                           int fila, int columna, int energia, int ataque, int defensa,
                           boolean defendiendo, String superpoder) {
}
