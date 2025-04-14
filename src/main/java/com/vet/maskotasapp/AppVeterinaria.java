package com.vet.maskotasapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.property.SimpleStringProperty;

public class AppVeterinaria extends Application {
    private Connection conexion;

    // Modelos de datos para TableViews
    public static class Contacto {
        private final int id;
        private final String tipo;
        private final String nombre;
        private final String direccion;
        private final String telefono;
        private final String email;
        private final String fechaNacimiento;
        private final String observaciones;

        public Contacto(int id, String tipo, String nombre, String direccion, String telefono, String email, String fechaNacimiento, String observaciones) {
            this.id = id;
            this.tipo = tipo;
            this.nombre = nombre;
            this.direccion = direccion;
            this.telefono = telefono;
            this.email = email;
            this.fechaNacimiento = fechaNacimiento;
            this.observaciones = observaciones;
        }

        public int getId() { return id; }
        public String getTipo() { return tipo; }
        public String getNombre() { return nombre; }
        public String getDireccion() { return direccion; }
        public String getTelefono() { return telefono; }
        public String getEmail() { return email; }
        public String getFechaNacimiento() { return fechaNacimiento; }
        public String getObservaciones() { return observaciones; }
    }

    public static class Asistencia {
        private final int id;
        private final String empleado;
        private final String fecha;
        private final String horaEntrada;
        private final String horaSalida;

        public Asistencia(int id, String empleado, String fecha, String horaEntrada, String horaSalida) {
            this.id = id;
            this.empleado = empleado;
            this.fecha = fecha;
            this.horaEntrada = horaEntrada;
            this.horaSalida = horaSalida;
        }

        public int getId() { return id; }
        public String getEmpleado() { return empleado; }
        public String getFecha() { return fecha; }
        public String getHoraEntrada() { return horaEntrada; }
        public String getHoraSalida() { return horaSalida; }
    }

    public static class Tarea {
        private final int id;
        private final String tarea;
        private final String asignadoA;
        private final String prioridad;
        private final String estado;
        private final String fechaLimite;

        public Tarea(int id, String tarea, String asignadoA, String prioridad, String estado, String fechaLimite) {
            this.id = id;
            this.tarea = tarea;
            this.asignadoA = asignadoA;
            this.prioridad = prioridad;
            this.estado = estado;
            this.fechaLimite = fechaLimite;
        }

        public int getId() { return id; }
        public String getTarea() { return tarea; }
        public String getAsignadoA() { return asignadoA; }
        public String getPrioridad() { return prioridad; }
        public String getEstado() { return estado; }
        public String getFechaLimite() { return fechaLimite; }
    }

    public static class Inventario {
        private final int id;
        private final String producto;
        private final int cantidad;

        public Inventario(int id, String producto, int cantidad) {
            this.id = id;
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public int getId() { return id; }
        public String getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ventanaPrincipal) {
        conectarBD();

        // Encabezado
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #007bff;");
        Label title = new Label("Veterinaria Mask!otas");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-family: 'Arial';");
        header.getChildren().add(title);
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/com/vet/maskotasapp/logo.png")));
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);
        header.getChildren().add(logo);

        // Diseño principal
        VBox raiz = new VBox(10);
        raiz.setPadding(new Insets(20));
        raiz.setStyle("-fx-background-color: #f8f9fa;");

        TabPane panelPestanas = new TabPane();
        panelPestanas.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6;");

        // Vincular el ancho del TabPane al ancho del contenedor raiz
        panelPestanas.prefWidthProperty().bind(raiz.widthProperty().subtract(40)); // Restar el padding de raiz

        Tab pestanaContactos = new Tab("Agenda Contactos", crearPestanaContactos());
        Tab pestanaAsistencia = new Tab("Control Asistencia", crearPestanaAsistencia());
        Tab pestanaTareas = new Tab("Organizador Tareas", crearPestanaTareas());
        Tab pestanaQR = new Tab("Códigos QR", crearPestanaQR());
        Tab pestanaInventario = new Tab("Control Inventario", crearPestanaInventario());

