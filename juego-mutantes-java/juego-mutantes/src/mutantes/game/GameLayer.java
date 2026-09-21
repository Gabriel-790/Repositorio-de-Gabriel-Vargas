package mutantes.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import mutantes.Config;
import mutantes.modelo.DPS;
import mutantes.modelo.Equipo;
import mutantes.modelo.Mutante;
import mutantes.modelo.PatronAleatorio;
import mutantes.modelo.PatronDiagonal;
import mutantes.modelo.PatronHorizontal;
import mutantes.modelo.Pattern;
import mutantes.modelo.ResultadoGolpe;
import mutantes.modelo.Superpower;
import mutantes.modelo.Superpoderes;
import mutantes.modelo.Tanque;

/**
 * Estado y reglas del juego. Toda acción de un mutante (mover, atacar, defender) pasa por aquí y
 * se ejecuta con el lock del tablero tomado, así que las reglas se aplican de una en una aunque
 * cada mutante corra en su propio hilo.
 */
public class GameLayer {
    private final int mutantesPorEquipo;
    private final Battlefield battlefield;
    private final List<Observer> observadores = new CopyOnWriteArrayList<>();
    private final AtomicBoolean partidaTerminada = new AtomicBoolean(false);
    private volatile Equipo equipo1;
    private volatile Equipo equipo2;
    private volatile Equipo ganador;

    /** Partida con el tamaño de equipo por defecto. */
    public GameLayer() {
        this(Config.MUTANTES_POR_EQUIPO_DEFECTO);
    }

    /**
     * Partida con equipos de {@code mutantesPorEquipo} mutantes (entre 3 y 11). El tablero es cuadrado
     * y su lado crece con el tamaño de los equipos.
     *
     * @throws IllegalArgumentException si el tamaño está fuera de rango
     */
    public GameLayer(int mutantesPorEquipo) {
        this.mutantesPorEquipo = Config.validarMutantesPorEquipo(mutantesPorEquipo);
        int lado = Config.ladoTablero(this.mutantesPorEquipo);
        this.battlefield = new Battlefield(lado, lado);
    }

    /** Crea los dos equipos y coloca a sus mutantes: el equipo 1 a la izquierda y el 2 a la derecha. */
    public void crearEquipos() {
        Equipo e1 = new Equipo(1, Config.SIMBOLO_EQUIPO_1);
        Equipo e2 = new Equipo(2, Config.SIMBOLO_EQUIPO_2);
        ReentrantLock lock = battlefield.getLock();
        lock.lock();
        try {
            poblar(e1, 0, 1);
            poblar(e2, battlefield.getColumnas() - 1, -1);
        } finally {
            lock.unlock();
        }
        equipo1 = e1;
        equipo2 = e2;
    }

    private void poblar(Equipo equipo, int columna, int direccion) {
        int n = mutantesPorEquipo;
        List<Superpower> poderes = Superpoderes.repartir(n);
        for (int i = 0; i < n; i++) {
            int fila = (int) ((i + 0.5) * battlefield.getFilas() / n);
            Pattern patron = crearPatron(i, direccion);
            String nombre = equipo.getSimbolo() + (i + 1);
            Mutante m = (i % 2 == 0)
                    ? new Tanque(equipo, nombre, patron, poderes.get(i))
                    : new DPS(equipo, nombre, patron, poderes.get(i));
            equipo.agregarMutante(m);
            battlefield.reservarCasilla(m, battlefield.getCasilla(fila, columna));
        }
    }

    private Pattern crearPatron(int indice, int direccion) {
        return switch (indice % 3) {
            case 0 -> new PatronAleatorio();
            case 1 -> new PatronHorizontal(direccion);
            default -> new PatronDiagonal(direccion);
        };
    }

    // ------------------------------------------------------------------ acciones

