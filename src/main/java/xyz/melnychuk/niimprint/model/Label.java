package xyz.melnychuk.niimprint.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Label {
    public static final int DEFAULT_WIDTH = 384;
    public static final int DEFAULT_HEIGHT = 240;

    private int width;
    private int height;
    private List<LabelElement> elements = new ArrayList<>();

    public Label() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Label(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
