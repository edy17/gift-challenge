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
public class User {

    private Integer id;
    private Integer balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId().equals(user.getId()) &&
                getBalance().equals(user.getBalance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBalance());
    }
}
