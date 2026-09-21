package mutantes.control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mutantes.Config;
import mutantes.game.GameEvent;
import mutantes.game.GameLayer;
import mutantes.game.Observer;
import mutantes.game.TipoEvento;
import mutantes.modelo.Mutante;

/** Arranca y detiene la partida: crea un hilo por mutante y los coordina. */
public class ControlLayer implements Observer {
    private final GameLayer juego;
    private final List<Thread> hilos = new ArrayList<>();
    private final AtomicBoolean partidaActiva = new AtomicBoolean(false);

    public ControlLayer(GameLayer juego) {
        this.juego = juego;
    }

    /** Crea los equipos y pone a todos los mutantes en movimiento. */
    public void iniciarPartida() {
        juego.crearEquipos();
        juego.registrarObservador(this);
        moverMutantes();
    }

    /** Lanza un hilo por mutante; cada uno se mueve y decide de forma autónoma. */
    public void moverMutantes() {
        partidaActiva.set(true);
        synchronized (hilos) {
            for (Mutante m : juego.getMutantes()) {
                HiloMutante tarea = new HiloMutante(m, juego, partidaActiva, Config.TICK_MS);
                Thread hilo = new Thread(tarea, "mutante-" + m.getNombre());
                hilo.setDaemon(true);
                hilos.add(hilo);
                hilo.start();
            }
        }
    }

    /** Detiene todos los hilos (por ejemplo, al cerrar la ventana o empezar otra partida). */
    public void detenerPartida() {
        partidaActiva.set(false);
        juego.quitarObservador(this);
        synchronized (hilos) {
            for (Thread hilo : hilos) {
                if (hilo != Thread.currentThread()) {
                    hilo.interrupt();
                }
            }
        }
    }

    public boolean isPartidaActiva() {
        return partidaActiva.get();
    }

    @Override
    public void actualizar(GameEvent evento) {
        if (evento.getTipo() == TipoEvento.VICTORIA) {
            partidaActiva.set(false);
        }
    }
}
