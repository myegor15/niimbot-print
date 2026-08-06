package xyz.melnychuk.niimbotprint.model;

import com.google.zxing.BarcodeFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BarcodeElementFormat {

    CODE_128(BarcodeFormat.CODE_128);

    private final BarcodeFormat zxingFormat;

}
