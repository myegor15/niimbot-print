package xyz.melnychuk.niimbotprint.i18n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum Language {

    ENGLISH(Locale.ENGLISH, "English", "EN"),
    UKRAINIAN(Locale.forLanguageTag("uk"), "Українська", "UK"),
    RUSSIAN(Locale.forLanguageTag("ru"), "Русский", "RU");

    private final Locale locale;
    private final String displayName;
    private final String code;
}
