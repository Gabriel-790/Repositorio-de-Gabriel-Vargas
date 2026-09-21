package mutantes.modelo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Un equipo de mutantes. El juego crea dos instancias (equipo 1 y equipo 2). */
public class Equipo {
    private final int numero;
    private final String simbolo;
    private final List<Mutante> mutantes = new CopyOnWriteArrayList<>();
    private final AtomicInteger cantidadMutantes = new AtomicInteger();

    public Equipo(int numero, String simbolo) {
        this.numero = numero;
        this.simbolo = simbolo;
    }

    public void agregarMutante(Mutante m) {
        mutantes.add(m);
        cantidadMutantes.incrementAndGet();
    }

    /** Descuenta un mutante vivo. Se llama una sola vez por baja. */
    public void registrarBaja(Mutante m) {
        cantidadMutantes.decrementAndGet();
    }

    public List<Mutante> mutantesVivos() {
        return mutantes.stream().filter(Mutante::estaVivo).toList();
    }

    public int getNumero() {
        return numero;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public int getCantidadMutantes() {
        return cantidadMutantes.get();
    }

    public List<Mutante> getMutantes() {
        return Collections.unmodifiableList(mutantes);
    }
}
