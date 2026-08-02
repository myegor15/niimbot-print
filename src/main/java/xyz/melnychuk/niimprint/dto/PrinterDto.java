package xyz.melnychuk.niimprint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrinterDto {

    private String model;
    private Integer dpi;
    private String detectedPrintTask;
    private String serial;
    private String mac;
    private Integer charge;
    private String softwareVersion;

}
