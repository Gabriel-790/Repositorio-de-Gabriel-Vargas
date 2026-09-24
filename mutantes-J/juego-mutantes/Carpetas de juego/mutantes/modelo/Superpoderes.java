package mutantes.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Catálogo de superpoderes y reparto entre los mutantes.
 *
 * Para agregar un superpoder basta con añadir una línea a CATALOGO: nombre y frase que se muestra al atacar.
 */
public final class Superpoderes {
    private Superpoderes() {}

    public static final List<Superpower> CATALOGO = List.of(
            new Superpower("TKFIRE", "lanzó una llamarada"),
            new Superpower("TKZAP", "liberó una descarga eléctrica"),
            new Superpower("TKCLAW", "desgarró al enemigo con sus garras"),
            new Superpower("TKSHOCK", "provoca una Onda sísmica"),
            new Superpower("TKICE", "exhala su Aliento helado"),
            new Superpower("TKPSYCHIC", "lanza al enemigo con telequinesis"),
            new Superpower("TKPOISON", "envenena al enemigo"),
            new Superpower("TKROCK", "dispara rocas al rival"),
            new Superpower("TKPOWER", "hace explotar al enemigo"),
            new Superpower("TKSOUND", "genera un potente alarido"),
            new Superpower("TKACID", "escupe una niebla ácida"));

    /**
     * Reparte un superpoder a cada uno de {@code cantidad} mutantes: baraja el catálogo y lo va
     * entregando en orden. Así todos reciben uno y no se repite ninguno dentro del reparto mientras
     * haya poderes distintos (el catálogo tiene tantos como el tamaño máximo de un equipo).
     * Si {@code cantidad} supera el catálogo, se vuelve a empezar por el principio de la baraja.
     */
    public static List<Superpower> repartir(int cantidad) {
        List<Superpower> baraja = new ArrayList<>(CATALOGO);
        Collections.shuffle(baraja, ThreadLocalRandom.current());
        List<Superpower> repartidos = new ArrayList<>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            repartidos.add(baraja.get(i % baraja.size()));
        }
        return repartidos;
    }
}
