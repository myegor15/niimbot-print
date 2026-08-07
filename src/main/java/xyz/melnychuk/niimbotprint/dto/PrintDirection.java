package xyz.melnychuk.niimbotprint.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrintDirection {

    TOP("top"),
    LEFT("left");

    private final String value;

}
