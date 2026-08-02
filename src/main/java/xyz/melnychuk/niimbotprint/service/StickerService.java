package xyz.melnychuk.niimbotprint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.Sticker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@Slf4j
public class StickerService {

    private final ObjectMapper mapper = new ObjectMapper();

    public Sticker loadSticker(File file) {
        try {
            return mapper.readValue(Files.readAllBytes(file.toPath()), Sticker.class);
        } catch (Exception e) {
            log.error("Exception in loadSticker().", e);
            throw new AppException(e);
        }
    }

    public void saveSticker(Sticker sticker, File file) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, sticker);
        } catch (Exception e) {
            log.error("Exception in saveSticker().", e);
            throw new AppException(e);
        }
    }

    public ImageElement loadImageElement(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            BufferedImage image = ImageIO.read(file);
            double w = 100;
            double h = image != null ? 100.0 * image.getHeight() / image.getWidth() : 100;
            return new ImageElement(10, 10, base64, w, h);
        } catch (Exception e) {
            log.error("Exception in loadImageElement().", e);
            throw new AppException(e);
        }
    }
}
