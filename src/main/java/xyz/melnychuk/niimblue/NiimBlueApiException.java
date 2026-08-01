package xyz.melnychuk.niimblue;

public class NiimBlueApiException extends RuntimeException {

    public NiimBlueApiException(String message) {
        super(message);
    }

    public NiimBlueApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public NiimBlueApiException(Throwable cause) {
        super(cause);
    }
}
