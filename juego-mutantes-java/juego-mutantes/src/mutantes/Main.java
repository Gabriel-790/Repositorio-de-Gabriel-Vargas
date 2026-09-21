package mutantes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import mutantes.control.ControlLayer;
import mutantes.game.GameLayer;
import mutantes.game.TipoEvento;
import mutantes.ui.UILayer;

/**
 * Punto de entrada.
 *   java -cp out mutantes.Main                       abre la interfaz gráfica
 *   java -cp out mutantes.Main --mutantes 8          tamaño inicial de los equipos (3 a 11)
 *   java -cp out mutantes.Main --consola             juega sin interfaz y escribe los eventos en la terminal
 *   (con --verbose también muestra los movimientos)
 * En la interfaz gráfica el tamaño de los equipos se puede cambiar antes de cada partida.
 */
public final class Main {
    private final int mutantesIniciales;
    private ControlLayer control;
    private UILayer ui;

    private Main(int mutantesIniciales) {
        this.mutantesIniciales = mutantesIniciales;
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> argumentos = Arrays.asList(args);
        int mutantes = leerMutantesPorEquipo(argumentos);
        if (argumentos.contains("--consola")) {
            ejecutarConsola(mutantes, argumentos.contains("--verbose"));
        } else {
            SwingUtilities.invokeLater(() -> new Main(mutantes).iniciarGui());
        }
    }

    private static int leerMutantesPorEquipo(List<String> argumentos) {
        int i = argumentos.indexOf("--mutantes");
        if (i < 0) {
            return Config.MUTANTES_POR_EQUIPO_DEFECTO;
        }
        try {
            return Config.validarMutantesPorEquipo(Integer.parseInt(argumentos.get(i + 1)));
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            System.err.println("Uso: --mutantes N, con N entre " + Config.MIN_MUTANTES_POR_EQUIPO
                    + " y " + Config.MAX_MUTANTES_POR_EQUIPO);
            System.exit(1);
            return 0;
        }
    }

    private void iniciarGui() {
        ui = new UILayer(this::nuevaPartida, mutantesIniciales);
        ui.mostrar();
        nuevaPartida(mutantesIniciales);
    }

    private void nuevaPartida(int mutantesPorEquipo) {
        if (control != null) {
            control.detenerPartida();
        }
        GameLayer juego = new GameLayer(mutantesPorEquipo);
        ui.enlazar(juego);
        control = new ControlLayer(juego);
        control.iniciarPartida();
    }

    private static void ejecutarConsola(int mutantesPorEquipo, boolean verbose) throws InterruptedException {
        GameLayer juego = new GameLayer(mutantesPorEquipo);
        CountDownLatch fin = new CountDownLatch(1);
        juego.registrarObservador(e -> {
            if (verbose || e.getTipo() != TipoEvento.MOVIMIENTO) {
                System.out.println(e.describir());
            }
            if (e.getTipo() == TipoEvento.VICTORIA) {
                fin.countDown();
            }
        });
        ControlLayer control = new ControlLayer(juego);
        control.iniciarPartida();
        fin.await();
        control.detenerPartida();
        System.out.println("Fin de la partida. Ganador: equipo " + juego.getGanador().getSimbolo());
    }
}
