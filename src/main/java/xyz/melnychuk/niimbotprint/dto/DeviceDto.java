package xyz.melnychuk.niimbotprint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceDto {

    private String name;
    private String address;

    @Override
    public String toString() {
        return name == null || name.isBlank() ? address : name;
    }

}