        pestanaContactos.setClosable(false);
        pestanaAsistencia.setClosable(false);
        pestanaTareas.setClosable(false);
        pestanaQR.setClosable(false);
        pestanaInventario.setClosable(false);

        panelPestanas.getTabs().addAll(pestanaContactos, pestanaAsistencia, pestanaTareas, pestanaQR, pestanaInventario);

        raiz.getChildren().addAll(header, panelPestanas);

        Scene escena = new Scene(raiz, 800, 600);
        escena.getStylesheets().add(getClass().getResource("/com/vet/maskotasapp/styles.css").toExternalForm());
        ventanaPrincipal.setTitle("Veterinaria Mask!otas");
        ventanaPrincipal.setScene(escena);
        ventanaPrincipal.show();
    }

    private void conectarBD() {
        try {
            conexion = DriverManager.getConnection("jdbc:sqlite:veterinaria.db");
            Statement sentencia = conexion.createStatement();
            sentencia.execute("CREATE TABLE IF NOT EXISTS contactos (" +
                "id INTEGER PRIMARY KEY, tipo TEXT, nombre TEXT, direccion TEXT, telefono TEXT, email TEXT, fecha_nacimiento TEXT, observaciones TEXT)");
            sentencia.execute("CREATE TABLE IF NOT EXISTS asistencia (" +
                "id INTEGER PRIMARY KEY, empleado TEXT, fecha TEXT, hora_entrada TEXT, hora_salida TEXT)");
            sentencia.execute("CREATE TABLE IF NOT EXISTS tareas (" +
                "id INTEGER PRIMARY KEY, tarea TEXT, asignado_a TEXT, prioridad TEXT, estado TEXT, fecha_limite TEXT)");
            sentencia.execute("CREATE TABLE IF NOT EXISTS inventario (" +
                "id INTEGER PRIMARY KEY, producto TEXT, cantidad INTEGER)");
        } catch (SQLException e) {
            System.out.println("Error en la base de datos: " + e.getMessage());
        }
    }

    private VBox crearPestanaContactos() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Campos de entrada
        ComboBox<String> campoTipo = new ComboBox<>();
        campoTipo.getItems().addAll("Cliente", "Empleado", "Proveedor");
        campoTipo.setPromptText("Tipo de Contacto");
        TextField campoNombre = new TextField();
        campoNombre.setPromptText("Nombre");
        TextField campoDireccion = new TextField();
        campoDireccion.setPromptText("Dirección");
        TextField campoTelefono = new TextField();
        campoTelefono.setPromptText("Teléfono");
        TextField campoEmail = new TextField();
        campoEmail.setPromptText("Email");
        TextField campoFechaNacimiento = new TextField();
        campoFechaNacimiento.setPromptText("Fecha Nacimiento (AAAA-MM-DD)");
        TextField campoObservaciones = new TextField();
        campoObservaciones.setPromptText("Observaciones");
        Button botonAgregar = new Button("Añadir Contacto");
        Button botonRefrescar = new Button("Refrescar Lista");

        // Tabla
        TableView<Contacto> tablaContactos = new TableView<>();
        TableColumn<Contacto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Contacto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<Contacto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Contacto, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        TableColumn<Contacto, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        TableColumn<Contacto, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Contacto, String> colFechaNacimiento = new TableColumn<>("Fecha Nacimiento");
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        TableColumn<Contacto, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        tablaContactos.getColumns().addAll(colId, colTipo, colNombre, colDireccion, colTelefono, colEmail, colFechaNacimiento, colObservaciones);
        tablaContactos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos iniciales
        tablaContactos.setItems(cargarContactos());

