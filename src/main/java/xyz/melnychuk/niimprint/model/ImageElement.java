package xyz.melnychuk.niimprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageElement extends StickerElement {

    private String imageBase64;
    private double width;
    private double height;

    public ImageElement() {
        this(0, 0, null, 100, 100);
    }

    public ImageElement(double x, double y, String imageBase64, double width, double height) {
        super(x, y);
        this.imageBase64 = imageBase64;
        this.width = width;
        this.height = height;
    }
}
