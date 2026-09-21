package mutantes.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import mutantes.modelo.Mutante;

/**
 * Tablero de casillas. Un único lock protege posiciones y ocupación: cualquier operación que lea o
 * escriba el tablero (movimiento, búsqueda de enemigos adyacentes, ataque) se hace con ese lock
 * tomado, de modo que "está libre, ocúpala" y "está a la par, golpea" son atómicos.
 */
public final class Battlefield {
    private final int filas;
    private final int columnas;
    private final Casilla[][] casillas;
    private final ReentrantLock lock = new ReentrantLock();

    public Battlefield(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        this.casillas = new Casilla[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                casillas[f][c] = new Casilla(this, f, c);
            }
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public boolean dentroDeLimites(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    /** Casilla en (fila, columna) o null si está fuera del tablero. */
    public Casilla getCasilla(int fila, int columna) {
        return dentroDeLimites(fila, columna) ? casillas[fila][columna] : null;
    }

    /**
     * Mueve (o coloca) al mutante en la casilla destino si está libre.
     *
     * @return true si la reserva tuvo éxito
     */
    public boolean reservarCasilla(Mutante m, Casilla destino) {
        lock.lock();
        try {
            if (destino == null || !destino.estaLibre()) {
                return false;
            }
            Casilla anterior = m.getPosicion();
            if (anterior != null) {
                anterior.setOcupante(null);
            }
            destino.setOcupante(m);
            m.setPosicion(destino);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void liberarCasilla(Casilla c) {
        lock.lock();
        try {
            c.setOcupante(null);
        } finally {
            lock.unlock();
        }
    }

    /** Saca al mutante del tablero (por ejemplo, al ser eliminado). */
    public void retirar(Mutante m) {
        lock.lock();
        try {
            Casilla c = m.getPosicion();
            if (c != null) {
                c.setOcupante(null);
                m.setPosicion(null);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Casilla> adyacentes(Casilla c) {
        List<Casilla> vecinas = new ArrayList<>(8);
        for (int df = -1; df <= 1; df++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (df == 0 && dc == 0) {
                    continue;
                }
                Casilla v = getCasilla(c.getFila() + df, c.getColumna() + dc);
                if (v != null) {
                    vecinas.add(v);
                }
            }
        }
        return vecinas;
    }

    /** Enemigos vivos en las casillas adyacentes al mutante. */
    public List<Mutante> enemigosAdyacentes(Mutante m) {
        lock.lock();
        try {
            List<Mutante> enemigos = new ArrayList<>();
            Casilla propia = m.getPosicion();
            if (propia == null) {
                return enemigos;
            }
            for (Casilla v : adyacentes(propia)) {
                Mutante otro = v.getOcupante();
                if (otro != null && otro.getEquipo() != m.getEquipo() && otro.estaVivo()) {
                    enemigos.add(otro);
                }
            }
            return enemigos;
        } finally {
            lock.unlock();
        }
    }

    /** Foto consistente de todos los mutantes en el tablero, para dibujar. */
    public List<VistaMutante> instantanea() {
        lock.lock();
        try {
            List<VistaMutante> vistas = new ArrayList<>();
            for (int f = 0; f < filas; f++) {
                for (int c = 0; c < columnas; c++) {
                    Mutante m = casillas[f][c].getOcupante();
                    if (m != null) {
                        vistas.add(new VistaMutante(m.getNombre(), m.getTipo(),
                                m.getEquipo().getSimbolo(), m.getEquipo().getNumero(),
                                f, c, m.getPuntosEnergia(), m.getAtaque(), m.getDefensa(),
                                m.isDefendiendo(), m.getSuperpoder().getNombre()));
                    }
                }
            }
            return vistas;
        } finally {
            lock.unlock();
        }
    }
}
