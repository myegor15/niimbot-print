package xyz.melnychuk.niimbotprint.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StickerSerializationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripPreservesElements() throws Exception {
        Sticker label = new Sticker(384, 240);
        label.getElements().add(new TextElement("Привет", 10, 15));
        label.getElements().add(new BarcodeElement("12345", 20, 30, 200, 80, "CODE_128"));
        label.getElements().add(new ImageElement(5, 5, "aGVsbG8=", 100, 50));

        String json = mapper.writeValueAsString(label);

        Sticker restored = mapper.readValue(json, Sticker.class);

        assertEquals(384, restored.getWidth());
        assertEquals(240, restored.getHeight());
        assertEquals(3, restored.getElements().size());

        TextElement text = (TextElement) restored.getElements().get(0);
        assertEquals("Привет", text.getText());
        assertEquals(10, text.getX());
        assertEquals("Arial", text.getFontFamily());

        BarcodeElement barcode = (BarcodeElement) restored.getElements().get(1);
        assertEquals("12345", barcode.getContent());
        assertEquals("CODE_128", barcode.getFormat());

        ImageElement image = (ImageElement) restored.getElements().get(2);
        assertEquals("aGVsbG8=", image.getImageBase64());
    }

    @Test
    void defaultElementsUseSaneDefaults() {
        Sticker label = new Sticker();
        TextElement text = new TextElement();
        assertTrue(label.getWidth() > 0);
        assertTrue(text.getFontSize() > 0);
        assertEquals("CODE_128", new BarcodeElement().getFormat());
    }
}
