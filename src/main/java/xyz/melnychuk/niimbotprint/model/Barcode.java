package xyz.melnychuk.niimbotprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Barcode extends Element {

    private static final String DEFAULT_CONTENT = "1234567890";
    private static final int DEFAULT_WIDTH = 180;
    private static final int DEFAULT_HEIGHT = 96;

    private BarcodeFormat format;
    private String content;
    private int width;
    private int height;
    private boolean showValue;

    public Barcode() {
        this(0, 0, BarcodeFormat.CODE_128, DEFAULT_CONTENT, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public Barcode(double x, double y, BarcodeFormat format, String content, int width, int height, boolean showValue) {
        super(x, y);
        this.format = format;
        this.content = content;
        this.width = width;
        this.height = height;
        this.showValue = showValue;
    }
}
