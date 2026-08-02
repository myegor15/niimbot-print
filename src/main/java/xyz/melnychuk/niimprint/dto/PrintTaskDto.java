package xyz.melnychuk.niimprint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrintTaskDto {

    private String imageBase64;
    private int width;
    private int height;
    private int density;
    private int quantity;
    private String direction;

}
