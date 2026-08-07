package xyz.melnychuk.niimbotprint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintTaskDto {

    private String imageBase64;
    private int width;
    private int height;
    private PrintDensity density;
    private int quantity;

}
