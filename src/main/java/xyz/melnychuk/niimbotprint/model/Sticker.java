package xyz.melnychuk.niimbotprint.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sticker {

    private PrinterModel printerModel;
    private int width;
    private int height;
    private List<Element> elements = new ArrayList<>();

    public Sticker() {
        this(PrinterModel.B1);
    }

    public Sticker(PrinterModel printerModel) {
        this.printerModel = printerModel;
        this.width = printerModel.getDefaultWidth();
        this.height = printerModel.getDefaultHeight();
    }

    public void copyFrom(Sticker source) {
        this.printerModel = source.printerModel;
        this.width = source.width;
        this.height = source.height;
        this.elements.clear();
        this.elements.addAll(source.elements);
    }
}
