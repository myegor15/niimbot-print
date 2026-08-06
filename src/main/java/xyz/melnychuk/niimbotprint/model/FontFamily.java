package xyz.melnychuk.niimbotprint.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    private final String displayName;

}
