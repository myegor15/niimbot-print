package xyz.melnychuk.niimbotprint.ui.canvas;

import java.util.List;

public record ResizeHandle(int sideX, int sideY) {

    public boolean isCorner() {
        return sideX != 0 && sideY != 0;
    }

    public static List<ResizeHandle> corners() {
        return List.of(
                new ResizeHandle(-1, -1),
                new ResizeHandle(1, -1),
                new ResizeHandle(1, 1),
                new ResizeHandle(-1, 1)
        );
    }

    public static List<ResizeHandle> cornersAndEdges() {
        return List.of(
                new ResizeHandle(-1, -1),
                new ResizeHandle(0, -1),
                new ResizeHandle(1, -1),
                new ResizeHandle(1, 0),
                new ResizeHandle(1, 1),
                new ResizeHandle(0, 1),
                new ResizeHandle(-1, 1),
                new ResizeHandle(-1, 0)
        );
    }
}