        // Añadir contacto
        botonAgregar.setOnAction(e -> {
            try {
                String tipo = campoTipo.getValue();
                String nombre = campoNombre.getText().trim();
                if (tipo == null || nombre.isEmpty()) {
                    System.out.println("Error: Seleccione tipo y complete nombre");
                    return;
                }
                PreparedStatement pstmt = conexion.prepareStatement(
                    "INSERT INTO contactos (tipo, nombre, direccion, telefono, email, fecha_nacimiento, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, tipo);
                pstmt.setString(2, nombre);
                pstmt.setString(3, campoDireccion.getText().trim());
                pstmt.setString(4, campoTelefono.getText().trim());
                pstmt.setString(5, campoEmail.getText().trim());
                pstmt.setString(6, campoFechaNacimiento.getText().trim());
                pstmt.setString(7, campoObservaciones.getText().trim());
                pstmt.executeUpdate();
                campoTipo.setValue(null);
                campoNombre.clear();
                campoDireccion.clear();
                campoTelefono.clear();
                campoEmail.clear();
                campoFechaNacimiento.clear();
                campoObservaciones.clear();
                tablaContactos.setItems(cargarContactos());
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Diseño de botones horizontal
        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(botonAgregar, botonRefrescar);

        // Añadir componentes al contenedor
        contenedor.getChildren().addAll(campoTipo, campoNombre, campoDireccion, campoTelefono, campoEmail, campoFechaNacimiento, campoObservaciones, buttonContainer, tablaContactos);

        return contenedor;
    }

    private ObservableList<Contacto> cargarContactos() {
        ObservableList<Contacto> contactos = FXCollections.observableArrayList();
        try {
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM contactos");
            while (rs.next()) {
                contactos.add(new Contacto(
                    rs.getInt("id"),
                    rs.getString("tipo"),
                    rs.getString("nombre"),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("fecha_nacimiento"),
                    rs.getString("observaciones")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar contactos: " + e.getMessage());
        }
        return contactos;
    }

    private VBox crearPestanaAsistencia() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Campos de entrada
        TextField campoEmpleado = new TextField();
        campoEmpleado.setPromptText("Empleado");
        TextField campoFecha = new TextField();
        campoFecha.setPromptText("Fecha (AAAA-MM-DD)");
        Button botonEntrada = new Button("Registrar Entrada");
        Button botonSalida = new Button("Registrar Salida");
        Button botonRefrescar = new Button("Refrescar Lista");

        // Tabla
        TableView<Asistencia> tablaAsistencia = new TableView<>();
        TableColumn<Asistencia, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Asistencia, String> colEmpleado = new TableColumn<>("Empleado");
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("empleado"));
        TableColumn<Asistencia, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        TableColumn<Asistencia, String> colHoraEntrada = new TableColumn<>("Hora Entrada");
        colHoraEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        TableColumn<Asistencia, String> colHoraSalida = new TableColumn<>("Hora Salida");
        colHoraSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        tablaAsistencia.getColumns().addAll(colId, colEmpleado, colFecha, colHoraEntrada, colHoraSalida);
        tablaAsistencia.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos iniciales
        tablaAsistencia.setItems(cargarAsistencia());

        // Registrar entrada
        botonEntrada.setOnAction(e -> {
            try {
                String empleado = campoEmpleado.getText().trim();
                String fecha = campoFecha.getText().trim();
                if (empleado.isEmpty() || fecha.isEmpty()) {
                    System.out.println("Error: Complete empleado y fecha");
                    return;
                }

                // Verificar si ya existe una entrada
                PreparedStatement checkStmt = conexion.prepareStatement(
                    "SELECT id FROM asistencia WHERE empleado = ? AND fecha = ? AND hora_entrada IS NOT NULL");
                checkStmt.setString(1, empleado);
                checkStmt.setString(2, fecha);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Error: Ya existe una entrada para este empleado y fecha");
                    return;
                }

                String horaEntrada = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                PreparedStatement pstmt = conexion.prepareStatement(
                    "INSERT INTO asistencia (empleado, fecha, hora_entrada) VALUES (?, ?, ?)");
                pstmt.setString(1, empleado);
                pstmt.setString(2, fecha);
                pstmt.setString(3, horaEntrada);
                pstmt.executeUpdate();

                campoEmpleado.clear();
                campoFecha.clear();
                tablaAsistencia.setItems(cargarAsistencia());
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Registrar salida
        botonSalida.setOnAction(e -> {
            try {
                String empleado = campoEmpleado.getText().trim();
                String fecha = campoFecha.getText().trim();
                if (empleado.isEmpty() || fecha.isEmpty()) {
                    System.out.println("Error: Complete empleado y fecha");
                    return;
                }

                String horaSalida = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                PreparedStatement pstmt = conexion.prepareStatement(
                    "UPDATE asistencia SET hora_salida = ? WHERE empleado = ? AND fecha = ? AND hora_entrada IS NOT NULL AND hora_salida IS NULL");
                pstmt.setString(1, horaSalida);
                pstmt.setString(2, empleado);
                pstmt.setString(3, fecha);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    System.out.println("Error: No se encontró entrada sin salida para este empleado y fecha");
                    return;
                }

                campoEmpleado.clear();
                campoFecha.clear();
                tablaAsistencia.setItems(cargarAsistencia());
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Actualizar tabla
        botonRefrescar.setOnAction(e -> tablaAsistencia.setItems(cargarAsistencia()));

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(botonEntrada, botonSalida, botonRefrescar);
        contenedor.getChildren().addAll(campoEmpleado, campoFecha, buttonContainer, tablaAsistencia);
        return contenedor;
    }

    private ObservableList<Asistencia> cargarAsistencia() {
        ObservableList<Asistencia> asistencias = FXCollections.observableArrayList();
        try {
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM asistencia");
            while (rs.next()) {
                asistencias.add(new Asistencia(
                    rs.getInt("id"),
                    rs.getString("empleado"),
                    rs.getString("fecha"),
                    rs.getString("hora_entrada"),
                    rs.getString("hora_salida")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar asistencia: " + e.getMessage());
        }
        return asistencias;
    }

    private VBox crearPestanaTareas() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Campos de entrada
        TextField campoTarea = new TextField();
        campoTarea.setPromptText("Tarea");
        TextField campoAsignadoA = new TextField();
        campoAsignadoA.setPromptText("Asignado a (Empleado/Grupo)");
        ComboBox<String> campoPrioridad = new ComboBox<>();
        campoPrioridad.getItems().addAll("Alta", "Media", "Baja");
        campoPrioridad.setPromptText("Prioridad");
        TextField campoFechaLimite = new TextField();
        campoFechaLimite.setPromptText("Fecha Límite (AAAA-MM-DD)");
        Button botonAgregarTarea = new Button("Añadir Tarea");
        Button botonCompletar = new Button("Marcar Completada");
        Button botonRefrescar = new Button("Refrescar Lista");

        // Tabla
        TableView<Tarea> tablaTareas = new TableView<>();
        TableColumn<Tarea, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Tarea, String> colTarea = new TableColumn<>("Tarea");
        colTarea.setCellValueFactory(new PropertyValueFactory<>("tarea"));
        TableColumn<Tarea, String> colAsignadoA = new TableColumn<>("Asignado a");
        colAsignadoA.setCellValueFactory(new PropertyValueFactory<>("asignadoA"));
        TableColumn<Tarea, String> colPrioridad = new TableColumn<>("Prioridad");
        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        TableColumn<Tarea, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        TableColumn<Tarea, String> colFechaLimite = new TableColumn<>("Fecha Límite");
        colFechaLimite.setCellValueFactory(new PropertyValueFactory<>("fechaLimite"));
        tablaTareas.getColumns().addAll(colId, colTarea, colAsignadoA, colPrioridad, colEstado, colFechaLimite);
        tablaTareas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos iniciales
        tablaTareas.setItems(cargarTareas());

        // Añadir tarea
        botonAgregarTarea.setOnAction(e -> {
            try {
                String tarea = campoTarea.getText().trim();
                String asignadoA = campoAsignadoA.getText().trim();
                String prioridad = campoPrioridad.getValue();
                if (tarea.isEmpty() || asignadoA.isEmpty() || prioridad == null) {
                    System.out.println("Error: Complete tarea, asignado a y prioridad");
                    return;
                }
                PreparedStatement pstmt = conexion.prepareStatement(
                    "INSERT INTO tareas (tarea, asignado_a, prioridad, estado, fecha_limite) VALUES (?, ?, ?, ?, ?)");
                pstmt.setString(1, tarea);
                pstmt.setString(2, asignadoA);
                pstmt.setString(3, prioridad);
                pstmt.setString(4, "Pendiente");
                pstmt.setString(5, campoFechaLimite.getText().trim());
                pstmt.executeUpdate();
                campoTarea.clear();
                campoAsignadoA.clear();
                campoPrioridad.setValue(null);
                campoFechaLimite.clear();
                tablaTareas.setItems(cargarTareas());
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Completar tarea
        botonCompletar.setOnAction(e -> {
            Tarea tareaSeleccionada = tablaTareas.getSelectionModel().getSelectedItem();
            if (tareaSeleccionada == null) {
                System.out.println("Error: Seleccione una tarea");
                return;
            }
            try {
                PreparedStatement pstmt = conexion.prepareStatement("UPDATE tareas SET estado = ? WHERE id = ?");
                pstmt.setString(1, "Completada");
                pstmt.setInt(2, tareaSeleccionada.getId());
                pstmt.executeUpdate();
                tablaTareas.setItems(cargarTareas());
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Actualizar tabla
        botonRefrescar.setOnAction(e -> tablaTareas.setItems(cargarTareas()));

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(botonAgregarTarea, botonCompletar, botonRefrescar);
        contenedor.getChildren().addAll(campoTarea, campoAsignadoA, campoPrioridad, campoFechaLimite, buttonContainer, tablaTareas);
        return contenedor;
    }

    private ObservableList<Tarea> cargarTareas() {
        ObservableList<Tarea> tareas = FXCollections.observableArrayList();
        try {
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tareas");
            while (rs.next()) {
                tareas.add(new Tarea(
                    rs.getInt("id"),
                    rs.getString("tarea"),
                    rs.getString("asignado_a"),
                    rs.getString("prioridad"),
                    rs.getString("estado"),
                    rs.getString("fecha_limite")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar tareas: " + e.getMessage());
        }
        return tareas;
    }

    private VBox crearPestanaQR() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        TextField campoMedicamento = new TextField();
        campoMedicamento.setPromptText("Medicamento/Vacuna");
        TextField campoDosis = new TextField();
        campoDosis.setPromptText("Dosis");
        Button botonGenerar = new Button("Generar QR");

        // Tabla para mostrar códigos QR generados
        TableView<File> tablaQR = new TableView<>();
        TableColumn<File, String> colNombre = new TableColumn<>("Nombre Archivo");
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        tablaQR.getColumns().add(colNombre);
        tablaQR.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar archivos QR generados
        ObservableList<File> archivosQR = FXCollections.observableArrayList();
        File directorio = new File(".");
        for (File archivo : directorio.listFiles((dir, nombre) -> nombre.startsWith("qr_") && nombre.endsWith(".png"))) {
            archivosQR.add(archivo);
        }
        tablaQR.setItems(archivosQR);

        // Generar código QR
        botonGenerar.setOnAction(e -> {
            String datos = "Medicamento: " + campoMedicamento.getText() + ", Dosis: " + campoDosis.getText();
            try {
                QRCodeWriter escritorQR = new QRCodeWriter();
                com.google.zxing.common.BitMatrix matriz = escritorQR.encode(datos, BarcodeFormat.QR_CODE, 200, 200);
                BufferedImage imagenQR = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < 200; x++) {
                    for (int y = 0; y < 200; y++) {
                        imagenQR.setRGB(x, y, matriz.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    }
                }
                String nombreArchivo = "qr_" + campoMedicamento.getText() + ".png";
                ImageIO.write(imagenQR, "png", new File(nombreArchivo));
                archivosQR.add(new File(nombreArchivo));
                tablaQR.refresh();
                campoMedicamento.clear();
                campoDosis.clear();
            } catch (WriterException | java.io.IOException ex) {
                System.out.println("Error al generar QR: " + ex.getMessage());
            }
        });

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().add(botonGenerar);
        contenedor.getChildren().addAll(campoMedicamento, campoDosis, buttonContainer, tablaQR);
        return contenedor;
    }

    private VBox crearPestanaInventario() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));

        // Campos de entrada
        TextField campoProducto = new TextField();
        campoProducto.setPromptText("Producto");
        TextField campoCantidad = new TextField();
        campoCantidad.setPromptText("Cantidad (positiva: compra, negativa: venta)");
        TextField campoStockMinimo = new TextField();
        campoStockMinimo.setPromptText("Stock Mínimo");
        Button botonActualizar = new Button("Actualizar Inventario");
        Button botonRefrescar = new Button("Refrescar Lista");

        // Tabla
        TableView<Inventario> tablaInventario = new TableView<>();
        TableColumn<Inventario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Inventario, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        TableColumn<Inventario, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        tablaInventario.getColumns().addAll(colId, colProducto, colCantidad);
        tablaInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Cargar datos iniciales
        tablaInventario.setItems(cargarInventario());

        // Actualizar inventario
        botonActualizar.setOnAction(e -> {
            try {
                String producto = campoProducto.getText().trim();
                String cantidadText = campoCantidad.getText().trim();
                String stockMinimoText = campoStockMinimo.getText().trim();

                // Validar entradas
                if (producto.isEmpty() || cantidadText.isEmpty()) {
                    System.out.println("Error: Complete producto y cantidad");
                    return;
                }

                int cantidad = Integer.parseInt(cantidadText);
                int stockMinimo = stockMinimoText.isEmpty() ? 0 : Integer.parseInt(stockMinimoText);

                // Verificar si el producto existe
                PreparedStatement checkStmt = conexion.prepareStatement("SELECT id, cantidad FROM inventario WHERE producto = ?");
                checkStmt.setString(1, producto);
                ResultSet rs = checkStmt.executeQuery();

                int nuevoStock;
                if (rs.next()) {
                    int stockActual = rs.getInt("cantidad");
                    nuevoStock = stockActual + cantidad;

                    if (nuevoStock < 0) {
                        System.out.println("Error: Stock insuficiente");
                        return;
                    }

                    // Actualizar producto existente
                    PreparedStatement updateStmt = conexion.prepareStatement("UPDATE inventario SET cantidad = ? WHERE producto = ?");
                    updateStmt.setInt(1, nuevoStock);
                    updateStmt.setString(2, producto);
                    updateStmt.executeUpdate();
                } else {
                    if (cantidad < 0) {
                        System.out.println("Error: No hay stock para vender");
                        return;
                    }
                    // Insertar nuevo producto
                    PreparedStatement insertStmt = conexion.prepareStatement("INSERT INTO inventario (producto, cantidad) VALUES (?, ?)");
                    insertStmt.setString(1, producto);
                    insertStmt.setInt(2, cantidad);
                    insertStmt.executeUpdate();
                    nuevoStock = cantidad;
                }

                // Verificar stock mínimo
                if (stockMinimo > 0 && nuevoStock <= stockMinimo) {
                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("Alerta de Stock");
                    alerta.setHeaderText(null);
                    alerta.setContentText("El producto " + producto + " ha alcanzado el stock mínimo: " + nuevoStock);
                    alerta.showAndWait();
                }

                campoProducto.clear();
                campoCantidad.clear();
                campoStockMinimo.clear();
                tablaInventario.setItems(cargarInventario());
            } catch (SQLException | NumberFormatException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        // Actualizar tabla
        botonRefrescar.setOnAction(e -> tablaInventario.setItems(cargarInventario()));

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(botonActualizar, botonRefrescar);
        contenedor.getChildren().addAll(campoProducto, campoCantidad, campoStockMinimo, buttonContainer, tablaInventario);
        return contenedor;
    }

    private ObservableList<Inventario> cargarInventario() {
        ObservableList<Inventario> inventario = FXCollections.observableArrayList();
        try {
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM inventario");
            while (rs.next()) {
                inventario.add(new Inventario(
                    rs.getInt("id"),
                    rs.getString("producto"),
                    rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar inventario: " + e.getMessage());
        }
        return inventario;
    }
}