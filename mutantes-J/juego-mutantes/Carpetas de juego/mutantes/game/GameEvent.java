package mutantes.game;

import mutantes.modelo.Equipo;
import mutantes.modelo.Mutante;

/** Evento inmutable que la Game layer publica a sus observadores. */
public final class GameEvent {
    private final TipoEvento tipo;
    private final Mutante origen;
    private final Mutante destino;
    private final int dano;
    private final boolean bloqueado;
    private final Equipo ganador;

    private GameEvent(TipoEvento tipo, Mutante origen, Mutante destino, int dano,
                      boolean bloqueado, Equipo ganador) {
        this.tipo = tipo;
        this.origen = origen;
        this.destino = destino;
        this.dano = dano;
        this.bloqueado = bloqueado;
        this.ganador = ganador;
    }

    public static GameEvent movimiento(Mutante m) {
        return new GameEvent(TipoEvento.MOVIMIENTO, m, null, 0, false, null);
    }

    public static GameEvent ataque(Mutante atacante, Mutante objetivo, int dano, boolean bloqueado) {
        return new GameEvent(TipoEvento.ATAQUE, atacante, objetivo, dano, bloqueado, null);
    }

    public static GameEvent defensa(Mutante m) {
        return new GameEvent(TipoEvento.DEFENSA, m, null, 0, false, null);
    }

    public static GameEvent baja(Mutante atacante, Mutante victima) {
        return new GameEvent(TipoEvento.BAJA, atacante, victima, 0, false, null);
    }

    public static GameEvent victoria(Equipo ganador) {
        return new GameEvent(TipoEvento.VICTORIA, null, null, 0, false, ganador);
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Mutante getOrigen() {
        return origen;
    }

    public Mutante getDestino() {
        return destino;
    }

    public int getDano() {
        return dano;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public Equipo getGanador() {
        return ganador;
    }

    /** Texto legible para logs y para la interfaz. En un ataque se muestra el mensaje del superpoder del atacante. */
    public String describir() {
        return switch (tipo) {
            case MOVIMIENTO -> origen + " se mueve";
            case ATAQUE -> describirAtaque();
            case DEFENSA -> origen + " se pone en guardia";
            case BAJA -> origen + " elimina a " + destino;
            case VICTORIA -> "¡Victoria del equipo " + ganador.getSimbolo() + "!";
        };
    }

    private String describirAtaque() {
        String resultado = ": -" + dano + " de energía" + (bloqueado ? " (bloqueado)" : "");
        return origen + " " + origen.getSuperpoder().getMensaje() + " sobre " + destino + resultado;
    }
}
