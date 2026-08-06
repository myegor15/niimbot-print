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
        @JsonSubTypes.Type(value = Text.class, name = "text"),
        @JsonSubTypes.Type(value = Image.class, name = "image"),
        @JsonSubTypes.Type(value = Barcode.class, name = "barcode")
})
public abstract class Element {

    @JsonIgnore
    private final UUID id = UUID.randomUUID();

    private double x;
    private double y;
    private double rotation;

    protected Element() {
    }

    protected Element(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
