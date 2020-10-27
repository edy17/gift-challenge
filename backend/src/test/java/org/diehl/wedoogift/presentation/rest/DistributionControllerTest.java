package org.diehl.wedoogift.presentation.rest;

import org.diehl.wedoogift.domain.model.Company;
import org.diehl.wedoogift.domain.model.Endowments;
import org.diehl.wedoogift.domain.model.User;
import org.diehl.wedoogift.domain.model.VoucherUser;
import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.model.WalletType;
import org.diehl.wedoogift.domain.service.EndowmentsService;
import org.diehl.wedoogift.domain.service.VouchersService;
import org.diehl.wedoogift.domain.service.utils.JsonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"data.output.file=Level3/endowments.json",
        "data.voucher.output.file=Level3/vouchers.json"})
public class DistributionControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private EndowmentsService endowmentsService;

    @Autowired
    private VouchersService vouchersService;

    @Value("${data.output.file}")
    private String outputFileName;

    @Value("${data.voucher.output.file}")
    private String outputVoucherFileName;

    private Company connectedCompany;
    private User inputUser;
    private VoucherUser inputVoucherUser;
    private Endowments inputEndowments;
    private Vouchers inputVouchers;

    @Before
    public void initStorage() throws IOException {
        this.inputEndowments = Optional.ofNullable(this.inputEndowments).orElse(this.endowmentsService.searchLastUpdate());
        this.connectedCompany = Optional.ofNullable(this.connectedCompany).orElse(this.inputEndowments.getCompanies()
                .stream().findFirst()
                .orElseThrow(IllegalStateException::new));
        this.inputUser = Optional.ofNullable(this.inputUser).orElse(this.inputEndowments.getUsers()
                .stream().findFirst()
                .orElseThrow(IllegalStateException::new));
        this.inputVouchers = Optional.ofNullable(this.inputVouchers).orElse(this.vouchersService.searchLastUpdate());
        this.inputVoucherUser = Optional.ofNullable(this.inputVoucherUser).orElse(this.inputVouchers.getUsers()
                .stream().findFirst()
                .orElseThrow(IllegalStateException::new));
    }

    @Test
    public void itShouldReturnRecentEndowmentsAndHttpStatusOK() {
        assertNotNull("Connected company must exist", connectedCompany);
        ResponseEntity<Endowments> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity("/level1/all", Endowments.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull("Recent endowments must exist", result.getBody());
    }

    @Test
    public void itShouldGiveEndowmentAndReturnUserIdAndHttpStatusOK() {
        assertNotNull("Connected company must exist", this.connectedCompany);
        assertNotNull("Input user must exist", this.inputUser);
        String url = "/level1/give/" +
                this.connectedCompany.getBalance() + "/" +
                LocalDate.now().format(JsonUtil.formatter) + "/" +
                this.connectedCompany.getId() + "/" +
                this.inputUser.getId();
        ResponseEntity<Integer> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity(url, Integer.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User id must be returned", this.inputUser.getId(), result.getBody());
    }

    @Test
    public void itShouldGetUserBalanceAndHttpStatusOK() {
        assertNotNull("Connected company must exist", connectedCompany);
        assertNotNull("Input user must exist", this.inputUser);
        String url = "/level1/balance/" + this.inputUser.getId();
        ResponseEntity<Integer> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity(url, Integer.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull("User balance must exist", result.getBody());
    }

    @Test
    public void itShouldReturnRecentVouchersAndHttpStatusOK() {
        assertNotNull("Connected company must exist", connectedCompany);
        ResponseEntity<Vouchers> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity("/level2/all", Vouchers.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull("Recent vouchers must exist", result.getBody());
    }

    @Test
    public void itShouldGiveVoucherAndReturnUserIdAndHttpStatusOK() {
        assertNotNull("Connected company must exist", this.connectedCompany);
        assertNotNull("Input voucher user must exist", this.inputVoucherUser);
        String walletId = WalletType.FOOD.getId().toString();
        String url = "/level2/give/" +
                walletId + "/" +
                this.connectedCompany.getBalance() + "/" +
                LocalDate.now().format(JsonUtil.formatter) + "/" +
                this.connectedCompany.getId() + "/" +
                this.inputVoucherUser.getId();
        System.out.println(walletId);
        System.out.println(url);
        ResponseEntity<Integer> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity(url, Integer.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User voucher id must be returned", this.inputVoucherUser.getId(), result.getBody());
    }

    @Test
    public void itShouldGetUserVoucherBalanceAndHttpStatusOK() {
        assertNotNull("Connected company must exist", this.connectedCompany);
        assertNotNull("Input voucher user must exist", this.inputVoucherUser);
        String url = "/level2/balance/" + WalletType.GIFT.getId() + "/" + this.inputVoucherUser.getId();
        ResponseEntity<Integer> result = template
                .withBasicAuth(connectedCompany.getName(), connectedCompany.getId().toString())
                .getForEntity(url, Integer.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull("User voucher balance must exist", result.getBody());
    }

    @After
    public void restoreFileSystem() {
        if (Paths.get(outputFileName).toFile().exists())
            assumeTrue("File " + outputFileName + " must not exist",
                    Paths.get(outputFileName).toFile().delete());
        if (Paths.get(outputVoucherFileName).toFile().exists())
            assumeTrue("File " + outputVoucherFileName + " must not exist",
                    Paths.get(outputVoucherFileName).toFile().delete());
    }
}
