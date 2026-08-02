package xyz.melnychuk.niimbotprint.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimblue.response.InfoResponse;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrinterInfoMapper {

    public static PrinterDto toDto(InfoResponse info) {
        if (info == null) {
            return null;
        }

        InfoResponse.PrinterInfo printer = info.printerInfo();
        InfoResponse.ModelMetadata model = info.modelMetadata();
        if (printer == null || model == null) {
            return null;
        }

        return new PrinterDto(
                model.model(), model.dpi(), info.detectedPrintTask(),
                printer.serial(), printer.mac(), printer.charge(), printer.softwareVersion()
        );
    }
}
