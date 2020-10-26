package org.diehl.wedoogift.domain.service;

import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.WalletType;

import java.io.IOException;
import java.text.ParseException;

public interface VouchersService {

    void giveVoucher(WalletType walletType, Integer amount, String startDate, Integer companyId, Integer userId) throws InsufficientFundsException, IOException, ParseException;

    Vouchers searchLastUpdate() throws IOException;

    Integer calculateUserBalance(WalletType walletType, Integer userId) throws IOException;
}
