package xyz.melnychuk.niimbotprint.i18n.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.i18n.Message;

@Getter
@RequiredArgsConstructor
public enum EditorMessage implements Message {

    PANEL_PROPERTIES("panel.properties"),
    TOOLTIP_UNDO("tooltip.undo"),
    TOOLTIP_REDO("tooltip.redo"),
    TOOLTIP_DELETE("tooltip.delete"),
    TOOLTIP_ADD_TEXT("tooltip.addText"),
    TOOLTIP_ADD_BARCODE("tooltip.addBarcode"),
    TOOLTIP_ADD_IMAGE("tooltip.addImage"),
    TOOLTIP_ROTATE_LEFT("tooltip.rotateLeft"),
    TOOLTIP_ROTATE_RIGHT("tooltip.rotateRight"),
    TOOLTIP_SHOW_GRID("tooltip.showGrid"),
    TOOLTIP_SNAP_POSITION("tooltip.snapPosition"),
    TOOLTIP_SNAP_ANGLE("tooltip.snapAngle"),
    MESSAGE_IMAGE_ADDED("message.imageAdded"),
    MESSAGE_ELEMENT_DELETED("message.elementDeleted"),
    FILECHOOSER_CHOOSE_IMAGE("filechooser.chooseImage"),
    LABEL_TEXT("label.text"),
    LABEL_FONT("label.font"),
    LABEL_SIZE("label.size"),
    LABEL_BOLD("label.bold"),
    LABEL_ITALIC("label.italic"),
    LABEL_UNDERLINE("label.underline"),
    LABEL_CONTENT("label.content"),
    LABEL_SHOW_VALUE("label.showValue"),
    TEXT_DEFAULT("text.default"),
    BARCODE_INVALID("barcode.invalid");

    private final String key;
}
