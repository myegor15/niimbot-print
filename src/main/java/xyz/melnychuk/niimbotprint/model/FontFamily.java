package xyz.melnychuk.niimbotprint.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum FontFamily {

    ARIAL("Arial"),
    ARIAL_BLACK("Arial Black"),
    COURIER_NEW("Courier New"),
    HELVETICA("Helvetica"),
    SEGOE_UI("Segoe UI"),
    TAHOMA("Tahoma"),
    TIMES_NEW_ROMAN("Times New Roman"),
    VERDANA("Verdana");

    private static final Map<String, FontFamily> BY_NAME =
            Stream.of(values()).collect(Collectors.toMap(FontFamily::getDisplayName, font -> font));

    private final String displayName;

    public static FontFamily byDisplayName(String name) {
        return BY_NAME.get(name);
    }

}
