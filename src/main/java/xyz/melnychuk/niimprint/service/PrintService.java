package xyz.melnychuk.niimprint.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimprint.AppException;
import xyz.melnychuk.niimprint.dto.DeviceDto;
import xyz.melnychuk.niimprint.dto.PrinterDto;
import xyz.melnychuk.niimprint.dto.PrintTaskDto;
import xyz.melnychuk.niimprint.mapper.DeviceMapper;
import xyz.melnychuk.niimprint.mapper.PrinterInfoMapper;
import xyz.melnychuk.niimprint.mapper.PrintTaskMapper;

import java.util.List;

@Slf4j
public class PrintService {

    @Getter
    private final String apiUrl;
    private final NiimBlueApi api;

    public PrintService(String apiUrl) {
        this.apiUrl = apiUrl;
        this.api = new NiimBlueApi(apiUrl);
    }

    public boolean isConnected() {
        try {
            return api.isConnected();
        } catch (Exception e) {
            log.error("Exception in isConnected().", e);
            throw new AppException(e);
        }
    }

    public List<DeviceDto> scanDevices() {
        try {
            return api.scan()
                    .devices()
                    .stream()
                    .map(DeviceMapper::toDto)
                    .toList();
        } catch (Exception e) {
            log.error("Exception in scanDevices().", e);
            throw new AppException(e);
        }
    }

    public boolean connect(DeviceDto device) {
        try {
            String target = device.getAddress() == null || device.getAddress().isBlank()
                    ? device.getName()
                    : device.getAddress();
            api.connect("ble", target);
            return api.isConnected();
        } catch (Exception e) {
            log.error("Exception in connect().", e);
            throw new AppException(e);
        }
    }

    public void disconnect() {
        try {
            api.disconnect();
        } catch (Exception e) {
            log.error("Exception in disconnect().", e);
            throw new AppException(e);
        }
    }

    public PrinterDto getPrinterInfo() {
        try {
            return PrinterInfoMapper.toDto(api.info());
        } catch (Exception e) {
            log.error("Exception in getPrinterInfo().", e);
            throw new AppException(e);
        }
    }

    public void print(PrintTaskDto task) {
        try {
            api.print(PrintTaskMapper.toApi(task));
        } catch (Exception e) {
            log.error("Exception in print().", e);
            throw new AppException(e);
        }
    }
}
