package xyz.melnychuk.niimbotprint.i18n;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class I18n {

    private static final String RESOURCE_BASE = "xyz.melnychuk.niimbotprint.i18n.messages";

    @Getter
    private static volatile Language language = Language.ENGLISH;
    @Getter
    private static volatile ResourceBundle bundle = load(Language.ENGLISH);

    private static final List<Consumer<Language>> languageListeners = new ArrayList<>();

    public static void init() {
        setLanguage(resolve(Locale.getDefault()));
    }

    public static void addLanguageListener(Consumer<Language> listener) {
        languageListeners.add(listener);
    }

    public static String get(Message message, Object... args) {
        String pattern = bundle.getString(message.getKey());
        return args == null ? pattern : MessageFormat.format(pattern, args);
    }

    public static void setLanguage(Language newLanguage) {
        language = newLanguage == null ? Language.ENGLISH : newLanguage;
        bundle = load(language);
        languageListeners.forEach(listener -> listener.accept(language));
    }

    private static Language resolve(Locale candidate) {
        if (candidate == null) {
            return Language.ENGLISH;
        }
        return Arrays.stream(Language.values())
                .filter(supported -> supported.getLocale().getLanguage().equals(candidate.getLanguage()))
                .findFirst()
                .orElse(Language.ENGLISH);
    }

    private static ResourceBundle load(Language target) {
        return ResourceBundle.getBundle(RESOURCE_BASE, target.getLocale(), I18n.class.getClassLoader());
    }
}
