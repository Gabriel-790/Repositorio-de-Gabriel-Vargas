package mutantes.modelo;

import java.util.concurrent.ThreadLocalRandom;

import mutantes.Config;
import mutantes.game.Battlefield;
import mutantes.game.Casilla;

/** Avanza en diagonal y rebota en las paredes; si la casilla está ocupada, invierte el sentido. */
public class PatronDiagonal implements Pattern {
    private int dx;
    private int dy;

    public PatronDiagonal(int direccionHorizontalInicial) {
        this.dx = direccionHorizontalInicial >= 0 ? 1 : -1;
        this.dy = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
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
        if (!campo.dentroDeLimites(f + dy, c)) {
            dy = -dy;
        }
        if (!campo.dentroDeLimites(f, c + dx)) {
            dx = -dx;
        }
        return campo.getCasilla(f + dy, c + dx);
    }

    @Override
    public void bloqueado() {
        dx = -dx;
        dy = -dy;
    }
}
