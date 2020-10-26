package org.diehl.wedoogift.domain.service;

import org.diehl.wedoogift.domain.model.Endowments;

import java.io.IOException;
import java.text.ParseException;

public interface EndowmentsService {

    void giveEndowment(Integer amount, String startDate, Integer companyId, Integer userId) throws InsufficientFundsException, IOException, ParseException;

    Endowments searchLastUpdate() throws IOException;

    Integer calculateUserBalance(Integer userId) throws IOException;
}
