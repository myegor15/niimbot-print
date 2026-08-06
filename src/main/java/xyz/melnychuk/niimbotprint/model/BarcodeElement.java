package xyz.melnychuk.niimbotprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BarcodeElement extends StickerElement {

    private static final String DEFAULT_CONTENT = "1234567890";
    private static final int DEFAULT_WIDTH = 180;
    private static final int DEFAULT_HEIGHT = 96;

    private BarcodeElementFormat format;
    private String content;
    private int width;
    private int height;
    private boolean showValue;

    public BarcodeElement() {
        this(BarcodeElementFormat.CODE_128, DEFAULT_CONTENT, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public BarcodeElement(BarcodeElementFormat format, String content, double x, double y, int width, int height, boolean showValue) {
        super(x, y);
        this.format = format;
        this.content = content;
        this.width = width;
        this.height = height;
        this.showValue = showValue;
    }
}
