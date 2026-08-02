package xyz.melnychuk.niimbotprint.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sticker {

    public static final int DEFAULT_WIDTH = 384;
    public static final int DEFAULT_HEIGHT = 240;

    private int width;
    private int height;
    private List<StickerElement> elements = new ArrayList<>();

    public Sticker() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Sticker(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
