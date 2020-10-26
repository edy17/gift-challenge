package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "wallet_id", "amount" })
public class VoucherBalance {

    @JsonProperty("wallet_id")
    private Integer walletId;
    private Integer amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoucherBalance)) return false;
        VoucherBalance voucherBalance = (VoucherBalance) o;
        return getWalletId().equals(voucherBalance.getWalletId()) &&
                getAmount().equals(voucherBalance.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWalletId(), getAmount());
    }
}
