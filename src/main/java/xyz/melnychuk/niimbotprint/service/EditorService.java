package xyz.melnychuk.niimbotprint.service;

import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.Barcode;
import xyz.melnychuk.niimbotprint.model.Image;
import xyz.melnychuk.niimbotprint.model.Text;
import xyz.melnychuk.niimbotprint.model.FontFamily;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@Slf4j
public class EditorService {

    public Text getTextElement() {
        return new Text(10, 10, FontFamily.ARIAL, "Текст", 16, false, false, false);
    }

    public Barcode getBarcodeElement() {
        return new Barcode();
    }

    public Image getImageElement(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            BufferedImage image = ImageIO.read(file);
            double w = 100;
            double h = image != null ? 100.0 * image.getHeight() / image.getWidth() : 100;
            return new Image(10, 10, base64, (int) Math.round(w), (int) Math.round(h));
        } catch (Exception e) {
            log.error("Exception in getImageElement().", e);
            throw new AppException(e);
        }
    }
}
