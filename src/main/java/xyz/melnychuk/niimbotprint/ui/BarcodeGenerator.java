package xyz.melnychuk.niimbotprint.ui;

import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.BarcodeFormat;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BarcodeGenerator {

    public static BufferedImage generate(String content, BarcodeFormat format, int width, int height) {
        try {
            BitMatrix matrix = encode(content, format);
            int[] bounds = bounds(matrix);
            int columns = bounds[1] - bounds[0] + 1;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int px = 0; px < width; px++) {
                int sx = bounds[0] + (int) ((long) columns * px / width);
                int rgb = matrix.get(sx, 0) ? 0x000000 : 0xFFFFFF;
                for (int y = 0; y < height; y++) {
                    image.setRGB(px, y, rgb);
                }
            }
            return image;
        } catch (WriterException | IllegalArgumentException e) {
            throw new AppException(e);
        }
    }

    private static BitMatrix encode(String content, BarcodeFormat format) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        return new MultiFormatWriter().encode(content, format.getZxingFormat(), 0, 0, hints);
    }

    private static int[] bounds(BitMatrix matrix) {
        int minX = matrix.getWidth();
        int maxX = 0;
        for (int x = 0; x < matrix.getWidth(); x++) {
            if (matrix.get(x, 0)) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
        }
        return new int[]{minX, maxX};
    }
}
