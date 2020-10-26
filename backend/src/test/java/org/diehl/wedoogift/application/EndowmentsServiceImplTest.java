package org.diehl.wedoogift.application;

import org.diehl.wedoogift.domain.model.Company;
import org.diehl.wedoogift.domain.model.Distribution;
import org.diehl.wedoogift.domain.model.Endowments;
import org.diehl.wedoogift.domain.model.User;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.diehl.wedoogift.domain.service.EndowmentsService;
import org.diehl.wedoogift.domain.service.InsufficientFundsException;
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
public class EndowmentsServiceImplTest {

    @Autowired
    private EndowmentsService endowmentsService;

    @Autowired
    private FileRepository<Endowments> endowmentsRepository;

    @Value("${data.expected.file}")
    private String expectedFileName;

    @Value("${data.output.file}")
    private String outputFileName;

    @Value("${data.input.file}")
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
    public void giveEndowmentTest() {
        try {
            Endowments input = this.endowmentsService.searchLastUpdate();
            Company inputCompany = input.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            User inputUser = input.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            LocalDate today = LocalDate.now();
            String expectedStartDate = today.format(JsonUtil.formatter);
            String expectedEndDate = today.plusYears(1).minusDays(1).format(JsonUtil.formatter);
            Integer expectedAmount = inputCompany.getBalance();
            endowmentsService.giveEndowment(expectedAmount,
                    today.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            Endowments result = this.endowmentsService.searchLastUpdate();
            Distribution resultedDistribution = this.getDistribution(result.getDistributions().size(), result)
                    .orElseThrow(IllegalStateException::new);
            String resultedStartDate = resultedDistribution.getStartDate().format(JsonUtil.formatter);
            String resultedEndDate = resultedDistribution.getEndDate().format(JsonUtil.formatter);
            Company resultedCompany = this.getCompany(inputCompany.getId(), result).orElseThrow(IllegalStateException::new);
            User resultedUser = this.getUser(inputUser.getId(), result).orElseThrow(IllegalStateException::new);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must start " + expectedStartDate,
                    expectedStartDate, resultedStartDate);
            assertEquals("Distribution " + resultedDistribution.getId() +
                            " in " + outputFileName + " must have a lifespan of 365 days",
                    expectedEndDate, resultedEndDate);
            assertEquals("Company " + resultedCompany.getId() + " must lose " + expectedAmount,
                    Integer.valueOf(inputCompany.getBalance() - expectedAmount),
                    resultedCompany.getBalance());
            assertEquals("User " + resultedUser.getId() + " must receive " + expectedAmount,
                    Integer.valueOf(inputUser.getBalance() + expectedAmount),
                    resultedUser.getBalance());
        } catch (IOException | ParseException | InsufficientFundsException e) {
            fail(e.toString());
        }
    }

