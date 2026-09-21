package mutantes.game;

/**
 * Observador de la partida. Se invoca desde los hilos de los mutantes, con el lock del tablero
 * tomado, así que las implementaciones deben ser rápidas y no bloquearse (por ejemplo, encolar el evento).
 */
public interface Observer {
    void actualizar(GameEvent evento);
}
