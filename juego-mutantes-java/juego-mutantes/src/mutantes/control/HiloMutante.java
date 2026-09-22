package mutantes.control;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import mutantes.Config;
import mutantes.game.GameLayer;
import mutantes.modelo.Mutante;

/**
 * Hilo de un mutante. En cada tick:
 * 1) decide al azar (85 % atacar, 15 % defender);
 * 2) si ataca, golpea a un enemigo adyacente (si no hay ninguno, no ataca);
 * 3) si defiende, activa la defensa;
 * 4) se mueve según su patrón (también mientras defiende);
 * 5) espera el intervalo del tick.
 * Termina cuando el mutante es eliminado o cuando acaba la partida.
 */
public class HiloMutante implements Runnable {
    public static final double PROB_ATACAR = Config.PROB_ATACAR;

    private final Mutante mutante;
    private final GameLayer juego;
    private final AtomicBoolean partidaActiva;
    private final long intervaloTick;

    public HiloMutante(Mutante mutante, GameLayer juego, AtomicBoolean partidaActiva, long intervaloTick) {
        this.mutante = mutante;
        this.juego = juego;
        this.partidaActiva = partidaActiva;
        this.intervaloTick = intervaloTick;
    }

    @Override
    public void run() {
        try {
            // Desfase inicial aleatorio para que los hilos no vayan al mismo compás.
            Thread.sleep(ThreadLocalRandom.current().nextLong(intervaloTick + 1));
            while (partidaActiva.get() && !juego.isPartidaTerminada() && mutante.estaVivo()) {
                if (decidirAccion() == Accion.ATACAR) {
                    atacarEnemigoAdyacente();
                } else {
                    activarDefensa();
                }
                moverSegunPatron();
                Thread.sleep(intervaloConVariacion());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Accion decidirAccion() {
        return ThreadLocalRandom.current().nextDouble() < PROB_ATACAR ? Accion.ATACAR : Accion.DEFENDER;
    }

    private void atacarEnemigoAdyacente() {
        juego.atacarEnemigoAdyacente(mutante);
    }

    private void activarDefensa() {
        juego.activarDefensa(mutante);
    }

    private void moverSegunPatron() {
        juego.moverMutante(mutante);
    }

    /** Intervalo del tick con una variación de ±20 %. */
    private long intervaloConVariacion() {
        double factor = 0.8 + ThreadLocalRandom.current().nextDouble() * 0.4;
        return Math.max(1L, Math.round(intervaloTick * factor));
    }
}
