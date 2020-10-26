package org.diehl.wedoogift.domain.service;

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(Integer companyId, Integer amount) {
        super("Insufficient funds of company " + companyId + " for gift of " + amount);
    }
}
