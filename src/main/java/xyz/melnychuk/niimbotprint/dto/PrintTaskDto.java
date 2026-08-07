package xyz.melnychuk.niimbotprint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PrintTaskDto {

    private String imageBase64;
    private int width;
    private int height;
    private PrintDensity density;
    private int quantity;

}
