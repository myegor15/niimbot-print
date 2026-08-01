package xyz.melnychuk.niimprint.rest;

public record PrintRequest(
        String imageBase64,
        String printDirection,
        Integer quantity,
        Integer labelWidth,
        Integer labelHeight,
        Integer density,
        Integer threshold,
        Boolean waitUntilFinished) {

    public static PrintRequest of(String imageBase64, int labelWidth, int labelHeight,
                                  int density, int quantity, String printDirection) {
        return new PrintRequest(imageBase64, printDirection, quantity, labelWidth, labelHeight,
                density, 128, true);
    }
}