    @Test(expected = InsufficientFundsException.class)
    public void giveEndowmentWhenInsufficientFundsExceptionTest() throws InsufficientFundsException {
        try {
            Endowments inputEndowments = this.endowmentsService.searchLastUpdate();
            Company company = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            User user = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            Integer requestAmount = company.getBalance() + 1;
            assertTrue("Company must request insufficient amount", requestAmount > company.getBalance());
            endowmentsService.giveEndowment(requestAmount,
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
            Endowments expectedEndowments = this.endowmentsRepository.searchByFilePath(inputFileName);
            Endowments recentEndowments = this.endowmentsService.searchLastUpdate();

            assertEquals("File " + inputFileName + " and last update must have same distributions size"
                    , expectedEndowments.getDistributions().size()
                    , recentEndowments.getDistributions().size());
            assertEquals("File " + inputFileName + " and last update must have same companies size"
                    , expectedEndowments.getCompanies().size()
                    , recentEndowments.getCompanies().size());
            assertEquals("File " + inputFileName + " and last update must have same users size"
                    , expectedEndowments.getUsers().size()
                    , recentEndowments.getUsers().size());

            List<Distribution> expectedDistributions = expectedEndowments.getDistributions();
            expectedDistributions.forEach(expectedDistribution -> {
                Distribution resultedDistribution = this.getDistribution(expectedDistribution.getId(), recentEndowments)
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
            List<User> expectedUsers = expectedEndowments.getUsers();
            expectedUsers.forEach(expectedUser -> {
                User resultedUser = this.getUser(expectedUser.getId(), recentEndowments)
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
            Endowments inputEndowments = this.endowmentsRepository.searchByFilePath(inputFileName);
            Company company = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            User user = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            endowmentsService.giveEndowment(company.getBalance() - 100,
                    LocalDate.now().format(JsonUtil.formatter),
                    company.getId(),
                    user.getId());
            assertTrue("File " + outputFileName + " must exist", Paths.get(outputFileName).toFile().exists());
            Endowments expectedEndowments = this.endowmentsRepository.searchByFilePath(outputFileName);
            Endowments recentEndowments = this.endowmentsService.searchLastUpdate();
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
                Distribution resultedDistribution = this.getDistribution(expectedDistribution.getId(), recentEndowments)
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
                User resultedUser = this.getUser(expectedUser.getId(), recentEndowments)
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
            Endowments inputEndowments = this.endowmentsService.searchLastUpdate();
            assertEquals("Distributions must empty", 0, inputEndowments.getDistributions().size());

            Company inputCompany = inputEndowments.getCompanies().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);
            User inputUser = inputEndowments.getUsers().stream().findFirst()
                    .orElseThrow(IllegalStateException::new);

            LocalDate dateOfActiveGift = LocalDate.now();
            Integer activeGiftAmount = (inputCompany.getBalance() / 3) - 1;
            endowmentsService.giveEndowment(activeGiftAmount,
                    dateOfActiveGift.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            assertTrue("Distribution of " + activeGiftAmount + " is active for user " + inputUser.getId(),
                    LocalDate.now().isBefore(dateOfActiveGift.plusYears(1).minusDays(1)));

            LocalDate dateOfExpiredGift = dateOfActiveGift.minusYears(2);
            Integer expiredGifAmount = (inputCompany.getBalance() / 3) - 1;
            endowmentsService.giveEndowment(expiredGifAmount,
                    dateOfExpiredGift.format(JsonUtil.formatter),
                    inputCompany.getId(),
                    inputUser.getId());
            assertTrue("Distribution of " + expiredGifAmount + " has expired for user " + inputUser.getId(),
                    LocalDate.now().isAfter(dateOfExpiredGift.plusYears(1).minusDays(1)));

            Integer resultedBalance = this.endowmentsService.calculateUserBalance(inputUser.getId());
            assertNotEquals("User " + inputUser.getId() + " must have not amount of all distributions",
                    (inputUser.getBalance() + activeGiftAmount + expiredGifAmount),
                    (int) resultedBalance);
            assertEquals("User " + inputUser.getId() + " must have amount of all active distributions",
                    (inputUser.getBalance() + activeGiftAmount),
                    (int) resultedBalance);
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
            Endowments expected = this.endowmentsRepository.searchByFilePath(expectedFileName);
            expected.getDistributions().forEach(expectedDistribution -> {
                String expectedStartDate = expectedDistribution.getStartDate().format(JsonUtil.formatter);
                try {
                    endowmentsService.giveEndowment(expectedDistribution.getAmount(),
                            expectedStartDate,
                            expectedDistribution.getCompanyId(),
                            expectedDistribution.getUserId());
                } catch (IOException | ParseException | InsufficientFundsException e) {
                    assumeNoException(e);
                }
            });
            Endowments result = this.endowmentsService.searchLastUpdate();
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same distributions size",
                    expected.getDistributions().size() == result.getDistributions().size());
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same companies size",
                    expected.getCompanies().size() == result.getCompanies().size());
            assumeTrue("Files " + outputFileName + " and " + expectedFileName
                            + " must have same users size",
                    expected.getUsers().size() == result.getUsers().size());
            List<Distribution> expectedDistributions = expected.getDistributions();
            expectedDistributions.forEach(expectedDistribution -> {
                Distribution resultedDistribution = this.getDistribution(expectedDistribution.getId(),
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
            List<User> expectedUsers = expected.getUsers();
            expectedUsers.forEach(expectedUser -> {
                User resultedUser = this.getUser(expectedUser.getId(), result)
                        .orElseThrow(IllegalStateException::new);
                assumeTrue("Files " + outputFileName + " and " + expectedFileName
                        + " must have same users", expectedUser.equals(resultedUser));
            });
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    private Optional<Distribution> getDistribution(Integer id, Endowments endowments) {
        List<Distribution> distributions = endowments.getDistributions();
        return distributions.stream().filter(company -> company.getId().equals(id))
                .findFirst();
    }

    private Optional<Company> getCompany(Integer id, Endowments endowments) {
        List<Company> companies = endowments.getCompanies();
        return companies.stream().filter(company -> company.getId().equals(id)).findFirst();
    }

    private Optional<User> getUser(Integer id, Endowments endowments) {
        List<User> users = endowments.getUsers();
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }
}
