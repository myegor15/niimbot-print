package xyz.melnychuk.niimbotprint.i18n.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.i18n.Message;

@Getter
@RequiredArgsConstructor
public enum StickerMessage implements Message {

    PANEL_STICKER("panel.sticker"),
    BUTTON_NEW("button.new"),
    BUTTON_OPEN("button.open"),
    BUTTON_SAVE("button.save"),
    BUTTON_SAVE_AS("button.saveAs"),
    MESSAGE_NEW_STICKER("message.newSticker"),
    MESSAGE_OPENED("message.opened"),
    MESSAGE_SAVED("message.saved"),
    FILECHOOSER_OPEN_STICKER("filechooser.openSticker"),
    FILECHOOSER_SAVE_STICKER("filechooser.saveSticker"),
    FILECHOOSER_STICKER_FILTER("filechooser.stickerFilter"),
    ERROR_INVALID_LABEL_FILE("error.invalidLabelFile");

    private final String key;
}
