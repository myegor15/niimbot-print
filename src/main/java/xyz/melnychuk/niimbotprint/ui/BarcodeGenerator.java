package xyz.melnychuk.niimbotprint.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BarcodeGenerator {

    public static final List<String> FORMATS = List.of("CODE_128", "CODE_39", "EAN_13", "UPC_A", "QR_CODE");

    public static BufferedImage generate(String content, String format, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.valueOf(format), width, height, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException | IllegalArgumentException e) {
            throw new AppException(e);
        }
    }
}
