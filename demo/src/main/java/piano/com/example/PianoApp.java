package piano.com.example;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jfugue.player.Player;
import java.util.List;
import java.util.ArrayList;

public class PianoApp extends JFrame implements ActionListener {
    private Player player;
    private JTextArea historial;
    private JLabel labelNota;
    private int octava = 4;
    private SecuenciaGrabada secuenciaGrabada;

    public PianoApp() {
        player = new Player();
        secuenciaGrabada = new SecuenciaGrabada(player);

        // Configurar la ventana principal
        setTitle("Teclado de Piano");
        setSize(720, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear componentes visuales
        labelNota = new JLabel("Nota: ");
        labelNota.setFont(new Font("Arial", Font.BOLD, 20));
        labelNota.setForeground(Color.WHITE);
        historial = new JTextArea(5, 30);
        historial.setEditable(false);
        historial.setFont(new Font("Arial", Font.PLAIN, 14));
        historial.setBackground(Color.DARK_GRAY);
        historial.setForeground(Color.WHITE);

        // Panel de botones del piano
        JPanel panelTeclas = new JPanel();
        panelTeclas.setLayout(null);
        panelTeclas.setBackground(Color.BLACK);
        String[] teclas = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        // Crear botones para cada tecla
        for (int i = 0; i < teclas.length; i++) {
            JButton boton = new JButton(teclas[i]);
            if (teclas[i].contains("#")) {
                boton.setBackground(Color.BLACK);  // Teclas sostenidas en negro
                boton.setForeground(Color.WHITE);
            } else {
                boton.setBackground(Color.WHITE);  // Teclas naturales en blanco
                boton.setForeground(Color.BLACK);
            }

            boton.setFont(new Font("Arial", Font.PLAIN, 20));
            boton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            boton.setBounds(i * 60, 0, 60, 250);
            boton.addActionListener(this);
            panelTeclas.add(boton);
        }

        // Agregar paneles al JFrame
        add(labelNota, BorderLayout.NORTH);
        add(new JScrollPane(historial), BorderLayout.SOUTH);
        add(panelTeclas, BorderLayout.CENTER);

        // Controles adicionales
        JPanel controlesPanel = new JPanel();
        controlesPanel.setLayout(new FlowLayout());
        controlesPanel.setBackground(Color.DARK_GRAY);

        // Agregar botones de controles
        JButton btnOctavaMas = new JButton("Octava +");
        JButton btnOctavaMenos = new JButton("Octava -");
        JButton btnGrabar = new JButton("Grabar");
        JButton btnParar = new JButton("Parar");
        JButton btnReproducir = new JButton("Reproducir");
        JButton btnLimpiarHistorial = new JButton("Limpiar Historial");

        btnOctavaMas.addActionListener(e -> cambiarOctava(true));
        btnOctavaMenos.addActionListener(e -> cambiarOctava(false));
        btnGrabar.addActionListener(e -> comenzarGrabacion());
        btnParar.addActionListener(e -> detenerGrabacion());
        btnReproducir.addActionListener(e -> reproducirSecuencia());
        btnLimpiarHistorial.addActionListener(e -> historial.setText(""));

        controlesPanel.add(btnOctavaMas);
        controlesPanel.add(btnOctavaMenos);
        controlesPanel.add(btnGrabar);
        controlesPanel.add(btnParar);
        controlesPanel.add(btnReproducir);
        controlesPanel.add(btnLimpiarHistorial);

        add(controlesPanel, BorderLayout.NORTH);

        // Asegurar que el panel tenga foco para escuchar teclas
        panelTeclas.setFocusable(true);
        panelTeclas.requestFocusInWindow();  // Darle el foco al panel para que escuche las teclas

        // KeyListener agregado al JFrame para escuchar teclas globalmente
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = e.getKeyChar();
                String nota = keyToNota(keyChar); // Convertir tecla a nota
                if (nota != null) {
                    // Simular clic del botón para la tecla presionada
                    playNote(nota);  // Call the playNote method to handle both actions (click and key press)
                }
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String nota = e.getActionCommand();
        playNote(nota);  // Call the playNote method to handle both actions (click and key press)
    }

    private void playNote(String nota) {
        labelNota.setText("Nota: " + nota + octava);
        historial.append(nota + " a las " + System.currentTimeMillis() + " ms\n");
        player.play(nota + octava);
        secuenciaGrabada.grabarNota(nota + octava);

        // Efecto de iluminación: cambio temporal de borde para resaltar la tecla
        JButton boton = (JButton) getSourceButton(nota);  // Retrieve the button for the corresponding note
        if (boton != null) {
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 102), 3),
                    BorderFactory.createLineBorder(Color.BLACK, 1)));
            Timer timer = new Timer(200, ev -> boton.setBorder(BorderFactory.createLineBorder(Color.BLACK)));
            timer.setRepeats(false);
            timer.start();
        }
    }

    // Retrieve the button for the given note
    private JButton getSourceButton(String nota) {
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component buttonComp : panel.getComponents()) {
                    if (buttonComp instanceof JButton) {
                        JButton boton = (JButton) buttonComp;
                        if (boton.getText().equals(nota)) {
                            return boton;
                        }
                    }
                }
            }
        }
        return null;
    }

    // Cambiar la octava de las notas
    private void cambiarOctava(boolean subir) {
        octava = subir ? octava + 1 : octava - 1;
        labelNota.setText("Octava: " + octava);
    }

    // Comenzar la grabación de la secuencia
    private void comenzarGrabacion() {
        secuenciaGrabada.limpiarSecuencia();
        historial.append("Grabando...\n");
    }

    // Detener la grabación de la secuencia
    private void detenerGrabacion() {
        historial.append("Grabación detenida.\n");
    }

    // Reproducir la secuencia grabada
    private void reproducirSecuencia() {
        try {
            secuenciaGrabada.reproducirSecuencia();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Convertir una tecla presionada a la nota correspondiente
    private String keyToNota(char key) {
        switch (key) {
            case 'a': return "C";
            case 'w': return "C#";
            case 's': return "D";
            case 'e': return "D#";
            case 'd': return "E";
            case 'f': return "F";
            case 't': return "F#";
            case 'g': return "G";
            case 'y': return "G#";
            case 'h': return "A";
            case 'u': return "A#";
            case 'j': return "B";
            default: return null; // No mapeo para otras teclas
        }
    }

    // Clase para gestionar la grabación de la secuencia
    public static class SecuenciaGrabada {
        private List<String> notas;
        private List<Long> tiempos;
        private Player player;

        public SecuenciaGrabada(Player player) {
            this.player = player;
            notas = new ArrayList<>();
            tiempos = new ArrayList<>();
        }

        public void grabarNota(String nota) {
            notas.add(nota);
            tiempos.add(System.currentTimeMillis());
        }

        public void limpiarSecuencia() {
            notas.clear();
            tiempos.clear();
        }

        public void reproducirSecuencia() throws InterruptedException {
            for (int i = 0; i < notas.size(); i++) {
                String nota = notas.get(i);
                long tiempo = tiempos.get(i);
                long espera = i == 0 ? 0 : tiempo - tiempos.get(i - 1);
                Thread.sleep(espera);
                player.play(nota);
            }
        }
    }

    public static void main(String[] args) {
        new PianoApp();
    }
}
