package xyz.melnychuk.niimbotprint.service;

import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.model.FontFamily;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@Slf4j
public class EditorService {

    public TextElement getTextElement() {
        return new TextElement(FontFamily.ARIAL, "Текст", 10, 10, 16, false, false, false);
    }

    public BarcodeElement getBarcodeElement() {
        return new BarcodeElement();
    }

    public ImageElement getImageElement(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            BufferedImage image = ImageIO.read(file);
            double w = 100;
            double h = image != null ? 100.0 * image.getHeight() / image.getWidth() : 100;
            return new ImageElement(10, 10, base64, (int) Math.round(w), (int) Math.round(h));
        } catch (Exception e) {
            log.error("Exception in getImageElement().", e);
            throw new AppException(e);
        }
    }
}
