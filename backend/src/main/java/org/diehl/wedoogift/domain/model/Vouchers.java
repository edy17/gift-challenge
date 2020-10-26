package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Vouchers {

    private List<Wallet> wallets;
    private List<Company> companies;
    private List<VoucherUser> users;
    private List<VoucherDistribution> distributions;
}
