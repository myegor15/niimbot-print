package xyz.melnychuk.niimbotprint.ui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageDecoder {

    private static final int BINARY_THRESHOLD = 128;

    public static Image decodeBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return toBinary(new Image(new ByteArrayInputStream(data)));
    }

    private static Image toBinary(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage binary = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = binary.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double luminance = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                writer.setColor(x, y, luminance >= BINARY_THRESHOLD / 255.0 ? Color.WHITE : Color.BLACK);
            }
        }
        return binary;
    }
}
