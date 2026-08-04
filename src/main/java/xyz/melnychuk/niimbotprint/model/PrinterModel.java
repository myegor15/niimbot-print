package xyz.melnychuk.niimbotprint.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrinterModel {

    B1("B1", 384, 240),
    D11("D11", 302, 270);

    private final String label;
    private final int defaultWidth;
    private final int defaultHeight;

    @Override
    public String toString() {
        return label;
    }
}