package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;

public class TextElementView implements ElementView {

    private final TextElement element;
    private final Label label = new Label();
    private double baseFontSize;

    public TextElementView(TextElement element) {
        this.element = element;
        label.setPadding(Insets.EMPTY);
        refresh();
        applyPosition();
    }

    @Override
    public StickerElement element() {
        return element;
    }

    @Override
    public Node node() {
        return label;
    }

    @Override
    public void applyPosition() {
        label.setLayoutX(element.getX());
        label.setLayoutY(element.getY());
    }

    @Override
    public void refresh() {
        label.setText(element.getText());
        label.setFont(Font.font(element.getFontFamily(),
                element.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                element.getFontSize()));
    }

    @Override
    public void beginResize() {
        baseFontSize = element.getFontSize();
    }

    @Override
    public void resize(double scale, double newX, double newY) {
        element.setX(newX);
        element.setY(newY);
        element.setFontSize((int) Math.round(Math.max(1, baseFontSize * scale)));
        refresh();
        applyPosition();
    }
}
