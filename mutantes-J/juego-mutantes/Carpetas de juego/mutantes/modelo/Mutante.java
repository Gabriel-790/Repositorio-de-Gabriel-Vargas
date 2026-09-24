package mutantes.modelo;

import java.util.concurrent.ThreadLocalRandom;

import mutantes.game.Casilla;

/**
 * Clase base de los mutantes.
 *
 * Sincronización: energía, ataque y estado de defensa están protegidos por el monitor del propio
 * mutante (métodos synchronized). Ningún método synchronized llama al tablero, así que el orden de
 * bloqueo siempre es: lock del tablero, después monitor del mutante.
 */
public abstract class Mutante {
    public static final int ENERGIA_INICIAL = 100;
    public static final int ATAQUE_MAX = 7;
    public static final int DANO_MINIMO = 1;

    private final Equipo equipo;
    private final String nombre;
    private final int defensa;
    private final Superpower superpoder;
    private final Pattern patronMovimiento;

    private int puntosEnergia = ENERGIA_INICIAL;
    private int ataque;
    private boolean defendiendo;
    private volatile Casilla posicion;

    protected Mutante(Equipo equipo, String nombre, int ataque, int defensa,
                      Pattern patronMovimiento, Superpower superpoder) {
        this.equipo = equipo;
        this.nombre = nombre;
        this.ataque = ataque;
        this.defensa = defensa;
        this.patronMovimiento = patronMovimiento;
        this.superpoder = superpoder;
    }

    /** Valor entero aleatorio en [min, max], ambos incluidos. */
    protected static int aleatorio(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public abstract String getTipo();

    /** Golpea al objetivo con el ataque actual. El superpoder solo cambia el mensaje, no el daño. */
    public ResultadoGolpe atacar(Mutante objetivo) {
        return objetivo.recibirGolpe(getAtaque());
    }

    /**
     * Activa la postura de defensa.
     *
     * @return true si cambió el estado (estaba vivo y no defendía)
     */
    public synchronized boolean defender() {
        if (defendiendo || puntosEnergia <= 0) {
            return false;
        }
        defendiendo = true;
        return true;
    }

    /**
     * Recibe un golpe. Atómico: solo el golpe que lleva la energía a 0 devuelve eliminado = true.
     * Defendiendo: daño = max(1, ataque // defensa) y la defensa se consume.
     * Sin defender: daño = ataque completo (mínimo 1).
     */
    public synchronized ResultadoGolpe recibirGolpe(int ataqueRival) {
        if (puntosEnergia <= 0) {
            return new ResultadoGolpe(0, false, false);
        }
        boolean bloqueado = defendiendo;
        int dano = bloqueado ? ataqueRival / defensa : ataqueRival;
        dano = Math.max(DANO_MINIMO, dano);
        defendiendo = false;
        puntosEnergia = Math.max(0, puntosEnergia - dano);
        return new ResultadoGolpe(dano, puntosEnergia == 0, bloqueado);
    }

    /** Suma 1 al ataque, con tope en ATAQUE_MAX. */
    public synchronized void aumentarAtaque() {
        if (ataque < ATAQUE_MAX) {
            ataque++;
        }
    }

    public synchronized boolean estaVivo() {
        return puntosEnergia > 0;
    }

    public synchronized int getPuntosEnergia() {
        return puntosEnergia;
    }

    public synchronized int getAtaque() {
        return ataque;
    }

    public synchronized boolean isDefendiendo() {
        return defendiendo;
    }

    public int getDefensa() {
        return defensa;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public String getNombre() {
        return nombre;
    }

    public Superpower getSuperpoder() {
        return superpoder;
    }

    public Pattern getPatronMovimiento() {
        return patronMovimiento;
    }

    public Casilla getPosicion() {
        return posicion;
    }

    /** Solo lo debe llamar el Battlefield, con su lock tomado. */
    public void setPosicion(Casilla posicion) {
        this.posicion = posicion;
    }

    @Override
    public String toString() {
        return nombre + " (" + getTipo() + ")";
    }
}
