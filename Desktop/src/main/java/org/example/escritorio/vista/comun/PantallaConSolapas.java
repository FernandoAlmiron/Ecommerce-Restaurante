package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Base de las áreas con varias pantallas (Cocina, Caja...): título grande, solapas subrayadas
 * y debajo la pantalla de la solapa elegida. Cada área solo agrega sus pantallas con {@link #agregar}.
 */
public class PantallaConSolapas extends JPanel {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final JPanel barraSolapas = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final ButtonGroup grupo = new ButtonGroup();

    public PantallaConSolapas(String titulo) {
        super(new BorderLayout(0, 24));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.relleno(30, 48, 28, 48));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.fuente(Font.BOLD, 34f));
        lblTitulo.setForeground(Estilo.TEXTO);

        contenido.setOpaque(false);

        JPanel solapas = Estilo.transparente(new BorderLayout());
        solapas.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE));
        solapas.add(barraSolapas, BorderLayout.WEST);

        JPanel arriba = Estilo.transparente(new BorderLayout(0, 18));
        arriba.add(lblTitulo, BorderLayout.NORTH);
        arriba.add(solapas, BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    /** Agrega una pantalla con su solapa. La primera que se agrega queda elegida. */
    protected Solapa agregar(String titulo, JComponent pantalla) {
        String clave = titulo;
        contenido.add(pantalla, clave);
        Solapa solapa = new Solapa(titulo);
        solapa.addActionListener(e -> tarjetas.show(contenido, clave));
        grupo.add(solapa);
        barraSolapas.add(solapa);
        if (grupo.getButtonCount() == 1) {
            solapa.setSelected(true);
        }
        return solapa;
    }

    /** Solapa con subrayado café cuando está elegida y, opcionalmente, un contador redondo. */
    public static class Solapa extends JToggleButton {
        private final String titulo;
        private final Estilo.Pildora contador = new Estilo.Pildora("", Estilo.CAFE, Color.WHITE,
                Estilo.fuente(Font.BOLD, 13f), -1);

        Solapa(String titulo) {
            this.titulo = titulo;
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setText(titulo);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(LEFT);
            setBorder(Estilo.relleno(10, 24, 16, 24));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("JButton.buttonType", "borderless");
            contador.setBorder(Estilo.relleno(2, 8, 2, 8));
            contador.setVisible(false);
            getModel().addChangeListener(e -> actualizarColores());
            actualizarColores();
        }

        /** Fondo del contador fijo; null = café si la solapa está elegida y gris si no. */
        private Color fondoFijo;

        /**
         * Muestra un contador al lado del título ("5", "2 bajos"); con texto null se oculta.
         * @param fondo color de fondo fijo, o null para que siga a la solapa (café elegida, gris si no)
         */
        public void setContador(String texto, Color fondo, Color color) {
            fondoFijo = fondo;
            contador.setForeground(color);
            contador.setText(texto == null ? "" : texto);
            contador.setVisible(texto != null);
            actualizarColores();
            // el contador va a la derecha del texto: se le deja lugar en el borde
            setBorder(Estilo.relleno(10, 24, 16, texto != null ? 24 + contador.getPreferredSize().width + 10 : 24));
            if (contador.getParent() == null) {
                add(contador);
            }
            revalidate();
            repaint();
        }

        private void actualizarColores() {
            setForeground(isSelected() ? Estilo.CAFE : Estilo.TEXTO_SUAVE);
            setFont(Estilo.fuente(isSelected() ? Font.BOLD : Font.PLAIN, 17f));
            contador.setFondo(fondoFijo != null ? fondoFijo : isSelected() ? Estilo.CAFE : Estilo.TEXTO_SUAVE);
        }

        @Override
        public void doLayout() {
            // el contador queda justo después del texto, centrado en alto con él
            if (contador.isVisible()) {
                java.awt.FontMetrics fm = getFontMetrics(Estilo.fuente(Font.BOLD, 17f));
                java.awt.Dimension d = contador.getPreferredSize();
                int x = getInsets().left + fm.stringWidth(titulo) + 10;
                int centro = getInsets().top + (getHeight() - getInsets().top - getInsets().bottom) / 2;
                contador.setBounds(x, centro - d.height / 2, d.width, d.height);
            }
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            // mismo ancho elegida o no (la negrita no tiene que mover a las demás)
            java.awt.FontMetrics fm = getFontMetrics(Estilo.fuente(Font.BOLD, 17f));
            java.awt.Insets i = getInsets();
            return new java.awt.Dimension(fm.stringWidth(titulo) + i.left + i.right + 2,
                    fm.getHeight() + i.top + i.bottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected()) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(Estilo.CAFE);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        }
    }
}
