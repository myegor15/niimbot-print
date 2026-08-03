package xyz.melnychuk.niimbotprint.ui;

import xyz.melnychuk.niimbotprint.model.StickerElement;

import java.util.function.Consumer;

public interface StickerEditor {

    StickerElement addElement(StickerElement element);

    void removeSelected();

    StickerElement getSelectedElement();

    void updateElement(StickerElement element);

    void refresh();

    void setLabelSize(int width, int height);

    void setGridVisible(boolean visible);

    boolean isGridVisible();

    void setPositionSnap(boolean enabled);

    boolean isPositionSnap();

    void setRotationSnap(boolean enabled);

    boolean isRotationSnap();

    String snapshotPngBase64();

    void setSelectionListener(Consumer<StickerElement> listener);

    void setChangeListener(Consumer<StickerElement> listener);
}
