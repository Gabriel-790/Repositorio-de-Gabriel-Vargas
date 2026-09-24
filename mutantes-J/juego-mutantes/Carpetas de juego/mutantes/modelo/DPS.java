package mutantes.modelo;

/** Mutante orientado a hacer daño: ataque inicial 2 a 3, defensa 1 a 3. */
public class DPS extends Mutante {
    public DPS(Equipo equipo, String nombre, Pattern patron, Superpower poder) {
        super(equipo, nombre, aleatorio(2, 3), aleatorio(1, 3), patron, poder);
    }

    @Override
    public String getTipo() {
        return "DPS";
    }
}
