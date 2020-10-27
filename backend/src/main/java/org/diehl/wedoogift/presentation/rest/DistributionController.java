package org.diehl.wedoogift.presentation.rest;

import org.diehl.wedoogift.domain.model.Endowments;
import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.WalletType;
import org.diehl.wedoogift.domain.service.EndowmentsService;
import org.diehl.wedoogift.domain.service.InsufficientFundsException;
import org.diehl.wedoogift.domain.service.VouchersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@CrossOrigin("*")
@RestController
public class DistributionController {

    private EndowmentsService endowmentsService;

    private VouchersService vouchersService;

    public DistributionController(EndowmentsService endowmentsService, VouchersService vouchersService) {
        this.endowmentsService = endowmentsService;
        this.vouchersService = vouchersService;
    }

    @GetMapping(path = "/level1/all")
    public ResponseEntity<Endowments> getRecentEndowments() throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(endowmentsService.searchLastUpdate());
    }

    @GetMapping(path = "/level1/give/{amount}/{startDate}/{companyId}/{userId}")
    public ResponseEntity<Integer> giveEndowment(@PathVariable Integer amount,
                                                @PathVariable String startDate,
                                                @PathVariable Integer companyId,
                                                @PathVariable Integer userId) throws InsufficientFundsException, ParseException, IOException {
        endowmentsService.giveEndowment(amount, startDate, companyId, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userId);
    }

    @GetMapping(path = "/level1/balance/{userId}")
    public ResponseEntity<Integer> getUserBalance(@PathVariable Integer userId) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(endowmentsService.calculateUserBalance(userId));
    }

    @GetMapping(path = "/level2/all")
    public ResponseEntity<Vouchers> getRecentVouchers() throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(vouchersService.searchLastUpdate());
    }

    @GetMapping(path = "/level2/give/{walletId}/{amount}/{startDate}/{companyId}/{userId}")
    public ResponseEntity<Integer> giveVoucher(@PathVariable Integer walletId,
                                              @PathVariable Integer amount,
                                              @PathVariable String startDate,
                                              @PathVariable Integer companyId,
                                              @PathVariable Integer userId)
            throws InsufficientFundsException, ParseException, IOException {
        vouchersService.giveVoucher(WalletType.getValueOf(walletId), amount, startDate, companyId, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userId);
    }

    @GetMapping(path = "/level2/balance/{walletId}/{userId}")
    public ResponseEntity<Integer> getUserVoucherBalance(@PathVariable Integer walletId,
                                                        @PathVariable Integer userId) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(vouchersService.calculateUserBalance(WalletType.getValueOf(walletId), userId));
    }
}
