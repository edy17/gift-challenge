package org.diehl.wedoogift.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Company {

    private Integer id;
    private String name;
    private Integer balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company)) return false;
        Company company = (Company) o;
        return getId().equals(company.getId()) &&
                getName().equals(company.getName()) &&
                getBalance().equals(company.getBalance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getBalance());
    }
}
