package earthquake2;


import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.*;

public class ClienteTerremoto extends JFrame {

    private JComboBox<String> comboPeriodoTiempo;
    private JTextArea areaRespuesta;
    private JButton botonObtenerDatos;

    public ClienteTerremoto() {
        setTitle("Información sobre Terremotos");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el combo box y el botón
        String[] periodosTiempo = {"Última Hora", "Últimas 24 horas", "Última Semana"};
        comboPeriodoTiempo = new JComboBox<>(periodosTiempo);

        botonObtenerDatos = new JButton("Obtener datos de Terremotos");
        botonObtenerDatos.addActionListener((ActionEvent e) -> {
            getDatosTerremotos();
        });

        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Período de tiempo:"));
        panelSuperior.add(comboPeriodoTiempo);
        panelSuperior.add(botonObtenerDatos);

        // Crear el área de respuesta con fondo gris semitransparente
        areaRespuesta = new JTextArea();
        areaRespuesta.setLineWrap(true);
        areaRespuesta.setWrapStyleWord(true);
        areaRespuesta.setEditable(false);
        areaRespuesta.setBackground(new Color(192, 192, 192, 180));  // Fondo gris semitransparente
        JScrollPane panelDesplazable = new JScrollPane(areaRespuesta);
        panelDesplazable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Crear el panel de imagen de fondo
        MapaPanel panelImagen = new MapaPanel("placas-tectonicas.png");

        // Usar un JLayeredPane para superponer el área de texto sobre la imagen
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1000, 500));

        // Agregar el panel de imagen
        panelImagen.setBounds(0, 0, 1000, 500);
        layeredPane.add(panelImagen, JLayeredPane.DEFAULT_LAYER);

        // Agregar el panel de texto (área de respuesta)
        panelDesplazable.setBounds(50, 50, 900, 400);
        layeredPane.add(panelDesplazable, JLayeredPane.PALETTE_LAYER);

        // Establecer el layout general y agregar los paneles
        setLayout(new BorderLayout());
        add(panelSuperior, BorderLayout.NORTH);
        add(layeredPane, BorderLayout.CENTER);
    }

    // Clase para el panel que dibuja la imagen
    class MapaPanel extends JPanel {
        private Image mapa;

        public MapaPanel(String rutaImagen) {
            this.mapa = new ImageIcon(rutaImagen).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Obtener el tamaño del panel
            int width = getWidth();
            int height = getHeight();
            // Dibujar la imagen escalada para que ocupe todo el panel
            g.drawImage(mapa, 0, 0, width, height, this);
        }
    }

    private void getDatosTerremotos() {
        String periodo = (String) comboPeriodoTiempo.getSelectedItem();
        String urlString = "";

        switch (periodo) {
            case "Última Hora":
                urlString = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson";
                break;
            case "Últimas 24 horas":
                urlString = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
                break;
            case "Última Semana":
                urlString = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_week.geojson";
                break;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String lineaEntrada;
            StringBuilder contenido = new StringBuilder();

            while ((lineaEntrada = entrada.readLine()) != null) {
                contenido.append(lineaEntrada);
            }
            entrada.close();

            analizarYMostrarDatosTerremotos(contenido.toString());
        } catch (Exception e) {
            areaRespuesta.setText("Error: " + e.getMessage());
        }
    }

    private void analizarYMostrarDatosTerremotos(String datosJson) {
        JSONObject jsonObject = new JSONObject(datosJson);
        JSONArray features = jsonObject.getJSONArray("features");

        areaRespuesta.setText("");  // Limpiar el contenido anterior

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            double magnitud = properties.getDouble("mag");
            String lugar = properties.getString("place");
            long tiempo = properties.getLong("time");

            areaRespuesta.append("Magnitud: " + magnitud + "\n");
            areaRespuesta.append("Ubicación: " + lugar + "\n");
            areaRespuesta.append("Hora: " + new java.util.Date(tiempo) + "\n\n");
            URL url = getClass().getResource("/data/terremotos.json");

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteTerremoto app = new ClienteTerremoto();
            app.setVisible(true);
        });
    }
}
