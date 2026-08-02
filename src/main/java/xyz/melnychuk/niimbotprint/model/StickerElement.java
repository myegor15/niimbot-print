package xyz.melnychuk.niimbotprint.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextElement.class, name = "text"),
        @JsonSubTypes.Type(value = ImageElement.class, name = "image"),
        @JsonSubTypes.Type(value = BarcodeElement.class, name = "barcode")
})
public abstract class StickerElement {

    private double x;
    private double y;

    protected StickerElement() {
    }

    protected StickerElement(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
