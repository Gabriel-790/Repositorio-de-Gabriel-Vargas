package mutantes.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import mutantes.game.Battlefield;
import mutantes.game.Casilla;

/**
 * Patrón de movimiento de un mutante. Cada mutante tiene su propia instancia
 * (los patrones guardan estado, por ejemplo la dirección actual).
 * Siempre se invoca con el lock del tablero tomado.
 */
public interface Pattern {

    /** Casilla adyacente a la que quiere ir el mutante, o null si no puede moverse. */
    Casilla siguientePosicion(Casilla actual, Battlefield campo);

    /** Se avisa cuando la casilla elegida estaba ocupada o no había casilla disponible. */
    default void bloqueado() {
    }

    /** Elige al azar una casilla adyacente libre (o null si no hay ninguna). */
    static Casilla pasoAleatorio(Casilla actual, Battlefield campo) {
        List<Casilla> libres = new ArrayList<>();
        for (Casilla c : campo.adyacentes(actual)) {
            if (c.estaLibre()) {
                libres.add(c);
            }
        }
        if (libres.isEmpty()) {
            return null;
        }
        return libres.get(ThreadLocalRandom.current().nextInt(libres.size()));
    }
}
