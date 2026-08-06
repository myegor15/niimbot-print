package xyz.melnychuk.niimbotprint.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BarcodeFormat {

    CODE_128(com.google.zxing.BarcodeFormat.CODE_128);

    private final com.google.zxing.BarcodeFormat zxingFormat;

}
