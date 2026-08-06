package xyz.melnychuk.niimbotprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Image extends Element {

    private String imageBase64;
    private int width;
    private int height;

    public Image() {
        this(0, 0, null, 100, 100);
    }

    public Image(double x, double y, String imageBase64, int width, int height) {
        super(x, y);
        this.imageBase64 = imageBase64;
        this.width = width;
        this.height = height;
    }
}
