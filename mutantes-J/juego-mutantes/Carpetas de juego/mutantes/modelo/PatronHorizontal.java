package mutantes.modelo;

import java.util.concurrent.ThreadLocalRandom;

import mutantes.Config;
import mutantes.game.Battlefield;
import mutantes.game.Casilla;

/**
 * Recorre la fila de lado a lado. Al llegar a una pared cambia de sentido y avanza una fila
 * (recorrido en serpiente); si la casilla está ocupada, da la vuelta.
 */
public class PatronHorizontal implements Pattern {
    private int dx;
    private int dy = 1;

    public PatronHorizontal(int direccionInicial) {
        this.dx = direccionInicial >= 0 ? 1 : -1;
    }

    @Override
    public Casilla siguientePosicion(Casilla actual, Battlefield campo) {
        if (ThreadLocalRandom.current().nextDouble() < Config.PROB_PASO_ALEATORIO) {
            Casilla azar = Pattern.pasoAleatorio(actual, campo);
            if (azar != null) {
                return azar;
            }
        }
        int f = actual.getFila();
        int c = actual.getColumna();
        Casilla siguiente = campo.getCasilla(f, c + dx);
        if (siguiente == null) {
            dx = -dx;
            if (!campo.dentroDeLimites(f + dy, c)) {
                dy = -dy;
            }
            siguiente = campo.getCasilla(f + dy, c);
        }
        return siguiente;
    }

    @Override
    public void bloqueado() {
        dx = -dx;
    }
}
