package xyz.melnychuk.niimprint.model;

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
public abstract class LabelElement {
    private double x;
    private double y;

    protected LabelElement() {
    }

    protected LabelElement(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
