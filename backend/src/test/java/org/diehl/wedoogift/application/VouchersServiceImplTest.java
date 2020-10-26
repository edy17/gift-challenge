package org.diehl.wedoogift.application;

import org.diehl.wedoogift.domain.model.Company;
import org.diehl.wedoogift.domain.model.VoucherBalance;
import org.diehl.wedoogift.domain.model.VoucherDistribution;
import org.diehl.wedoogift.domain.model.VoucherUser;
import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.WalletType;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.diehl.wedoogift.domain.service.InsufficientFundsException;
import org.diehl.wedoogift.domain.service.VouchersService;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VouchersServiceImplTest {

    @Autowired
    private VouchersService vouchersService;

    @Autowired
    private FileRepository<Vouchers> vouchersRepository;

    @Value("${data.voucher.expected.file}")
    private String expectedFileName;

    @Value("${data.voucher.output.file}")
    private String outputFileName;

    @Value("${data.voucher.input.file}")
    private String inputFileName;

    @Before
    public void initOutput() {
        if (Paths.get(outputFileName).toFile().exists()) {
            assumeTrue("File " + outputFileName + " must not exist",
                    Paths.get(outputFileName).toFile().delete());
        }
        assumeTrue("File " + inputFileName + " must exist", Paths.get(inputFileName).toFile().exists());
    }

    @Test
    public void giveFoodVoucherTest() {
        try {
            Vouchers input = this.vouchersService.searchLastUpdate();
            Company inputCompany = input.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherUser inputUser = input.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherBalance inputVoucherBalance = inputUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.FOOD.getId()))
                    .findFirst()
                    .orElse(new VoucherBalance(WalletType.FOOD.getId(), 0));
            LocalDate today = LocalDate.now();
            String expectedStartDate = today.format(JsonUtil.formatter);
            String expectedEndDate = today.plusYears(1).withMonth(Month.FEBRUARY.getValue())
                    .with(TemporalAdjusters.lastDayOfMonth()).format(JsonUtil.formatter);
            Integer expectedAmount = inputCompany.getBalance();
            vouchersService.giveVoucher(WalletType.FOOD, expectedAmount,
                    today.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            Vouchers result = this.vouchersService.searchLastUpdate();
            VoucherDistribution resultedDistribution = this.getDistribution(result.getDistributions().size(), result)
                    .orElseThrow(IllegalStateException::new);
            String resultedStartDate = resultedDistribution.getStartDate().format(JsonUtil.formatter);
            String resultedEndDate = resultedDistribution.getEndDate().format(JsonUtil.formatter);
            Company resultedCompany = this.getCompany(inputCompany.getId(), result).orElseThrow(IllegalStateException::new);
            VoucherUser resultedUser = this.getUser(inputUser.getId(), result).orElseThrow(IllegalStateException::new);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must start " + expectedStartDate,
                    expectedStartDate, resultedStartDate);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must end on last day of February of next  year",
                    expectedEndDate, resultedEndDate);
            assertEquals("Company " + resultedCompany.getId() + " must lose " + expectedAmount,
                    Integer.valueOf(inputCompany.getBalance() - expectedAmount),
                    resultedCompany.getBalance());

            VoucherBalance expectedVoucherBalance = new VoucherBalance(WalletType.FOOD.getId(),
                    inputVoucherBalance.getAmount() + expectedAmount);
            VoucherBalance resultedVoucherBalance = resultedUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.FOOD.getId()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            assertEquals("User " + resultedUser.getId() + " must receive voucher " + WalletType.FOOD.name()
                            + " of " + expectedVoucherBalance.getAmount(),
                    expectedVoucherBalance,
                    resultedVoucherBalance);
        } catch (IOException | ParseException | InsufficientFundsException e) {
            fail(e.toString());
        }
    }

    @Test
    public void giveGiftVoucherTest() {
        try {
            Vouchers input = this.vouchersService.searchLastUpdate();
            Company inputCompany = input.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherUser inputUser = input.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherBalance inputVoucherBalance = inputUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.GIFT.getId()))
                    .findFirst()
                    .orElse(new VoucherBalance(WalletType.GIFT.getId(), 0));
            LocalDate today = LocalDate.now();
            String expectedStartDate = today.format(JsonUtil.formatter);
            String expectedEndDate = today.plusYears(1).minusDays(1).format(JsonUtil.formatter);
            Integer expectedAmount = inputCompany.getBalance();
            vouchersService.giveVoucher(WalletType.GIFT, expectedAmount,
                    today.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            Vouchers result = this.vouchersService.searchLastUpdate();
            VoucherDistribution resultedDistribution = this.getDistribution(result.getDistributions().size(), result)
                    .orElseThrow(IllegalStateException::new);
            String resultedStartDate = resultedDistribution.getStartDate().format(JsonUtil.formatter);
            String resultedEndDate = resultedDistribution.getEndDate().format(JsonUtil.formatter);
            Company resultedCompany = this.getCompany(inputCompany.getId(), result).orElseThrow(IllegalStateException::new);
            VoucherUser resultedUser = this.getUser(inputUser.getId(), result).orElseThrow(IllegalStateException::new);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must start " + expectedStartDate,
                    expectedStartDate, resultedStartDate);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must have a lifespan of 365 days",
                    expectedEndDate, resultedEndDate);
            assertEquals("Company " + resultedCompany.getId() + " must lose " + expectedAmount,
                    Integer.valueOf(inputCompany.getBalance() - expectedAmount),
                    resultedCompany.getBalance());

            VoucherBalance expectedVoucherBalance = new VoucherBalance(WalletType.GIFT.getId(),
                    inputVoucherBalance.getAmount() + expectedAmount);
            VoucherBalance resultedVoucherBalance = resultedUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.GIFT.getId()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            assertEquals("User " + resultedUser.getId() + " must receive voucher " + WalletType.GIFT.name()
                            + " of " + expectedVoucherBalance.getAmount(),
                    expectedVoucherBalance,
                    resultedVoucherBalance);
        } catch (IOException | ParseException | InsufficientFundsException e) {
            fail(e.toString());
        }
    }

    @Test(expected = InsufficientFundsException.class)
    public void giveVoucherWhenInsufficientFundsExceptionTest() throws InsufficientFundsException {
        try {
            Vouchers inputEndowments = this.vouchersService.searchLastUpdate();
            Company company = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherUser user = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            Integer requestAmount = company.getBalance() + 1;
            assertTrue("Company must request insufficient amount", requestAmount > company.getBalance());
            vouchersService.giveVoucher(WalletType.GIFT, requestAmount,
                    LocalDate.now().format(JsonUtil.formatter),
                    company.getId(),
                    user.getId());
        } catch (IOException | ParseException e) {
            fail(e.toString());
        }
    }

    @Test
    public void searchLastUpdateIfOutputFileIsAbsentTest() {
        assertTrue("File " + outputFileName + " must not exist", !Paths.get(outputFileName).toFile().exists());
        assertTrue("File " + inputFileName + " must exist", Paths.get(inputFileName).toFile().exists());
        try {
            Vouchers expectedEndowments = this.vouchersRepository.searchByFilePath(inputFileName);
            Vouchers recentEndowments = this.vouchersService.searchLastUpdate();

            assertEquals("File " + inputFileName + " and last update must have same distributions size"
                    , expectedEndowments.getDistributions().size()
                    , recentEndowments.getDistributions().size());
            assertEquals("File " + inputFileName + " and last update must have same companies size"
                    , expectedEndowments.getCompanies().size()
                    , recentEndowments.getCompanies().size());
            assertEquals("File " + inputFileName + " and last update must have same users size"
                    , expectedEndowments.getUsers().size()
                    , recentEndowments.getUsers().size());

            List<VoucherDistribution> expectedDistributions = expectedEndowments.getDistributions();
            expectedDistributions.forEach(expectedDistribution -> {
                VoucherDistribution resultedDistribution = this.getDistribution(expectedDistribution.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + inputFileName + " and last update must have same distributions"
                        , expectedDistribution, resultedDistribution);
            });
            List<Company> expectedCompanies = expectedEndowments.getCompanies();
            expectedCompanies.forEach(expectedCompany -> {
                Company resultedCompany = this.getCompany(expectedCompany.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + inputFileName + " and last update must have same companies"
                        , expectedCompany, resultedCompany);
            });
            List<VoucherUser> expectedUsers = expectedEndowments.getUsers();
            expectedUsers.forEach(expectedUser -> {
                VoucherUser resultedUser = this.getUser(expectedUser.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + inputFileName + " and last update must have same users"
                        , expectedUser, resultedUser);
            });
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void searchLastUpdateIfOutputFileIsPresentTest() {
        try {
            assertTrue("File " + outputFileName + " must not exist", !Paths.get(outputFileName).toFile().exists());
            assertTrue("File " + inputFileName + " must exist", Paths.get(inputFileName).toFile().exists());
            Vouchers inputEndowments = this.vouchersRepository.searchByFilePath(inputFileName);
            Company company = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherUser user = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            vouchersService.giveVoucher(WalletType.GIFT, company.getBalance() - 100,
                    LocalDate.now().format(JsonUtil.formatter),
                    company.getId(),
                    user.getId());
            assertTrue("File " + outputFileName + " must exist", Paths.get(outputFileName).toFile().exists());
            Vouchers expectedEndowments = this.vouchersRepository.searchByFilePath(outputFileName);
            Vouchers recentEndowments = this.vouchersService.searchLastUpdate();
            assertEquals("File " + outputFileName + " and last update must have same distributions size"
                    , expectedEndowments.getDistributions().size()
                    , recentEndowments.getDistributions().size());
            assertEquals("File " + outputFileName + " and last update must have same companies size"
                    , expectedEndowments.getCompanies().size()
                    , recentEndowments.getCompanies().size());
            assertEquals("File " + outputFileName + " and last update must have same users size"
                    , expectedEndowments.getUsers().size()
                    , recentEndowments.getUsers().size());

            expectedEndowments.getDistributions().forEach(expectedDistribution -> {
                VoucherDistribution resultedDistribution = this.getDistribution(expectedDistribution.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + outputFileName + " and last update must have same distributions"
                        , expectedDistribution, resultedDistribution);
            });
            expectedEndowments.getCompanies().forEach(expectedCompany -> {
                Company resultedCompany = this.getCompany(expectedCompany.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + outputFileName + " and last update must have same companies"
                        , expectedCompany, resultedCompany);
            });
            expectedEndowments.getUsers().forEach(expectedUser -> {
                VoucherUser resultedUser = this.getUser(expectedUser.getId(), recentEndowments)
                        .orElseThrow(IllegalStateException::new);
                assertEquals("File " + outputFileName + " and last update must have same users"
                        , expectedUser, resultedUser);
            });
        } catch (IOException | ParseException | InsufficientFundsException e) {
            fail(e.toString());
        }
    }

    @Test
    public void calculateUserBalanceTest() {
        assertTrue("File " + outputFileName + " must not exist", !Paths.get(outputFileName).toFile().exists());
        assertTrue("File " + inputFileName + " must exist", Paths.get(inputFileName).toFile().exists());
        try {
            Vouchers inputEndowments = this.vouchersService.searchLastUpdate();
            assertEquals("Distributions must empty", 0, inputEndowments.getDistributions().size());

            Company inputCompany = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            VoucherUser inputUser = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);

            LocalDate dateOfActiveVoucher = LocalDate.now();

            Integer activeVoucherAmount = (inputCompany.getBalance() / 4);
            Integer expiredVoucherAmount = (inputCompany.getBalance() / 4);
            vouchersService.giveVoucher(WalletType.GIFT, activeVoucherAmount,
                    dateOfActiveVoucher.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            vouchersService.giveVoucher(WalletType.FOOD, activeVoucherAmount,
                    dateOfActiveVoucher.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            assertTrue("Vouchers of " + (activeVoucherAmount * 2) + " is active for user " + inputUser.getId(),
                    LocalDate.now().isBefore(dateOfActiveVoucher.plusYears(1).minusDays(1)) &&
                            LocalDate.now().isBefore(dateOfActiveVoucher.plusYears(1).withMonth(Month.FEBRUARY.getValue())
                                    .with(TemporalAdjusters.lastDayOfMonth())));

            LocalDate dateOfExpiredVoucher = dateOfActiveVoucher.minusYears(2);
            vouchersService.giveVoucher(WalletType.GIFT, expiredVoucherAmount,
                    dateOfExpiredVoucher.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            vouchersService.giveVoucher(WalletType.FOOD, expiredVoucherAmount,
                    dateOfExpiredVoucher.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            assertTrue("Gift voucher of " + (expiredVoucherAmount * 2) + " has expired for user " + inputUser.getId(),
                    LocalDate.now().isAfter(dateOfExpiredVoucher.plusYears(1).minusDays(1)) &&
                            LocalDate.now().isAfter(dateOfExpiredVoucher.plusYears(1).withMonth(Month.FEBRUARY.getValue())
                                    .with(TemporalAdjusters.lastDayOfMonth())));

            VoucherBalance inputGiftVoucher = inputUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.GIFT.getId()))
                    .findFirst()
                    .orElse(new VoucherBalance(WalletType.GIFT.getId(), 0));
            VoucherBalance inputFoodVoucher = inputUser.getBalance().stream()
                    .filter(voucherBalance -> voucherBalance.getWalletId().equals(WalletType.FOOD.getId()))
                    .findFirst()
                    .orElse(new VoucherBalance(WalletType.FOOD.getId(), 0));

            Integer resultedGiftVoucherBalance = this.vouchersService.calculateUserBalance(WalletType.GIFT, inputUser.getId());
            Integer resultedFoodVoucherBalance = this.vouchersService.calculateUserBalance(WalletType.FOOD, inputUser.getId());

            assertNotEquals("User " + inputUser.getId() + " must have not amount of all gift distributions in gift voucher",
                    (inputGiftVoucher.getAmount() + activeVoucherAmount + expiredVoucherAmount),
                    (int) resultedGiftVoucherBalance);
            assertEquals("User " + inputUser.getId() + " must have amount of all active gift distributions in gift voucher",
                    (inputGiftVoucher.getAmount() + activeVoucherAmount),
                    (int) resultedGiftVoucherBalance);

            assertNotEquals("User " + inputUser.getId() + " must have not amount of all food distributions in food voucher",
                    (inputFoodVoucher.getAmount() + activeVoucherAmount + expiredVoucherAmount),
                    (int) resultedFoodVoucherBalance);
            assertEquals("User " + inputUser.getId() + " must have amount of all active food distributions in food voucher",
                    (inputFoodVoucher.getAmount() + activeVoucherAmount),
                    (int) resultedFoodVoucherBalance);
        } catch (IOException | ParseException | InsufficientFundsException e) {
            fail(e.toString());
        }
    }

    @After
    public void generateOutput() {
        try {
            if (Paths.get(outputFileName).toFile().exists()) {
                assumeTrue("File " + outputFileName + " must not exist",
                        Paths.get(outputFileName).toFile().delete());
            }
            assumeTrue("File " + inputFileName + " must exist",
                    Paths.get(inputFileName).toFile().exists());
            assumeTrue("File " + expectedFileName + " must exist",
                    Paths.get(expectedFileName).toFile().exists());
            Vouchers expected = this.vouchersRepository.searchByFilePath(expectedFileName);
            expected.getDistributions().forEach(expectedDistribution -> {
                String expectedStartDate = expectedDistribution.getStartDate().format(JsonUtil.formatter);
                try {
                    vouchersService.giveVoucher(WalletType.getValueOf(expectedDistribution.getWalletId()),
                            expectedDistribution.getAmount(),
                            expectedStartDate,
                            expectedDistribution.getCompanyId(),
                            expectedDistribution.getUserId());
                } catch (IOException | ParseException | InsufficientFundsException e) {
                    assumeNoException(e);
                }
            });
            Vouchers result = this.vouchersService.searchLastUpdate();
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same distributions size",
                    expected.getDistributions().size() == result.getDistributions().size());
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same companies size",
                    expected.getCompanies().size() == result.getCompanies().size());
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same users size",
                    expected.getUsers().size() == result.getUsers().size());
            List<VoucherDistribution> expectedDistributions = expected.getDistributions();
            expectedDistributions.forEach(expectedDistribution -> {
                VoucherDistribution resultedDistribution = this.getDistribution(expectedDistribution.getId(),
                        result).orElseThrow(IllegalStateException::new);
                assumeTrue("Files " + outputFileName + " and " + expectedFileName
                                + " must have same distributions",
                        expectedDistribution.equals(resultedDistribution));
            });
            List<Company> expectedCompanies = expected.getCompanies();
            expectedCompanies.forEach(expectedCompany -> {
                Company resultedCompany = this.getCompany(expectedCompany.getId(), result)
                        .orElseThrow(IllegalStateException::new);
                assumeTrue("Files " + outputFileName + " and " + expectedFileName
                        + " must have same companies", expectedCompany.equals(resultedCompany));
            });
            List<VoucherUser> expectedUsers = expected.getUsers();
            expectedUsers.forEach(expectedUser -> {
                VoucherUser resultedUser = this.getUser(expectedUser.getId(), result)
                        .orElseThrow(IllegalStateException::new);
                assumeTrue("Files " + outputFileName + " and " + expectedFileName
                        + " must have same users", expectedUser.equals(resultedUser));
            });
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    private Optional<VoucherDistribution> getDistribution(Integer id, Vouchers endowments) {
        List<VoucherDistribution> distributions = endowments.getDistributions();
        return distributions.stream().filter(company -> company.getId().equals(id))
                .findFirst();
    }

    private Optional<Company> getCompany(Integer id, Vouchers endowments) {
        List<Company> companies = endowments.getCompanies();
        return companies.stream().filter(company -> company.getId().equals(id)).findFirst();
    }

    private Optional<VoucherUser> getUser(Integer id, Vouchers endowments) {
        List<VoucherUser> users = endowments.getUsers();
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }
}
