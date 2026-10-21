package org.example.escritorio.vista;

import org.example.escritorio.controlador.LoginControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

/** Pantalla de inicio de sesión del personal: el logo a la izquierda y el formulario a la derecha. */
public class LoginFrame extends JFrame {

    private static final int ANCHO_FORMULARIO = 420;
    private static final String ESTILO_CAMPO = "arc: 16; focusWidth: 4; innerFocusWidth: 0;"
            + " focusColor: #EFE7DF; borderColor: #E0D6CC; focusedBorderColor: #8C5E35;"
            + " margin: 12,14,12,14; iconTextGap: 12";

    private final JTextField txtUsuario = new JTextField(22);
    private final JPasswordField txtClave = new JPasswordField(22);
    private final JButton btnIngresar = new Boton("Ingresar", Boton.Tipo.PRIMARIO);
    private final LoginControlador controlador;
    private boolean mostrarPass = false;

    public LoginFrame() {
        super("La Nona - Iniciar sesión");
        controlador = new LoginControlador(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        txtClave.getDocument().addDocumentListener(alEscribir);

        btnIngresar.addActionListener(e -> controlador.ingresar(txtUsuario.getText(), txtClave.getPassword()));

        setSize(1100, 690);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JPanel armarContenido() {
        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(armarMarca());
        raiz.add(armarFormulario());
        return raiz;
    }

    /** Mitad café: "La Nona", "pastas caseras", el rodillo y abajo de todo para quién es el sistema. */
    private JComponent armarMarca() {
        JLabel nombre = new JLabel("La Nona", SwingConstants.CENTER);
        nombre.setFont(Estilo.fuenteLogo(76f));
        nombre.setForeground(Color.WHITE);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bajada = new JLabel("pastas caseras", SwingConstants.CENTER);
        bajada.setFont(Estilo.fuenteSerif(30f));
        bajada.setForeground(Color.WHITE);
        bajada.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logo = Estilo.transparente(null);
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.add(nombre);
        logo.add(bajada);
        logo.add(Box.createVerticalStrut(28));
        logo.add(new Estilo.Rodillo(170, 116, 16));

        JLabel pie = new JLabel("Sistema interno del personal", SwingConstants.CENTER);
        pie.setFont(Estilo.fuente(Font.PLAIN, 15f));
        pie.setForeground(new Color(255, 255, 255, 215));

        JPanel centrado = Estilo.transparente(new GridBagLayout());
        centrado.add(logo);

        JPanel marca = new JPanel(new BorderLayout());
        marca.setBackground(Estilo.CAFE);
        marca.setBorder(Estilo.relleno(24, 24, 36, 24));
        marca.add(centrado, BorderLayout.CENTER);
        marca.add(pie, BorderLayout.SOUTH);
        return marca;
    }

    /** Mitad blanca con el formulario centrado. */
    private JComponent armarFormulario() {
        JLabel titulo = new JLabel("Iniciar sesión");
        titulo.setFont(Estilo.fuente(Font.BOLD, 34f));
        titulo.setForeground(Estilo.TEXTO);

        JLabel subtitulo = new JLabel("Ingresá con tu usuario del restaurante");
        subtitulo.setFont(Estilo.fuente(Font.PLAIN, 17f));
        subtitulo.setForeground(Estilo.TEXTO_SUAVE);

        prepararCampo(txtUsuario, "Ingresá tu usuario", Icono.Tipo.USUARIO);
        prepararCampo(txtClave, "Ingresá tu contraseña", Icono.Tipo.CANDADO);
        // el ojito va en un panel con margen para que no quede pegado al borde del campo
        JPanel conMargen = Estilo.transparente(new BorderLayout());
        conMargen.setBorder(Estilo.relleno(0, 0, 0, 8));
        conMargen.add(armarOjo());
        txtClave.putClientProperty("JTextField.trailingComponent", conMargen);

        btnIngresar.setFont(Estilo.fuente(Font.BOLD, 19f));
        btnIngresar.setBorder(Estilo.relleno(16, 22, 16, 22));
        ((Boton) btnIngresar).conArco(18);

        JLabel ayuda = new JLabel("<html><body style='width:" + (ANCHO_FORMULARIO - 80) + "px'><center>¿Olvidaste tu contraseña? "
                + "Pedile a Administración que la restablezca.</center></body></html>", SwingConstants.CENTER);
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 15f));
        ayuda.setForeground(Estilo.TEXTO_SUAVE);

        JPanel formulario = Estilo.transparente(null);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        agregar(formulario, titulo, 6);
        agregar(formulario, subtitulo, 30);
        agregar(formulario, etiqueta("Usuario"), 8);
        agregar(formulario, txtUsuario, 22);
        agregar(formulario, etiqueta("Contraseña"), 8);
        agregar(formulario, txtClave, 30);
        agregar(formulario, btnIngresar, 24);
        agregar(formulario, ayuda, 0);
        formulario.setPreferredSize(new Dimension(ANCHO_FORMULARIO, formulario.getPreferredSize().height));

        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(Estilo.BLANCO);
        lado.add(formulario);
        return lado;
    }

    private static void agregar(JPanel panel, JComponent componente, int espacioDespues) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(ANCHO_FORMULARIO, componente.getPreferredSize().height));
        panel.add(componente);
        if (espacioDespues > 0) {
            panel.add(Box.createVerticalStrut(espacioDespues));
        }
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuente(Font.BOLD, 16f));
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static void prepararCampo(JTextField campo, String placeholder, Icono.Tipo icono) {
        campo.setFont(Estilo.fuente(Font.PLAIN, 17f));
        campo.setForeground(Estilo.TEXTO);
        campo.putClientProperty("JTextField.placeholderText", placeholder);
        campo.putClientProperty("JTextField.leadingIcon", new Icono(icono, 22, Estilo.CAFE));
        Estilo.flat(campo, ESTILO_CAMPO);
    }

    /** Botón del ojito: muestra u oculta la contraseña. */
    private JButton armarOjo() {
        char puntito = txtClave.getEchoChar();
        JButton ojo = new JButton(new Icono(Icono.Tipo.OJO, 22, Estilo.TEXTO_SUAVE));
        ojo.setContentAreaFilled(false);
        ojo.setBorderPainted(false);
        ojo.setFocusPainted(false);
        ojo.setFocusable(false);
        ojo.setOpaque(false);
        ojo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ojo.setToolTipText("Mostrar contraseña");
        ojo.putClientProperty("JButton.buttonType", "toolBarButton");
        ojo.addActionListener(e -> {
            mostrarPass = !mostrarPass;
            txtClave.setEchoChar(mostrarPass ? (char) 0 : puntito);
            ojo.setIcon(new Icono(mostrarPass ? Icono.Tipo.OJO_TACHADO : Icono.Tipo.OJO, 22, Estilo.TEXTO_SUAVE));
            ojo.setToolTipText(mostrarPass ? "Ocultar contraseña" : "Mostrar contraseña");
        });
        return ojo;
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
}
