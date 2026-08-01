package xyz.melnychuk.niimprint.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class BarcodeGenerator {
    public static final String[] FORMATS = {"CODE_128", "CODE_39", "EAN_13", "UPC_A", "QR_CODE"};

    private BarcodeGenerator() {
    }

    public static BufferedImage generate(String content, String format, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.valueOf(format), width, height, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
