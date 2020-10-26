package org.diehl.wedoogift.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Wallet {

    private Integer id;
    private String name;
    private WalletType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet)) return false;
        Wallet wallet = (Wallet) o;
        return getId().equals(wallet.getId()) &&
                getName().equals(wallet.getName()) &&
                getType().name().equals(wallet.getType().name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getType().name());
    }
}
