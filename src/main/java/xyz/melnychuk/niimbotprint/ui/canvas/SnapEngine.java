package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.scene.shape.Line;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapEngine {

    private static final double SNAP_THRESHOLD = 6;
    private static final double ROTATION_SNAP_STEP = 15;
    private static final double ROTATION_SNAP_THRESHOLD = 5;
    private static final double[] SNAP_OFFSETS = {0, 0.5, 1};

    public static double[] snapPosition(double x, double y, double sizeW, double sizeH,
                                        double maxW, double maxH, Line vGuide, Line hGuide) {
        double sx = snapAxis(x, sizeW, maxW, vGuide, true, maxW, maxH);
        double sy = snapAxis(y, sizeH, maxH, hGuide, false, maxW, maxH);
        return new double[]{sx, sy};
    }

    private static double snapAxis(double coord, double size, double max, Line guide,
                                   boolean vertical, double w, double h) {
        double best = Double.MAX_VALUE;
        double bestCoord = coord;
        double bestTarget = Double.NaN;
        double[] targets = {max / 2, 0, max};
        for (double offset : SNAP_OFFSETS) {
            double ref = coord + offset * size;
            for (double target : targets) {
                double distance = Math.abs(ref - target);
                if (distance < SNAP_THRESHOLD && distance < best) {
                    best = distance;
                    bestCoord = target - offset * size;
                    bestTarget = target;
                }
            }
        }
        if (best < SNAP_THRESHOLD) {
            placeGuide(guide, vertical, bestTarget, w, h);
            return bestCoord;
        }
        guide.setVisible(false);
        return coord;
    }

    private static void placeGuide(Line guide, boolean vertical, double target, double w, double h) {
        guide.setVisible(true);
        if (vertical) {
            guide.setStartX(target);
            guide.setEndX(target);
            guide.setStartY(0);
            guide.setEndY(h);
        } else {
            guide.setStartX(0);
            guide.setEndX(w);
            guide.setStartY(target);
            guide.setEndY(target);
        }
    }

    public static double snapRotation(double degrees) {
        double nearest = Math.round(degrees / ROTATION_SNAP_STEP) * ROTATION_SNAP_STEP;
        return Math.abs(degrees - nearest) <= ROTATION_SNAP_THRESHOLD ? normalizeDegrees(nearest) : degrees;
    }

    private static double normalizeDegrees(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}
