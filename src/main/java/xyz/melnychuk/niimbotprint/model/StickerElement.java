package xyz.melnychuk.niimbotprint.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextElement.class, name = "text"),
        @JsonSubTypes.Type(value = ImageElement.class, name = "image"),
        @JsonSubTypes.Type(value = BarcodeElement.class, name = "barcode")
})
public abstract class StickerElement {

    @JsonIgnore
    private final UUID id = UUID.randomUUID();

    private double x;
    private double y;
    private double rotation;

    protected StickerElement() {
    }

    protected StickerElement(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
