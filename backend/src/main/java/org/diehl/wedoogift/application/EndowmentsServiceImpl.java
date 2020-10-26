package org.diehl.wedoogift.application;

import org.diehl.wedoogift.domain.model.Company;
import org.diehl.wedoogift.domain.model.Distribution;
import org.diehl.wedoogift.domain.model.Endowments;
import org.diehl.wedoogift.domain.model.User;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.diehl.wedoogift.domain.service.EndowmentsService;
import org.diehl.wedoogift.domain.service.InsufficientFundsException;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class EndowmentsServiceImpl implements EndowmentsService {

    private final Logger logger = LoggerFactory.getLogger(EndowmentsServiceImpl.class);

    private FileRepository<Endowments> endowmentsRepository;

    @Value("${data.input.file}")
    private String inputFileName;

    @Value("${data.output.file}")
    private String outputFileName;

    public EndowmentsServiceImpl(FileRepository<Endowments> endowmentsRepository) {
        this.endowmentsRepository = endowmentsRepository;
    }

    @Override
    public void giveEndowment(Integer amount, String startDate, Integer companyId, Integer userId) throws IOException, InsufficientFundsException {
        Endowments endowments = this.searchLastUpdate();
        List<Distribution> distributions = endowments.getDistributions();
        List<Company> companies = endowments.getCompanies();
        List<User> users = endowments.getUsers();
        Company company = companies.stream().filter(c -> c.getId().equals(companyId)).findFirst()
                .orElseThrow(IllegalStateException::new);
        if (amount>company.getBalance())
            throw new InsufficientFundsException(companyId, amount);
        User user = users.stream().filter(u -> u.getId().equals(userId)).findFirst()
                .orElseThrow(IllegalStateException::new);
        LocalDate date = LocalDate.parse(startDate, JsonUtil.formatter);
        LocalDate endDate = date.plusYears(1).minusDays(1);
        Distribution distribution = new Distribution(distributions.size() + 1,
                amount, date, endDate, companyId, userId);
        distributions.add(distribution);
        Company newCompany = new Company(company.getId(), company.getName(),
                company.getBalance() - amount);
        companies.set(companies.indexOf(company), newCompany);
        User updatedUser = new User(user.getId(), user.getBalance());
        updatedUser.setBalance(user.getBalance() + amount);
        users.set(users.indexOf(user), updatedUser);
        Endowments updatedEndowments = new Endowments(companies, users, distributions);
        this.endowmentsRepository.saveToFilePath(updatedEndowments, outputFileName);
        logger.info("The user {} received a gift of {} from the company {} on {}", userId, amount, companyId, startDate);
    }

    @Override
    public Integer calculateUserBalance(Integer userId) throws IOException {
        Endowments endowments = this.searchLastUpdate();
        List<Distribution> distributions = endowments.getDistributions();
        List<User> users = endowments.getUsers();
        User user = users.stream().filter(u -> u.getId().equals(userId)).findFirst()
                .orElseThrow(IllegalStateException::new);
        final Integer[] balance = {user.getBalance()};
        final LocalDate today = LocalDate.now();
        distributions.stream().filter(distribution -> distribution.getUserId().equals(userId))
                .filter(distribution -> distribution.getEndDate().isBefore(today))
                .forEach(distribution -> {
                    balance[0] = balance[0] - distribution.getAmount();
                });
        logger.info("The consumable balance of user {} has been calculated", userId);
        return balance[0];
    }

    @Override
    public Endowments searchLastUpdate() throws IOException {
        Path outputPath = Paths.get(outputFileName);
        Path inputPath = Paths.get(inputFileName);
        if (outputPath.toFile().exists()) {
            return this.endowmentsRepository.searchByFilePath(outputFileName);
        } else if (inputPath.toFile().exists()) {
            return this.endowmentsRepository.searchByFilePath(inputFileName);
        } else {
            throw new IllegalStateException("File " + inputFileName + " or file " + outputFileName + " must exit");
        }
    }
}
