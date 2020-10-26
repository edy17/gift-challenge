package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum WalletType {

    GIFT(1),
    FOOD(2),
    UNKNOWN(null);

    private Integer id;

    WalletType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static WalletType getValueOf(Integer id) {
        return Arrays.stream(WalletType.values())
                .filter(wallet -> id.equals(wallet.getId()))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @JsonCreator
    public static WalletType getValueOfName(String name) {
        return Arrays.stream(WalletType.values())
                .filter(wallet -> wallet.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
