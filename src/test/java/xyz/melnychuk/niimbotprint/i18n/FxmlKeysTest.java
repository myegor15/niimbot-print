package xyz.melnychuk.niimbotprint.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FxmlKeysTest {

    private static final Pattern KEY_PATTERN = Pattern.compile("%([a-zA-Z][a-zA-Z0-9.]*)");

    @BeforeEach
    void setUp() {
        I18n.setLanguage(Language.ENGLISH);
    }

    @Test
    void allFxmlKeysResolve() throws Exception {
        URI root = I18n.class.getResource("/xyz/melnychuk/niimbotprint").toURI();
        if (!"file".equals(root.getScheme())) {
            return;
        }
        try (Stream<Path> paths = Files.walk(Paths.get(root))) {
            paths.filter(p -> p.toString().endsWith(".fxml")).forEach(this::assertKeysResolve);
        }
    }

    private void assertKeysResolve(Path fxml) {
        try {
            String content = Files.readString(fxml);
            Set<String> missing = new HashSet<>();
            Matcher matcher = KEY_PATTERN.matcher(content);
            while (matcher.find()) {
                String key = matcher.group(1);
                try {
                    I18n.getBundle().getString(key);
                } catch (MissingResourceException e) {
                    missing.add(key);
                }
            }
            assertTrue(missing.isEmpty(), fxml.getFileName() + ": missing keys " + missing);
        } catch (IOException e) {
            fail(e);
        }
    }
}
