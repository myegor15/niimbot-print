package xyz.melnychuk.niimbotprint.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Text extends Element {

    private static final String DEFAULT_TEXT = "Текст";
    private static final FontFamily DEFAULT_FONT = FontFamily.ARIAL;
    private static final int DEFAULT_FONT_SIZE = 16;

    private String text;
    private FontFamily fontFamily;
    private int fontSize;
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public Text() {
        this(0, 0, DEFAULT_FONT, DEFAULT_TEXT, DEFAULT_FONT_SIZE, false, false, false);
    }

    public Text(double x, double y, FontFamily fontFamily, String text, int fontSize, boolean bold, boolean italic, boolean underline) {
        super(x, y);
        this.text = text;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }
}
