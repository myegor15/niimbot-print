package xyz.melnychuk.niimbotprint.ui.canvas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapEngine {

    private static final double SNAP_THRESHOLD = 6;
    private static final double ROTATION_SNAP_STEP = 15;
    private static final double ROTATION_SNAP_THRESHOLD = 5;
    private static final double[] SNAP_OFFSETS = {0, 0.5, 1};

    public record AxisSnap(double coord, Double guide) {
    }

    public record SnapResult(double x, double y, Double vGuide, Double hGuide) {
    }

    public static SnapResult snapPosition(double x, double y, double sizeW, double sizeH,
                                          double maxW, double maxH) {
        AxisSnap v = snapAxis(x, sizeW, maxW);
        AxisSnap h = snapAxis(y, sizeH, maxH);
        return new SnapResult(v.coord(), h.coord(), v.guide(), h.guide());
    }

    private static AxisSnap snapAxis(double coord, double size, double max) {
        double best = Double.MAX_VALUE;
        double bestCoord = coord;
        Double bestTarget = null;
        double[] targets = {max / 2, 0, max};
        for (double offset : SNAP_OFFSETS) {
            double ref = coord + offset * size;
            for (double target : targets) {
                double distance = Math.abs(ref - target);
                if (distance < SNAP_THRESHOLD && distance < best) {
                    best = distance;
                    bestTarget = target;
                    bestCoord = target - offset * size;
                }
            }
        }
        boolean snapped = best < SNAP_THRESHOLD && bestCoord >= 0 && bestCoord + size <= max;
        return new AxisSnap(snapped ? bestCoord : coord, snapped ? bestTarget : null);
    }

    public static double snapRotation(double degrees) {
        double nearest = Math.round(degrees / ROTATION_SNAP_STEP) * ROTATION_SNAP_STEP;
        return Math.abs(degrees - nearest) <= ROTATION_SNAP_THRESHOLD ? normalizeDegrees(nearest) : degrees;
    }

    private static double normalizeDegrees(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}