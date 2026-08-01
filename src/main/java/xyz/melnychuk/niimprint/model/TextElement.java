package xyz.melnychuk.niimprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextElement extends LabelElement {
    private String text;
    private String fontFamily;
    private double fontSize;
    private boolean bold;

    public TextElement() {
        this("Текст", 0, 0);
    }

    public TextElement(String text, double x, double y) {
        super(x, y);
        this.text = text;
        this.fontFamily = "Arial";
        this.fontSize = 16;
    }
}
