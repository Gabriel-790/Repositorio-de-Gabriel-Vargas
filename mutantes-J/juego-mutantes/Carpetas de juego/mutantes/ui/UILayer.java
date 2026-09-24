package mutantes.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;

import mutantes.Config;
import mutantes.game.GameEvent;
import mutantes.game.GameLayer;
import mutantes.game.Observer;
import mutantes.game.TipoEvento;
import mutantes.modelo.Equipo;

/**
 * Interfaz gráfica (Swing). Observa a la Game layer: los hilos de juego solo encolan eventos y un
 * Timer de Swing los consume desde el hilo de interfaz. Todos los métodos, salvo actualizar(),
 * deben llamarse desde el hilo de eventos de Swing.
 */
public class UILayer implements Observer {
    private static final int MAX_LINEAS_LOG = 300;

    private final BlockingQueue<GameEvent> colaEventos = new LinkedBlockingQueue<>();
    private final IntConsumer alReiniciar;
    private final int mutantesIniciales;
    private volatile GameLayer juego;

    private final PanelCampo panel = new PanelCampo();
    private JFrame ventana;
    private JSpinner selectorTamano;
    private JTextArea log;
    private JLabel marcador;
    private JLabel banner;

    /**
     * @param alReiniciar       se invoca con el tamaño de equipo elegido al pulsar "Nueva partida"
     * @param mutantesIniciales valor inicial del selector (entre 3 y 11)
     */
    public UILayer(IntConsumer alReiniciar, int mutantesIniciales) {
        this.alReiniciar = alReiniciar;
        this.mutantesIniciales = Config.validarMutantesPorEquipo(mutantesIniciales);
    }

    /** Construye y muestra la ventana. */
    public void mostrar() {
        ventana = new JFrame("Juego de mutantes");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        marcador = new JLabel(" ");
        marcador.setFont(marcador.getFont().deriveFont(Font.BOLD, 15f));

        selectorTamano = new JSpinner(new SpinnerNumberModel(mutantesIniciales,
                Config.MIN_MUTANTES_POR_EQUIPO, Config.MAX_MUTANTES_POR_EQUIPO, 1));
        selectorTamano.setToolTipText("Mutantes por equipo (de " + Config.MIN_MUTANTES_POR_EQUIPO
                + " a " + Config.MAX_MUTANTES_POR_EQUIPO + "); el tablero crece con el tamaño");
        JButton nueva = new JButton("Nueva partida");
        nueva.setToolTipText("Empieza una partida con el tamaño de equipo elegido");
        nueva.addActionListener(e -> pedirNuevaPartida());

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.add(new JLabel("Mutantes por equipo:"));
        controles.add(selectorTamano);
        controles.add(nueva);

        JPanel norte = new JPanel(new BorderLayout(10, 0));
        norte.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        norte.add(marcador, BorderLayout.CENTER);
        norte.add(controles, BorderLayout.EAST);

        log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setPreferredSize(new Dimension(340, 200));

        banner = new JLabel(" ", JLabel.CENTER);
        banner.setFont(banner.getFont().deriveFont(Font.BOLD, 18f));
        JLabel leyenda = new JLabel("<html>Cuadrado = Tanque, círculo = DPS. Borde verde grueso = defendiendo. "
                + "Barra = energía. Números = ataque·defensa. Pasa el ratón sobre un mutante para ver sus datos.</html>");
        leyenda.setFont(leyenda.getFont().deriveFont(11f));
        JPanel sur = new JPanel(new BorderLayout(0, 4));
        sur.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        sur.add(banner, BorderLayout.NORTH);
        sur.add(leyenda, BorderLayout.SOUTH);

        ventana.add(norte, BorderLayout.NORTH);
        ventana.add(panel, BorderLayout.CENTER);
        ventana.add(scroll, BorderLayout.EAST);
        ventana.add(sur, BorderLayout.SOUTH);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        new Timer(60, e -> refrescar()).start();
    }

    private void pedirNuevaPartida() {
        try {
            selectorTamano.commitEdit();
        } catch (ParseException ignorada) {
            // Texto no numérico: el selector conserva el último valor válido.
        }
        alReiniciar.accept(((Number) selectorTamano.getValue()).intValue());
    }

    /** Conecta la UI con una partida nueva (deja de observar la anterior) y ajusta la ventana al tablero. */
    public void enlazar(GameLayer nuevo) {
        GameLayer anterior = this.juego;
        if (anterior != null) {
            anterior.quitarObservador(this);
        }
        colaEventos.clear();
        this.juego = nuevo;
        nuevo.registrarObservador(this);
        panel.setJuego(nuevo);
        if (ventana != null) {
            log.setText("");
            banner.setText(" ");
            Dimension antes = ventana.getSize();
            ventana.pack();
            if (!antes.equals(ventana.getSize())) {
                ventana.setLocationRelativeTo(null);
            }
        }
    }

    /** Lo invocan los hilos de los mutantes: solo encola, no toca componentes de Swing. */
    @Override
    public void actualizar(GameEvent evento) {
        colaEventos.offer(evento);
    }

    private void refrescar() {
        List<GameEvent> lote = new ArrayList<>();
        colaEventos.drainTo(lote, 500);
        for (GameEvent e : lote) {
            if (e.getTipo() == TipoEvento.MOVIMIENTO) {
                continue;
            }
            log.append(e.describir() + "\n");
            if (e.getTipo() == TipoEvento.VICTORIA) {
                mostrarVictoria(e.getGanador());
            }
        }
        recortarLog();
        log.setCaretPosition(log.getDocument().getLength());
        actualizarMarcador();
        dibujarCampo();
    }

    public void dibujarCampo() {
        panel.repaint();
    }

    public void mostrarVictoria(Equipo ganador) {
        banner.setText("¡Victoria del equipo " + ganador.getSimbolo() + "!");
        banner.setForeground(ganador.getNumero() == 1 ? new Color(0x2F7DD1) : new Color(0xD9542B));
    }

    private void actualizarMarcador() {
        GameLayer j = juego;
        if (j == null || j.getEquipo1() == null) {
            return;
        }
        marcador.setText("Equipo " + j.getEquipo1().getSimbolo() + ": " + j.getNumMutantesEquipo1()
                + " vivos    |    Equipo " + j.getEquipo2().getSimbolo() + ": " + j.getNumMutantesEquipo2() + " vivos");
    }

    private void recortarLog() {
        int sobrantes = log.getLineCount() - MAX_LINEAS_LOG;
        if (sobrantes > 0) {
            try {
                log.replaceRange("", 0, log.getLineEndOffset(sobrantes - 1));
            } catch (BadLocationException ignorada) {
                // No debería ocurrir: el índice de línea se acaba de calcular.
            }
        }
    }
}
