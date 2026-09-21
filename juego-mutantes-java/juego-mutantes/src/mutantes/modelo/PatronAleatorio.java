package mutantes.modelo;

import mutantes.game.Battlefield;
import mutantes.game.Casilla;

/** Se mueve a una casilla adyacente libre elegida al azar. */
public class PatronAleatorio implements Pattern {
    @Override
    public Casilla siguientePosicion(Casilla actual, Battlefield campo) {
        return Pattern.pasoAleatorio(actual, campo);
    }
}
