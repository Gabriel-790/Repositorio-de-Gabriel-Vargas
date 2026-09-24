package mutantes.game;

import java.util.List;

import mutantes.modelo.Mutante;

/** Casilla del tablero. El ocupante solo se lee o modifica con el lock del Battlefield tomado. */
public class Casilla {
    private final Battlefield campo;
    private final int fila;
    private final int columna;
    private Mutante ocupante;

    Casilla(Battlefield campo, int fila, int columna) {
        this.campo = campo;
        this.fila = fila;
        this.columna = columna;
    }

    public boolean estaLibre() {
        return ocupante == null;
    }

    public Mutante getOcupante() {
        return ocupante;
    }

    void setOcupante(Mutante ocupante) {
        this.ocupante = ocupante;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    /** Las hasta 8 casillas vecinas (vertical, horizontal y diagonal). */
    public List<Casilla> adyacentes() {
        return campo.adyacentes(this);
    }
}
