package xyz.melnychuk.niimblue.response;

import java.util.List;

public record InfoResponse(PrinterInfo printerInfo,
                           ModelMetadata modelMetadata,
                           String detectedPrintTask) {

    public record PrinterInfo(String connectResult, Integer protocolVersion, Integer modelId, String serial,
                              String mac, Integer charge, String autoShutdownTime, String labelType,
                              String softwareVersion, String hardwareVersion) {
    }

    public record ModelMetadata(String model, List<Integer> id, Integer dpi, String printDirection,
                                Integer printheadPixels, List<Integer> paperTypes, Integer densityMin,
                                Integer densityMax, Integer densityDefault) {
    }
}
