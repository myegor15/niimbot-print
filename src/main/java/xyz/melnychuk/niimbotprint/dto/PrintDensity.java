package xyz.melnychuk.niimbotprint.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.i18n.I18n;
import xyz.melnychuk.niimbotprint.i18n.message.PrinterMessage;

@Getter
@RequiredArgsConstructor
public enum PrintDensity {

    LIGHT(PrinterMessage.DENSITY_LIGHT, 1),
    NORMAL(PrinterMessage.DENSITY_NORMAL, 3),
    DARK(PrinterMessage.DENSITY_DARK, 5);

    private final PrinterMessage labelKey;
    private final int value;

    @Override
    public String toString() {
        return I18n.get(labelKey);
    }
}
