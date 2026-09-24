package mutantes.modelo;

/** Mutante orientado a resistir: ataque inicial 1 a 3, defensa 2 a 3. */
public class Tanque extends Mutante {
    public Tanque(Equipo equipo, String nombre, Pattern patron, Superpower poder) {
        super(equipo, nombre, aleatorio(1, 3), aleatorio(2, 3), patron, poder);
    }

    @Override
    public String getTipo() {
        return "Tanque";
    }
}
