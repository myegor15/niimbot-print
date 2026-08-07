package xyz.melnychuk.niimbotprint.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.dto.PrintDirection;
import xyz.melnychuk.niimblue.request.PrintRequest;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrintTaskMapper {

    public static PrintRequest toApi(PrintTaskDto task) {
        return PrintRequest.of(
                task.getImageBase64(), task.getWidth(), task.getHeight(),
                task.getDensity().getValue(), task.getQuantity(), PrintDirection.TOP.getValue()
        );
    }
}
