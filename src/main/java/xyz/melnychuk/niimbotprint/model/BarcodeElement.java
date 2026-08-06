package xyz.melnychuk.niimbotprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BarcodeElement extends StickerElement {

    private String content;
    private String format;
    private int width;
    private int height;

    public BarcodeElement() {
        this("1234567890", 0, 0, 200, 80, "CODE_128");
    }

    public BarcodeElement(String content, double x, double y, int width, int height, String format) {
        super(x, y);
        this.content = content;
        this.format = format;
        this.width = width;
        this.height = height;
    }
}
