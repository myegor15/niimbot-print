package xyz.melnychuk.niimbotprint.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrintDensity {

    LIGHT("Светлая", 1),
    NORMAL("Нормальная", 3),
    DARK("Насыщенная", 5);

    private final String label;
    private final int value;

    @Override
    public String toString() {
        return label;
    }
}
