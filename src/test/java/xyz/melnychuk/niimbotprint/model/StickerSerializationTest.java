package xyz.melnychuk.niimbotprint.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StickerSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper strictMapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);

    @Test
    void roundTripPreservesElements() throws Exception {
        Sticker label = new Sticker(PrinterModel.D11);
        label.getElements().add(new TextElement("Привет", 10, 15));
        label.getElements().add(new BarcodeElement(BarcodeElementFormat.CODE_128, "12345", 20, 30, 0, 80, true));
        label.getElements().add(new ImageElement(5, 5, "aGVsbG8=", 100, 50));

        String json = mapper.writeValueAsString(label);

        Sticker restored = mapper.readValue(json, Sticker.class);

        assertEquals(PrinterModel.D11, restored.getPrinterModel());
        assertEquals(PrinterModel.D11.getDefaultWidth(), restored.getWidth());
        assertEquals(PrinterModel.D11.getDefaultHeight(), restored.getHeight());
        assertEquals(3, restored.getElements().size());

        TextElement text = (TextElement) restored.getElements().get(0);
        assertEquals("Привет", text.getText());
        assertEquals(10, text.getX());
        assertEquals("Arial", text.getFontFamily());

        BarcodeElement barcode = (BarcodeElement) restored.getElements().get(1);
        assertEquals("12345", barcode.getContent());
        assertEquals(BarcodeElementFormat.CODE_128, barcode.getFormat());

        ImageElement image = (ImageElement) restored.getElements().get(2);
        assertEquals("aGVsbG8=", image.getImageBase64());
    }

    @Test
    void defaultElementsUseSaneDefaults() {
        Sticker label = new Sticker();
        TextElement text = new TextElement();
        assertTrue(label.getWidth() > 0);
        assertTrue(text.getFontSize() > 0);
        assertEquals(BarcodeElementFormat.CODE_128, new BarcodeElement().getFormat());
    }

    @Test
    void strictMapperRejectsUnknownProperty() {
        String json = "{\"width\":384,\"height\":240,\"foo\":1}";
        assertThrows(Exception.class, () -> strictMapper.readValue(json, Sticker.class));
    }

    @Test
    void strictMapperRejectsUnknownElementType() {
        String json = "{\"width\":384,\"height\":240,\"elements\":[{\"type\":\"qrcode\"}]}";
        assertThrows(Exception.class, () -> strictMapper.readValue(json, Sticker.class));
    }
}
