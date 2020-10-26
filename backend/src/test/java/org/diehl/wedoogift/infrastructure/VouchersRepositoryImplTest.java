package org.diehl.wedoogift.infrastructure;

import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.VoucherUser;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VouchersRepositoryImplTest {

    @Autowired
    private FileRepository<Vouchers> endowmentsRepository;

    @Value("${data.voucher.input.file}")
    private String inputFileName;

    @Test
    public void searchByFilePathTest() throws IOException {
        Path inputPath = Paths.get(inputFileName);
        assertTrue("File " + inputFileName + " must exist", inputPath.toFile().exists());
        Vouchers endowments = this.endowmentsRepository.searchByFilePath(inputFileName);
        assertTrue("Companies must exist", endowments.getCompanies().size() > 0);
        assertTrue("Users must exist", endowments.getUsers().size() > 0);
    }

    @Test
    public void saveToFilePathTest() throws IOException {
        String testFileName = "Level2/data/test.json";
        Path testPath = Paths.get(testFileName);
        Vouchers endowments = new Vouchers(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        VoucherUser expectedUser = new VoucherUser(1, Collections.emptyList());
        endowments.setUsers(Collections.singletonList(expectedUser));
        this.endowmentsRepository.saveToFilePath(endowments, testFileName);
        assertTrue("File " + testFileName + " must be written", testPath.toFile().exists());
        Vouchers storedEndowments = this.endowmentsRepository.searchByFilePath(testFileName);
        assertEquals("Distributions in " + testFileName + " must exist and empty", 0, storedEndowments.getDistributions().size());
        assertEquals("Companies in " + testFileName + " must exist and empty", 0, storedEndowments.getCompanies().size());
        assertEquals("File " + testFileName + " must contain one user", 1, storedEndowments.getUsers().size());
        VoucherUser storedUser = storedEndowments.getUsers().get(0);
        assertEquals("File " + testFileName + " must contain expected user", expectedUser, storedUser);
        assumeTrue("File " + testFileName + " must not exist", testPath.toFile().delete());
    }
}
