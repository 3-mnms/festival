package com.teckit.festival.enumeration;

import java.util.Arrays;

public enum TicketPick {
    DELIVERY(0), ONSITE(1), BOTH(2);

    private final int code;

    TicketPick(int code) {
        this.code = code;
    }

    public static TicketPick fromCode(int code) {
        return Arrays.stream(values())
                .filter(p -> p.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid ticket pick"));
    }
}