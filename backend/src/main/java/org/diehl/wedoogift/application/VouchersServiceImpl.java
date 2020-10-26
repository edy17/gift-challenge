package org.diehl.wedoogift.application;

import org.diehl.wedoogift.domain.model.Company;
import org.diehl.wedoogift.domain.model.VoucherBalance;
import org.diehl.wedoogift.domain.model.VoucherDistribution;
import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.VoucherUser;
import org.diehl.wedoogift.domain.model.WalletType;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.diehl.wedoogift.domain.service.InsufficientFundsException;
import org.diehl.wedoogift.domain.service.VouchersService;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VouchersServiceImpl implements VouchersService {

    private final Logger logger = LoggerFactory.getLogger(VouchersServiceImpl.class);

    private FileRepository<Vouchers> vouchersRepository;

    @Value("${data.voucher.input.file}")
    private String inputFileName;

    @Value("${data.voucher.output.file}")
    private String outputFileName;

    public VouchersServiceImpl(FileRepository<Vouchers> vouchersRepository) {
        this.vouchersRepository = vouchersRepository;
    }

    @Override
    public void giveVoucher(WalletType walletType, Integer amount, String startDate, Integer companyId, Integer userId) throws IOException, InsufficientFundsException {
        Vouchers endowments = this.searchLastUpdate();
        List<VoucherDistribution> distributions = endowments.getDistributions();
        List<Company> companies = endowments.getCompanies();
        List<VoucherUser> users = endowments.getUsers();
        Company company = companies.stream().filter(c -> c.getId().equals(companyId)).findFirst()
                .orElseThrow(IllegalStateException::new);
        if (amount > company.getBalance())
            throw new InsufficientFundsException(companyId, amount);
        VoucherUser user = users.stream().filter(u -> u.getId().equals(userId)).findFirst()
                .orElseThrow(IllegalStateException::new);
        LocalDate date = LocalDate.parse(startDate, JsonUtil.formatter);
        LocalDate endDate = null;
        if (walletType == WalletType.GIFT)
            endDate = date.plusYears(1).minusDays(1);
        else if (walletType == WalletType.FOOD) {
            endDate = date.plusYears(1)
                    .withMonth(Month.FEBRUARY.getValue())
                    .with(TemporalAdjusters.lastDayOfMonth());
        } else {
            throw new IllegalStateException("Unknown wallet type");
        }
        VoucherDistribution distribution = new VoucherDistribution(distributions.size() + 1, walletType.getId(),
                amount, date, endDate, companyId, userId);
        distributions.add(distribution);
        Company newCompany = new Company(company.getId(), company.getName(),
                company.getBalance() - amount);
        companies.set(companies.indexOf(company), newCompany);
        List<VoucherBalance> unaffectedVoucherBalances = user.getBalance().stream()
                .filter(v -> !v.getWalletId().equals(walletType.getId()))
                .collect(Collectors.toList());
        VoucherBalance affectedVoucherBalance = user.getBalance().stream()
                .filter(v -> v.getWalletId().equals(walletType.getId()))
                .findFirst().orElse(new VoucherBalance(walletType.getId(), 0));
        affectedVoucherBalance.setAmount(affectedVoucherBalance.getAmount() + amount);
        List<VoucherBalance> updatedBalance = new ArrayList<>(unaffectedVoucherBalances);
        updatedBalance.add(affectedVoucherBalance);
        VoucherUser updatedUser = new VoucherUser(user.getId(), updatedBalance);
        users.set(users.indexOf(user), updatedUser);
        Vouchers updatedEndowments = new Vouchers(null, companies, users, distributions);
        this.vouchersRepository.saveToFilePath(updatedEndowments, outputFileName);
        logger.info("The user {} received a gift of {} from the company {} on {}", userId, amount, companyId, startDate);
    }

    @Override
    public Integer calculateUserBalance(WalletType walletType, Integer userId) throws IOException {
        Vouchers endowments = this.searchLastUpdate();
        List<VoucherDistribution> distributions = endowments.getDistributions();
        List<VoucherUser> users = endowments.getUsers();
        VoucherBalance voucherBalance = users.stream().filter(u -> u.getId().equals(userId))
                .findFirst().orElseThrow(IllegalStateException::new)
                .getBalance().stream().filter(v -> v.getWalletId().equals(walletType.getId()))
                .findFirst().orElse(new VoucherBalance(walletType.getId(), 0));
        final Integer[] balance = {voucherBalance.getAmount()};
        final LocalDate today = LocalDate.now();
        distributions.stream()
                .filter(distribution -> distribution.getUserId().equals(userId)
                        && distribution.getWalletId().equals(walletType.getId())
                        && distribution.getEndDate().isBefore(today))
                .forEach(distribution -> balance[0] = balance[0] - distribution.getAmount());
        logger.info("The consumable balance of user {} has been calculated for {} voucher in the amount of {}",
                userId,
                walletType,
                balance[0]);
        return balance[0];
    }

    @Override
    public Vouchers searchLastUpdate() throws IOException {
        Path outputPath = Paths.get(outputFileName);
        Path inputPath = Paths.get(inputFileName);
        if (outputPath.toFile().exists()) {
            return this.vouchersRepository.searchByFilePath(outputFileName);
        } else if (inputPath.toFile().exists()) {
            return this.vouchersRepository.searchByFilePath(inputFileName);
        } else {
            throw new IllegalStateException("File " + inputFileName + " or file " + outputFileName + " must exit");
        }
    }
}
