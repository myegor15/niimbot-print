package xyz.melnychuk.niimbotprint.ui.canvas.element;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import xyz.melnychuk.niimbotprint.model.Text;

public class TextView extends AbstractElementView<Text> {

    private final Label label = new Label();
    private double baseFontSize;

    public TextView(Text element) {
        super(element);
        refreshNode();
    }

    @Override
    public Node node() {
        return label;
    }

    @Override
    public void applyPosition() {
        label.setLayoutX(element().getX());
        label.setLayoutY(element().getY());
    }

    @Override
    public void refresh() {
        label.setPadding(Insets.EMPTY);
        label.setText(element().getText());
        label.setFont(Font.font(element().getFontFamily().getDisplayName(),
                element().isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                element().isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                element().getFontSize()));
        label.setUnderline(element().isUnderline());
    }

    @Override
    public void beginResize() {
        super.beginResize();
        baseFontSize = element().getFontSize();
    }

    @Override
    protected void applySize(double newWidth, double newHeight, double newX, double newY) {
        element().setX(newX);
        element().setY(newY);
        int fontSize = (int) Math.round(baseFontSize * (newWidth / Math.max(1, baseW)));
        element().setFontSize(Math.max(1, fontSize));
        refreshNode();
    }
}
