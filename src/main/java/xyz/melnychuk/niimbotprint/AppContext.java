package xyz.melnychuk.niimbotprint;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.melnychuk.niimblue.NiimBlueApiManager;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.EditorHistoryService;
import xyz.melnychuk.niimbotprint.service.EditorService;
import xyz.melnychuk.niimbotprint.service.PrinterService;
import xyz.melnychuk.niimbotprint.service.StickerService;

import java.util.Objects;

@Getter
public class AppContext {

    @Getter(AccessLevel.PACKAGE)
    private final NiimBlueApiManager apiManager;

    private final Sticker sticker;

    private final StickerService stickerService;
    private final EditorService editorService;
    private final EditorHistoryService editorHistoryService;
    private final PrinterService printerService;

    public AppContext(NiimBlueApiManager apiManager) {
        this.apiManager = Objects.requireNonNull(apiManager);
        this.sticker = new Sticker();
        this.stickerService = new StickerService();
        this.editorService = new EditorService();
        this.editorHistoryService = new EditorHistoryService(sticker);
        this.printerService = new PrinterService(apiManager.getApi());
    }
}
