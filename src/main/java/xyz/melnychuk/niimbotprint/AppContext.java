package xyz.melnychuk.niimbotprint;

import lombok.Builder;
import lombok.Getter;
import xyz.melnychuk.niimblue.NiimBlueApiManager;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.EditorHistoryService;
import xyz.melnychuk.niimbotprint.service.EditorService;
import xyz.melnychuk.niimbotprint.service.PrinterService;
import xyz.melnychuk.niimbotprint.service.StickerService;

@Getter
@Builder
public class AppContext {

    private final Sticker sticker;

    private final StickerService stickerService;
    private final EditorService editorService;
    private final EditorHistoryService editorHistoryService;
    private final PrinterService printerService;

    public static AppContext create(NiimBlueApiManager apiManager) {
        Sticker sticker = new Sticker();
        return builder()
                .sticker(sticker)
                .stickerService(new StickerService())
                .editorService(new EditorService())
                .editorHistoryService(new EditorHistoryService(sticker))
                .printerService(new PrinterService(apiManager.getApi()))
                .build();
    }
}
