package xyz.melnychuk.niimprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BarcodeElement extends LabelElement {
    private String content;
    private String format;
    private double width;
    private double height;

    public BarcodeElement() {
        this("1234567890", 0, 0, 200, 80, "CODE_128");
    }

    public BarcodeElement(String content, double x, double y, double width, double height, String format) {
        super(x, y);
        this.content = content;
        this.format = format;
        this.width = width;
        this.height = height;
    }
}
