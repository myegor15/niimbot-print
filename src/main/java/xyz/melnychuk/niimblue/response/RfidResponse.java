package xyz.melnychuk.niimblue.response;

public record RfidResponse(RfidInfo paperRfidInfo,
                           RfidInfo ribbonRfidInfo) {

    public record RfidInfo(boolean tagPresent, String uuid, String barCode,
                           String serialNumber, Integer allPaper, Integer usedPaper,
                           String consumablesType, Integer capacity) {
    }
}
