package xyz.melnychuk.niimprint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import xyz.melnychuk.niimprint.model.*;
import xyz.melnychuk.niimblue.response.DevicesResponse;
import xyz.melnychuk.niimblue.response.InfoResponse;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimblue.request.PrintRequest;
import xyz.melnychuk.niimprint.ui.BarcodeGenerator;
import xyz.melnychuk.niimprint.ui.StickerCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class MainController {

    private static final List<String> FONTS = List.of(
            "Arial", "Arial Black", "Courier New", "Helvetica",
            "Segoe UI", "Tahoma", "Times New Roman", "Verdana"
    );

    private Sticker label = new Sticker();

    private StickerCanvas canvas;
    private NiimBlueApi api;
    private VBox propertiesBody;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private TextField serverField;
    @FXML
    private ComboBox<DevicesResponse.Device> deviceCombo;
    @FXML
    private Button scanButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Button printButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;
    @FXML
    private Spinner<Integer> densitySpinner;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private ComboBox<String> directionCombo;
    @FXML
    private TextArea printerInfoArea;
    @FXML
    private Label connectionLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private StackPane canvasHost;
    @FXML
    private VBox propertiesBox;

    public void setServerBaseUrl(String url) {
        serverField.setText(url);
        api = new NiimBlueApi(url);
    }

    @FXML
    private void initialize() {
        api = new NiimBlueApi(serverField.getText());

        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, label.getWidth()));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, label.getHeight()));
        densitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        directionCombo.getItems().addAll("top", "left");
        directionCombo.setValue("top");

        canvas = new StickerCanvas(label);
        canvas.setSelectionListener(this::showProperties);
        canvasHost.getChildren().add(canvas);

        widthSpinner.valueProperty().addListener((o, a, b) -> {
            label.setWidth(b);
            canvas.setLabelSize(b, heightSpinner.getValue());
        });
        heightSpinner.valueProperty().addListener((o, a, b) -> {
            label.setHeight(b);
            canvas.setLabelSize(widthSpinner.getValue(), b);
        });
        serverField.focusedProperty().addListener((o, wasFocused, focused) -> {
            if (!focused) {
                api = new NiimBlueApi(serverField.getText());
            }
        });

        propertiesBody = new VBox(8);
        propertiesBox.getChildren().add(propertiesBody);

        updateConnectionUi(false);
        showProperties(null);

        Timeline poller = new Timeline(new KeyFrame(Duration.seconds(2), e -> pollStatus()));
        poller.setCycleCount(Timeline.INDEFINITE);
        poller.play();
    }

    private void pollStatus() {
        runAsync(() -> api.isConnected(), ok -> {
            updateConnectionUi(Boolean.TRUE.equals(ok));
            if (Boolean.TRUE.equals(ok)) {
                loadPrinterInfo();
            } else {
                printerInfoArea.clear();
            }
        });
    }

    private void updateConnectionUi(boolean connected) {
        connectionLabel.setText(connected ? "Подключено" : "Не подключено");
        connectionLabel.setStyle(connected ? "-fx-text-fill: green;" : "-fx-text-fill: gray;");
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        scanButton.setDisable(connected);
        printButton.setDisable(!connected);
    }

    private void loadPrinterInfo() {
        runAsync(() -> {
            InfoResponse info = api.info();
            InfoResponse.PrinterInfo printer = info.printerInfo();
            InfoResponse.ModelMetadata model = info.modelMetadata();
            return "Модель: " + model.model() + "\n"
                    + "DPI: " + model.dpi() + "\n"
                    + "Задача: " + info.detectedPrintTask() + "\n"
                    + "Серийник: " + printer.serial() + "\n"
                    + "MAC: " + printer.mac() + "\n"
                    + "Заряд: " + printer.charge() + "%\n"
                    + "FW: " + printer.softwareVersion();
        }, printerInfoArea::setText);
    }

    @FXML
    private void onScan() {
        setMessage("Поиск устройств...");
        runAsync(api::scan, devices -> {
            deviceCombo.getItems().setAll(devices.devices());
            setMessage("Найдено устройств: " + devices.devices().size());
        });
    }

    @FXML
    private void onConnect() {
        DevicesResponse.Device device = deviceCombo.getValue();
        if (device == null) {
            setMessage("Сначала выполните поиск и выберите устройство");
            return;
        }
        runAsync(() -> {
            String target = device.address() == null || device.address().isBlank() ? device.name() : device.address();
            api.connect("ble", target);
            return api.isConnected();
        }, ok -> setMessage(ok ? "Подключено к " + device : "Не удалось подключиться"));
    }

    @FXML
    private void onDisconnect() {
        runAsync(() -> {
            api.disconnect();
            return true;
        }, ok -> setMessage("Отключено"));
    }

    @FXML
    private void onNew() {
        label = new Sticker();
        widthSpinner.getValueFactory().setValue(label.getWidth());
        heightSpinner.getValueFactory().setValue(label.getHeight());
        canvas.setSticker(label);
        showProperties(null);
        setMessage("Новая этикетка");
    }

    @FXML
    private void onOpen() {
        File file = chooseFile("Открыть этикетку");
        if (file == null) {
            return;
        }
        runAsync(() -> mapper.readValue(Files.readAllBytes(file.toPath()), Sticker.class), loaded -> {
            label = loaded;
            widthSpinner.getValueFactory().setValue(loaded.getWidth());
            heightSpinner.getValueFactory().setValue(loaded.getHeight());
            canvas.setSticker(label);
            showProperties(null);
            setMessage("Открыто: " + file.getName());
        });
    }

    @FXML
    private void onSave() {
        File file = chooseFile("Сохранить этикетку");
        if (file == null) {
            return;
        }
        runAsync(() -> {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, label);
            return true;
        }, ok -> setMessage("Сохранено: " + file.getName()));
    }

    @FXML
    private void onAddText() {
        canvas.addElement(new TextElement("Текст", 10, 10));
    }

    @FXML
    private void onAddBarcode() {
        canvas.addElement(new BarcodeElement());
    }

    @FXML
    private void onAddImage() {
        File file = chooseFile("Выбрать изображение");
        if (file == null) {
            return;
        }
        runAsync(() -> {
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            var image = javax.imageio.ImageIO.read(file);
            double w = 100;
            double h = image != null ? 100.0 * image.getHeight() / image.getWidth() : 100;
            return new ImageElement(10, 10, base64, w, h);
        }, element -> {
            canvas.addElement(element);
            setMessage("Изображение добавлено");
        });
    }

    @FXML
    private void onDelete() {
        if (canvas.getSelectedElement() != null) {
            canvas.removeSelected();
            setMessage("Элемент удалён");
        }
    }

    @FXML
    private void onPrint() {
        String base64 = snapshotBase64();
        if (base64 == null) {
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                PrintRequest request = PrintRequest.of(base64, label.getWidth(), label.getHeight(),
                        densitySpinner.getValue(), quantitySpinner.getValue(), directionCombo.getValue());
                api.print(request);
                return "Печать отправлена (" + quantitySpinner.getValue() + " шт.)";
            }
        };
        task.setOnSucceeded(e -> setMessage(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private String snapshotBase64() {
        canvas.setSelectionVisible(false);
        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            canvas.setSelectionVisible(true);
        }
    }

    private void showProperties(StickerElement element) {
        propertiesBody.getChildren().clear();
        if (element == null) {
            propertiesBody.getChildren().add(new Label("Выберите элемент на этикетке"));
            return;
        }
        if (element instanceof TextElement text) {
            TextField field = new TextField(text.getText());
            field.textProperty().addListener((o, a, b) -> {
                text.setText(b);
                canvas.updateElement(element);
            });
            addRow("Текст", field);

            ComboBox<String> fontBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(FONTS));
            fontBox.setValue(List.of(FONTS).contains(text.getFontFamily()) ? text.getFontFamily() : "Arial");
            fontBox.valueProperty().addListener((o, a, b) -> {
                text.setFontFamily(b);
                canvas.updateElement(element);
            });
            addRow("Шрифт", fontBox);

            Spinner<Double> size = doubleSpinner(text.getFontSize(), 6, 200);
            size.valueProperty().addListener((o, a, b) -> {
                text.setFontSize(b);
                canvas.updateElement(element);
            });
            addRow("Размер", size);

            CheckBox bold = new CheckBox();
            bold.setSelected(text.isBold());
            bold.selectedProperty().addListener((o, a, b) -> {
                text.setBold(b);
                canvas.updateElement(element);
            });
            addRow("Жирный", bold);
        } else if (element instanceof BarcodeElement barcode) {
            TextField field = new TextField(barcode.getContent());
            field.textProperty().addListener((o, a, b) -> {
                barcode.setContent(b);
                canvas.updateElement(element);
            });
            addRow("Содержимое", field);

            ComboBox<String> formatBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(BarcodeGenerator.FORMATS));
            formatBox.setValue(barcode.getFormat());
            formatBox.valueProperty().addListener((o, a, b) -> {
                barcode.setFormat(b);
                canvas.updateElement(element);
            });
            addRow("Формат", formatBox);

            Spinner<Double> bw = doubleSpinner(barcode.getWidth(), 1, 2000);
            bw.valueProperty().addListener((o, a, b) -> {
                barcode.setWidth(b);
                canvas.updateElement(element);
            });
            addRow("Ширина", bw);

            Spinner<Double> bh = doubleSpinner(barcode.getHeight(), 1, 2000);
            bh.valueProperty().addListener((o, a, b) -> {
                barcode.setHeight(b);
                canvas.updateElement(element);
            });
            addRow("Высота", bh);
        } else if (element instanceof ImageElement image) {
            Spinner<Double> iw = doubleSpinner(image.getWidth(), 1, 2000);
            iw.valueProperty().addListener((o, a, b) -> {
                image.setWidth(b);
                canvas.updateElement(element);
            });
            addRow("Ширина", iw);

            Spinner<Double> ih = doubleSpinner(image.getHeight(), 1, 2000);
            ih.valueProperty().addListener((o, a, b) -> {
                image.setHeight(b);
                canvas.updateElement(element);
            });
            addRow("Высота", ih);
        }

        Spinner<Double> x = doubleSpinner(element.getX(), 0, 2000);
        x.valueProperty().addListener((o, a, b) -> {
            element.setX(b);
            canvas.updateElement(element);
        });
        addRow("X", x);

        Spinner<Double> y = doubleSpinner(element.getY(), 0, 2000);
        y.valueProperty().addListener((o, a, b) -> {
            element.setY(b);
            canvas.updateElement(element);
        });
        addRow("Y", y);
    }

    private Spinner<Double> doubleSpinner(double value, double min, double max) {
        Spinner<Double> spinner = new Spinner<>(min, max, value, 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        return spinner;
    }

    private void addRow(String name, javafx.scene.Node control) {
        HBox row = new HBox(8, new Label(name), control);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        propertiesBody.getChildren().add(row);
    }

    private File chooseFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (title.startsWith("Открыть") || title.startsWith("Выбрать")) {
            return chooser.showOpenDialog(canvasHost.getScene().getWindow());
        }
        return chooser.showSaveDialog(canvasHost.getScene().getWindow());
    }

    private <T> void runAsync(ThrowingSupplier<T> action, java.util.function.Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.get();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void setMessage(String message) {
        messageLabel.setText(message);
    }

    private void showError(Throwable error) {
        setMessage("Ошибка: " + (error != null ? error.getMessage() : "неизвестна"));
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
