package xyz.melnychuk.niimbotprint.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.AppReadableException;
import xyz.melnychuk.niimbotprint.i18n.message.StickerMessage;
import xyz.melnychuk.niimbotprint.model.Sticker;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class StickerService {

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);

    public Sticker loadSticker(File file) {
        try {
            return mapper.readValue(Files.readAllBytes(file.toPath()), Sticker.class);
        } catch (Exception e) {
            log.error("Exception in loadSticker().", e);
            throw new AppReadableException(StickerMessage.ERROR_INVALID_LABEL_FILE, e.getMessage());
        }
    }

    public void saveSticker(Sticker sticker, File file) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, sticker);
        } catch (Exception e) {
            log.error("Exception in saveSticker().", e);
            throw new AppException(e);
        }
    }
}
