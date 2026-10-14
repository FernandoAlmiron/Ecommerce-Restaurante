package org.example.escritorio.vista;

import org.example.escritorio.controlador.LoginControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Pantalla de inicio de sesión del personal. */
public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = new JTextField(22);
    private final JPasswordField txtClave = new JPasswordField(22);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final LoginControlador controlador;
    private static final Color GRIS_PLACEHOLDER = new Color(185, 185, 185);
    private static final Color NEGRO_PLACEHOLDER = new Color(0, 0, 0);
    private boolean mostrarPass = false;

    public LoginFrame() {
        super("Restaurante - Iniciar sesión");
        controlador = new LoginControlador(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(armarContenido());

        // Enter en cualquier campo equivale a apretar Ingresar
        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.setEnabled(false);
        DocumentListener alEscribir = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarBoton(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarBoton(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarBoton(); }
        };
        txtUsuario.getDocument().addDocumentListener(alEscribir);
        String placeholderUsuario = "Ingrese su nombre del usuario";
        txtUsuario.setText(placeholderUsuario);
        txtUsuario.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUsuario.getText().equals(placeholderUsuario)) {
                    txtUsuario.setForeground(NEGRO_PLACEHOLDER);
                    txtUsuario.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtUsuario.getText().isEmpty()) {
                    txtUsuario.setText(placeholderUsuario);
                    txtUsuario.setForeground(GRIS_PLACEHOLDER);
                }
            }
        });
        txtClave.getDocument().addDocumentListener(alEscribir);
        String placeholderPass = "Ingrese su contraseña";
        char puntito = txtClave.getEchoChar();

        txtClave.setText(placeholderPass);
        txtClave.setForeground(GRIS_PLACEHOLDER);
        txtClave.setEchoChar((char) 0);

        txtClave.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(txtClave.getPassword()).equals(placeholderPass)) {
                    txtClave.setText("");
                    txtClave.setForeground(NEGRO_PLACEHOLDER);
                    txtClave.setEchoChar(mostrarPass ? (char) 0 : puntito);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtClave.getPassword().length == 0) {
                    txtClave.setText(placeholderPass);
                    txtClave.setForeground(GRIS_PLACEHOLDER);
                    txtClave.setEchoChar((char) 0);
                }
            }
        });

        // ===== OJITO PARA VER LA CONTRASEÑA =====
        IconoOjo icono = new IconoOjo();
        JButton ojo = new JButton(icono);
        ojo.setContentAreaFilled(false);
        ojo.setBorderPainted(false);
        ojo.setFocusPainted(false);
        ojo.setFocusable(false);
        ojo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ojo.addActionListener(e -> {
            mostrarPass = !mostrarPass;
            icono.tachado = mostrarPass;
            if (!new String(txtClave.getPassword()).equals(placeholderPass)) {
                txtClave.setEchoChar(mostrarPass ? (char) 0 : puntito);
            }
            ojo.repaint();
        });

        txtClave.add(ojo);
        txtClave.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ojo.setBounds(txtClave.getWidth() - 30, 2, 28, txtClave.getHeight() - 4);
            }
        });
        txtClave.setBorder(BorderFactory.createCompoundBorder(
                txtClave.getBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, 26)));

        btnIngresar.addActionListener(e -> controlador.ingresar(txtUsuario.getText(), txtClave.getPassword()));

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel armarContenido() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel titulo = new JLabel("Restaurante", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(titulo, c);

        JLabel subtitulo = new JLabel("Sistema interno del personal", SwingConstants.CENTER);
        c.insets = new Insets(0, 0, 20, 0);
        panel.add(subtitulo, c);

        c.insets = new Insets(0, 0, 4, 0);
        panel.add(new JLabel("Usuario"), c);
        c.insets = new Insets(0, 0, 12, 0);
        panel.add(txtUsuario, c);

        c.insets = new Insets(0, 0, 4, 0);
        panel.add(new JLabel("Contraseña"), c);
        c.insets = new Insets(0, 0, 20, 0);
        panel.add(txtClave, c);

        c.insets = new Insets(0, 0, 0, 0);
        panel.add(btnIngresar, c);

        return panel;
    }

    private void actualizarBoton() {
        btnIngresar.setEnabled(!txtUsuario.getText().isBlank() && txtClave.getDocument().getLength() > 0);
    }

    public JButton getBotonIngresar() {
        return btnIngresar;
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void limpiarClave() {
        txtClave.setText("");
        txtClave.requestFocusInWindow();
    }

    public void irAPantallaPrincipal() {
        dispose();
        new PrincipalFrame().setVisible(true);
    }

    // ===== ÍCONO DEL OJO (tachado = contraseña visible) =====
    private static class IconoOjo implements Icon {
        boolean tachado = false;

        @Override public int getIconWidth()  { return 20; }
        @Override public int getIconHeight() { return 14; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(120, 120, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x + 1, y + 2, 18, 10);
            g2.fillOval(x + 7, y + 4, 6, 6);
            if (tachado) {
                g2.drawLine(x + 2, y + 13, x + 18, y + 1);
            }
            g2.dispose();
        }
    }
}