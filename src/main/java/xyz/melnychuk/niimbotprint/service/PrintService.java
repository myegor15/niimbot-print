package xyz.melnychuk.niimbotprint.service;

import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;
import xyz.melnychuk.niimbotprint.mapper.DeviceMapper;
import xyz.melnychuk.niimbotprint.mapper.PrintTaskMapper;
import xyz.melnychuk.niimbotprint.mapper.PrinterInfoMapper;

import java.util.List;
import java.util.Objects;

@Slf4j
public class PrintService {

    private final NiimBlueApi api;

    public PrintService(NiimBlueApi api) {
        this.api = Objects.requireNonNull(api);
    }

    public String getApiUrl() {
        return api.getUrl();
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