    /** Mueve al mutante un paso según su patrón. Si no puede, el patrón se entera (bloqueado). */
    public void moverMutante(Mutante m) {
        ReentrantLock lock = battlefield.getLock();
        lock.lock();
        try {
            if (partidaTerminada.get() || !m.estaVivo() || m.getPosicion() == null) {
                return;
            }
            Casilla destino = m.getPatronMovimiento().siguientePosicion(m.getPosicion(), battlefield);
            if (destino != null && battlefield.reservarCasilla(m, destino)) {
                notificar(GameEvent.movimiento(m));
            } else {
                m.getPatronMovimiento().bloqueado();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ataca a un enemigo adyacente elegido al azar. Sin enemigos a la par no hace nada.
     *
     * @return true si llegó a atacar
     */
    public boolean atacarEnemigoAdyacente(Mutante atacante) {
        ReentrantLock lock = battlefield.getLock();
        lock.lock();
        try {
            if (partidaTerminada.get() || !atacante.estaVivo()) {
                return false;
            }
            List<Mutante> enemigos = battlefield.enemigosAdyacentes(atacante);
            if (enemigos.isEmpty()) {
                return false;
            }
            Mutante objetivo = enemigos.get(ThreadLocalRandom.current().nextInt(enemigos.size()));
            ResultadoGolpe resultado = atacante.atacar(objetivo);
            notificar(GameEvent.ataque(atacante, objetivo, resultado.dano(), resultado.bloqueado()));
            if (resultado.eliminado()) {
                procesarBaja(atacante, objetivo);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Pone al mutante en postura de defensa hasta que reciba un ataque. */
    public void activarDefensa(Mutante m) {
        ReentrantLock lock = battlefield.getLock();
        lock.lock();
        try {
            if (partidaTerminada.get() || !m.estaVivo()) {
                return;
            }
            if (m.defender()) {
                notificar(GameEvent.defensa(m));
            }
        } finally {
            lock.unlock();
        }
    }

    private void procesarBaja(Mutante atacante, Mutante victima) {
        battlefield.retirar(victima);
        victima.getEquipo().registrarBaja(victima);
        atacante.aumentarAtaque();
        notificar(GameEvent.baja(atacante, victima));
        verificarVictoria();
    }

    /**
     * Gana el equipo que elimina a todos los mutantes del otro. Se anuncia una sola vez.
     *
     * @return el equipo ganador, o null si la partida sigue
     */
    public Equipo verificarVictoria() {
        ReentrantLock lock = battlefield.getLock();
        lock.lock();
        try {
            Equipo e1 = equipo1;
            Equipo e2 = equipo2;
            if (e1 == null || e2 == null) {
                return null;
            }
            Equipo candidato = null;
            if (e1.getCantidadMutantes() == 0) {
                candidato = e2;
            } else if (e2.getCantidadMutantes() == 0) {
                candidato = e1;
            }
            if (candidato != null && partidaTerminada.compareAndSet(false, true)) {
                ganador = candidato;
                notificar(GameEvent.victoria(candidato));
            }
            return ganador;
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------ observadores

    public void registrarObservador(Observer o) {
        observadores.add(o);
    }

    public void quitarObservador(Observer o) {
        observadores.remove(o);
    }

    public void notificar(GameEvent evento) {
        for (Observer o : observadores) {
            o.actualizar(evento);
        }
    }

    // ------------------------------------------------------------------ consultas

    public int getMutantesPorEquipo() {
        return mutantesPorEquipo;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public Equipo getEquipo1() {
        return equipo1;
    }

    public Equipo getEquipo2() {
        return equipo2;
    }

    public int getNumMutantesEquipo1() {
        Equipo e = equipo1;
        return e == null ? 0 : e.getCantidadMutantes();
    }

    public int getNumMutantesEquipo2() {
        Equipo e = equipo2;
        return e == null ? 0 : e.getCantidadMutantes();
    }

    public List<Mutante> getMutantes() {
        List<Mutante> todos = new ArrayList<>();
        if (equipo1 != null) {
            todos.addAll(equipo1.getMutantes());
        }
        if (equipo2 != null) {
            todos.addAll(equipo2.getMutantes());
        }
        return todos;
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada.get();
    }

    public Equipo getGanador() {
        return ganador;
    }

    public List<VistaMutante> instantanea() {
        return battlefield.instantanea();
    }
}
