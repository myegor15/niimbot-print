package xyz.melnychuk.niimbotprint.i18n;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class I18n {

    private static final String RESOURCE_PATH = "xyz/melnychuk/niimbotprint/i18n/messages";

    @Getter
    private static Language language = Language.ENGLISH;
    @Getter
    private static ResourceBundle bundle = load(Language.ENGLISH);

    private static final List<Consumer<Language>> languageListeners = new ArrayList<>();

    public static void init(Locale osLocale) {
        setLanguage(resolve(osLocale));
    }

    public static void addLanguageListener(Consumer<Language> listener) {
        languageListeners.add(listener);
    }

    public static String get(Message message, Object... args) {
        String pattern = bundle.getString(message.getKey());
        return args == null || args.length == 0 ? pattern : MessageFormat.format(pattern, args);
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
        String resourceName = RESOURCE_PATH + localeSuffix(target) + ".properties";
        try (InputStream stream = I18n.class.getResourceAsStream("/" + resourceName)) {
            if (stream == null) {
                throw new AppException("i18n bundle not found: " + resourceName);
            }
            return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AppException("Cannot load i18n bundle " + resourceName, e);
        }
    }

    private static String localeSuffix(Language target) {
        return target == Language.ENGLISH ? "" : "_" + target.getLocale().getLanguage();
    }
}
