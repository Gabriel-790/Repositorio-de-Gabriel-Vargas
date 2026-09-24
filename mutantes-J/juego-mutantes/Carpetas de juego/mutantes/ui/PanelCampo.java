package mutantes.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import mutantes.game.Battlefield;
import mutantes.game.GameLayer;
import mutantes.game.VistaMutante;
import mutantes.modelo.Mutante;

/**
 * Dibuja el tablero y los mutantes a partir de una foto consistente del estado. El tamaño de las
 * casillas se ajusta para que tableros grandes sigan cabiendo en pantalla; al pasar el ratón sobre
 * un mutante se muestran sus datos.
 */
public final class PanelCampo extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int CELDA_MAX = 50;
    private static final int LADO_MAX_PX = 660;
    private static final Color AZUL = new Color(0x2F7DD1);
    private static final Color ROJO = new Color(0xD9542B);
    private static final Color VERDE = new Color(0x1D9E75);
    private static final Color CASILLA_A = new Color(0xF4F3EE);
    private static final Color CASILLA_B = new Color(0xE9E7DF);

    private transient volatile GameLayer juego;
    private volatile int celda = CELDA_MAX;

    public PanelCampo() {
        setBackground(Color.WHITE);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void setJuego(GameLayer juego) {
        this.juego = juego;
        Battlefield campo = juego.getBattlefield();
        celda = Math.min(CELDA_MAX, LADO_MAX_PX / Math.max(campo.getFilas(), campo.getColumnas()));
        setPreferredSize(new Dimension(campo.getColumnas() * celda, campo.getFilas() * celda));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        GameLayer j = juego;
        if (j == null) {
            return;
        }
        int lado = celda;
        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Battlefield campo = j.getBattlefield();
            for (int f = 0; f < campo.getFilas(); f++) {
                for (int c = 0; c < campo.getColumnas(); c++) {
                    g.setColor((f + c) % 2 == 0 ? CASILLA_A : CASILLA_B);
                    g.fillRect(c * lado, f * lado, lado, lado);
                }
            }
            for (VistaMutante v : j.instantanea()) {
                dibujarMutante(g, v, lado);
            }
        } finally {
            g.dispose();
        }
    }

    /** Tanque = cuadrado redondeado, DPS = círculo; borde verde grueso = defendiendo. */
    private void dibujarMutante(Graphics2D g, VistaMutante v, int lado) {
        double k = lado / (double) CELDA_MAX;
        int relleno = Math.max(2, (int) Math.round(3 * k));
        int margen = Math.max(2, (int) Math.round(4 * k));
        int alturaBarra = Math.max(3, (int) Math.round(5 * k));
        int x = v.columna() * lado + margen;
        int y = v.fila() * lado + relleno;
        int ancho = lado - 2 * margen;
        int alto = lado - 3 * relleno - alturaBarra;
        Color base = v.equipo() == 1 ? AZUL : ROJO;

        Shape forma = "Tanque".equals(v.tipo())
                ? new RoundRectangle2D.Double(x, y, ancho, alto, 10 * k, 10 * k)
                : new Ellipse2D.Double(x, y, ancho, alto);
        g.setColor(base);
        g.fill(forma);
        g.setStroke(new BasicStroke(v.defendiendo() ? (float) Math.max(3, 4 * k) : 1.5f));
        g.setColor(v.defendiendo() ? VERDE : base.darker().darker());
        g.draw(forma);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, (int) Math.round(12 * k))));
        centrar(g, v.nombre(), x + ancho / 2, y + (int) Math.round(alto * 0.38));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(8, (int) Math.round(10 * k))));
        centrar(g, v.ataque() + "·" + v.defensa(), x + ancho / 2, y + (int) Math.round(alto * 0.74));

        // Barra de energía.
        int barraY = y + alto + relleno;
        double proporcion = Math.max(0, Math.min(1, v.energia() / (double) Mutante.ENERGIA_INICIAL));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0xB4B2A9));
        g.fillRect(x, barraY, ancho, alturaBarra);
        g.setColor(proporcion > 0.5 ? new Color(0x3B9B3B)
                : proporcion > 0.25 ? new Color(0xE0A020) : new Color(0xC93030));
        g.fillRect(x, barraY, (int) Math.round(ancho * proporcion), alturaBarra);
    }

    private void centrar(Graphics2D g, String texto, int cx, int cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, cx - fm.stringWidth(texto) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        GameLayer j = juego;
        if (j == null) {
            return null;
        }
        int columna = e.getX() / celda;
        int fila = e.getY() / celda;
        for (VistaMutante v : j.instantanea()) {
            if (v.fila() == fila && v.columna() == columna) {
                return v.nombre() + " (" + v.tipo() + ") · energía " + v.energia() + " · ataque " + v.ataque()
                        + " · defensa " + v.defensa() + " · poder " + v.superpoder()
                        + (v.defendiendo() ? " · defendiendo" : "");
            }
        }
        return null;
    }
}
