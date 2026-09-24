package mutantes.modelo;

/**
 * Superpoder de un mutante. Es solo un nombre y un mensaje: no cambia el daño ni la defensa.
 * Cuando el mutante ataca, el mensaje del superpoder es el que aparece en el log y en la UI.
 * Ejemplo: mensaje "lanza una Bola de fuego" produce
 * "A1 (DPS) lanza una Bola de fuego sobre B3 (Tanque): -3 de energía".
 */
public final class Superpower {
    private final String nombre;
    private final String mensaje;

    public Superpower(String nombre, String mensaje) {
        this.nombre = nombre;
        this.mensaje = mensaje;
    }

    /** Nombre corto del poder (se ve al pasar el ratón sobre el mutante). */
    public String getNombre() {
        return nombre;
    }

    /** Frase que se muestra al atacar, entre el nombre del atacante y el del objetivo. */
    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
