package xyz.melnychuk.niimblue.response;

import java.util.List;

public record DevicesResponse(List<Device> devices) {

    public record Device(String name, String address) {
        @Override
        public String toString() {
            return name == null || name.isBlank() ? address : name;
        }
    }
}
