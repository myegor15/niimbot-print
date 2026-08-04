package xyz.melnychuk.niimbotprint.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sticker {

    private PrinterModel printerModel;
    private int width;
    private int height;
    private List<StickerElement> elements = new ArrayList<>();

    public Sticker() {
        this(PrinterModel.B1);
    }

    public Sticker(PrinterModel printerModel) {
        this.printerModel = printerModel;
        this.width = printerModel.getDefaultWidth();
        this.height = printerModel.getDefaultHeight();
    }
}
