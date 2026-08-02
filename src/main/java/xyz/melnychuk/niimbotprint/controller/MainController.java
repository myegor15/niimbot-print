package xyz.melnychuk.niimbotprint.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.service.PrintService;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.StickerCanvas;
import xyz.melnychuk.niimbotprint.ui.view.ElementPropertiesView;
import xyz.melnychuk.niimbotprint.ui.view.ElementPropertiesViewFactory;
import xyz.melnychuk.niimbotprint.util.View;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@View(
        fxml = "main-view.fxml",
        title = "NiimBot Print",
        width = 1200,
        height = 700,
        stylesheets = "style.css"
)
public class MainController extends Controller {

    private Sticker label = new Sticker();
    private StickerCanvas canvas;
    private VBox propertiesBody;

    @FXML
    private TextField serverField;
    @FXML
    private ComboBox<DeviceDto> deviceCombo;
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

    private Timeline timeline;

    private PrintService printService;
    @Setter
    private StickerService stickerService;

    public void setPrintService(PrintService printService) {
        this.printService = printService;
        serverField.setText(printService.getApiUrl());
    }

    @FXML
    private void initialize() {
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
        propertiesBody = new VBox(8);
        propertiesBox.getChildren().add(propertiesBody);

        updateConnectionUi(false);
        showProperties(null);
        initTimeline();
    }

    private void initTimeline() {
        if (timeline != null) {
            return;
        }

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> pollStatus())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pollStatus() {
        run(
                () -> printService.isConnected(),
                ok -> {
                    updateConnectionUi(Boolean.TRUE.equals(ok));
                    if (Boolean.TRUE.equals(ok)) {
                        loadPrinterInfo();
                    } else {
                        printerInfoArea.clear();
                    }
                },
                this::showError
        );
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
        run(
                printService::getPrinterInfo,
                this::setPrinterInfo,
                this::showError
        );
    }

    private void setPrinterInfo(PrinterDto info) {
        printerInfoArea.setText(
                "Модель: " + info.getModel() + "\n"
                        + "DPI: " + info.getDpi() + "\n"
                        + "Задача: " + info.getDetectedPrintTask() + "\n"
                        + "Серийник: " + info.getSerial() + "\n"
                        + "MAC: " + info.getMac() + "\n"
                        + "Заряд: " + info.getCharge() + "%\n"
                        + "FW: " + info.getSoftwareVersion()
        );
    }

    @FXML
    private void onScan() {
        setMessage("Поиск устройств...");
        run(
                printService::scanDevices,
                devices -> {
                    deviceCombo.getItems().setAll(devices);
                    setMessage("Найдено устройств: " + devices.size());
                },
                this::showError
        );
    }

    @FXML
    private void onConnect() {
        DeviceDto device = deviceCombo.getValue();
        if (device == null) {
            setMessage("Сначала выполните поиск и выберите устройство");
            return;
        }
        run(
                () -> printService.connect(device),
                ok -> setMessage(ok ? "Подключено к " + device : "Не удалось подключиться"),
                this::showError
        );
    }

    @FXML
    private void onDisconnect() {
        run(
                () -> {
                    printService.disconnect();
                    return true;
                },
                ok -> setMessage("Отключено"),
                this::showError
        );
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
        run(
                () -> stickerService.loadSticker(file),
                loaded -> {
                    label = loaded;
                    widthSpinner.getValueFactory().setValue(loaded.getWidth());
                    heightSpinner.getValueFactory().setValue(loaded.getHeight());
                    canvas.setSticker(label);
                    showProperties(null);
                    setMessage("Открыто: " + file.getName());
                },
                this::showError
        );
    }

    @FXML
    private void onSave() {
        File file = chooseFile("Сохранить этикетку");
        if (file == null) {
            return;
        }
        run(
                () -> {
                    stickerService.saveSticker(label, file);
                    return true;
                },
                ok -> setMessage("Сохранено: " + file.getName()),
                this::showError
        );
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
        run(
                () -> stickerService.loadImageElement(file),
                element -> {
                    canvas.addElement(element);
                    setMessage("Изображение добавлено");
                },
                this::showError
        );
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

        run(
                () -> {
                    PrintTaskDto task = new PrintTaskDto(base64, label.getWidth(), label.getHeight(),
                            densitySpinner.getValue(), quantitySpinner.getValue(), directionCombo.getValue());
                    printService.print(task);
                    return "Печать отправлена (" + quantitySpinner.getValue() + " шт.)";
                },
                this::setMessage,
                this::showError
        );
    }

    private String snapshotBase64() {
        canvas.setSelectionVisible(false);
        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new AppException(e);
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
        ElementPropertiesView<StickerElement> view = ElementPropertiesViewFactory.create(element);
        view.setChangeListener(canvas::updateElement);
        view.show(element);
        propertiesBody.getChildren().add(view);
    }

    private File chooseFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (title.startsWith("Открыть") || title.startsWith("Выбрать")) {
            return chooser.showOpenDialog(canvasHost.getScene().getWindow());
        }
        return chooser.showSaveDialog(canvasHost.getScene().getWindow());
    }

    private void setMessage(String message) {
        messageLabel.setText(message);
    }

    private void showError(Throwable error) {
        log.error("Exception in showError().", error);
        setMessage("Ошибка: " + (error != null ? error.getMessage() : "неизвестна"));
    }
}
