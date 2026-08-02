package xyz.melnychuk.niimbotprint.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimblue.response.DevicesResponse;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeviceMapper {

    public static DeviceDto toDto(DevicesResponse.Device device) {
        return new DeviceDto(device.name(), device.address());
    }
}
