package xyz.melnychuk.niimbotprint.i18n.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.i18n.Message;

@Getter
@RequiredArgsConstructor
public enum PrinterMessage implements Message {

    PANEL_PRINTER("panel.printer"),
    PANEL_PRINT("panel.print"),
    STATUS_CONNECTED("status.connected"),
    STATUS_DISCONNECTED("status.disconnected"),
    LABEL_DENSITY("label.density"),
    LABEL_QUANTITY("label.quantity"),
    LABEL_MODEL("label.model"),
    LABEL_WIDTH("label.width"),
    LABEL_HEIGHT("label.height"),
    LABEL_WIDTH_PX("label.widthPx"),
    LABEL_HEIGHT_PX("label.heightPx"),
    MESSAGE_SCANNING("message.scanning"),
    MESSAGE_DEVICES_FOUND("message.devicesFound"),
    MESSAGE_CONNECTED_TO("message.connectedTo"),
    MESSAGE_CONNECT_FAILED("message.connectFailed"),
    MESSAGE_DISCONNECTED("message.disconnected"),
    MESSAGE_PRINT_SENT("message.printSent"),
    PRINTER_INFO("printer.info"),
    TOOLTIP_REFRESH("tooltip.refresh"),
    TOOLTIP_DISCONNECT("tooltip.disconnect"),
    DENSITY_LIGHT("density.light"),
    DENSITY_NORMAL("density.normal"),
    DENSITY_DARK("density.dark");

    private final String key;
}
