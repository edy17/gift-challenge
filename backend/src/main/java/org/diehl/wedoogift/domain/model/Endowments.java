package org.diehl.wedoogift.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Endowments {

    private List<Company> companies;
    private List<User> users;
    private List<Distribution> distributions;
}
