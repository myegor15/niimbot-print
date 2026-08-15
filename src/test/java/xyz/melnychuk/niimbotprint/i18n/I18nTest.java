package xyz.melnychuk.niimbotprint.i18n;

import org.junit.jupiter.api.Test;
import xyz.melnychuk.niimbotprint.i18n.message.AppMessage;
import xyz.melnychuk.niimbotprint.i18n.message.EditorMessage;
import xyz.melnychuk.niimbotprint.i18n.message.PrinterMessage;
import xyz.melnychuk.niimbotprint.i18n.message.StickerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class I18nTest {

    @Test
    void setLanguageKeepsSupportedLanguage() {
        I18n.setLanguage(Language.UKRAINIAN);
        assertEquals(Language.UKRAINIAN, I18n.getLanguage());
        I18n.setLanguage(Language.RUSSIAN);
        assertEquals(Language.RUSSIAN, I18n.getLanguage());
        I18n.setLanguage(Language.ENGLISH);
        assertEquals(Language.ENGLISH, I18n.getLanguage());
    }

    @Test
    void initFallsBackToEnglishForUnsupportedLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("fr"));
            I18n.init();
            assertEquals(Language.ENGLISH, I18n.getLanguage());
            Locale.setDefault(Locale.ROOT);
            I18n.init();
            assertEquals(Language.ENGLISH, I18n.getLanguage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void initDetectsSupportedOsLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("uk"));
            I18n.init();
            assertEquals(Language.UKRAINIAN, I18n.getLanguage());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void getResolvesLocalizedText() {
        I18n.setLanguage(Language.RUSSIAN);
        assertEquals("Готово", I18n.get(AppMessage.STATUS_READY));
        I18n.setLanguage(Language.ENGLISH);
        assertEquals("Ready", I18n.get(AppMessage.STATUS_READY));
    }

    @Test
    void getFormatsArguments() {
        I18n.setLanguage(Language.ENGLISH);
        assertEquals("Devices found: 3", I18n.get(PrinterMessage.MESSAGE_DEVICES_FOUND, 3));
    }

    @Test
    void allKeysPresentInEverySupportedBundle() {
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(List.of(AppMessage.values()));
        allMessages.addAll(List.of(PrinterMessage.values()));
        allMessages.addAll(List.of(StickerMessage.values()));
        allMessages.addAll(List.of(EditorMessage.values()));
        for (Language language : Language.values()) {
            I18n.setLanguage(language);
            ResourceBundle bundle = I18n.getBundle();
            for (Message message : allMessages) {
                assertFalse(bundle.getString(message.getKey()).isBlank(), message + " is empty in " + language);
            }
        }
    }

    @Test
    void missingKeyThrows() {
        ResourceBundle bundle = I18n.getBundle();
        assertThrows(MissingResourceException.class, () -> bundle.getString("no.such.key"));
    }
}
