package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonRootName(value = "user")
public class VoucherUser {

    private Integer id;
    private List<VoucherBalance> balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoucherUser)) return false;
        VoucherUser user = (VoucherUser) o;
        boolean isSameBalanceWalletId = balance.stream()
                .map(VoucherBalance::getWalletId)
                .allMatch(walletId -> user.getBalance().stream().map(VoucherBalance::getWalletId).anyMatch(walletId::equals));
        boolean isSameBalanceAmount = balance.stream()
                .map(VoucherBalance::getAmount)
                .allMatch(amount -> user.getBalance().stream().map(VoucherBalance::getAmount).anyMatch(amount::equals));
        return getId().equals(user.getId()) &&
                isSameBalanceWalletId && isSameBalanceAmount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
