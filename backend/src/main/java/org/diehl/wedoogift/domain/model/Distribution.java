package org.diehl.wedoogift.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Distribution {

    private Integer id;
    private Integer amount;
    @JsonProperty("start_date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDate startDate;
    @JsonProperty("end_date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDate endDate;
    @JsonProperty("company_id")
    private Integer companyId;
    @JsonProperty("user_id")
    private Integer userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distribution)) return false;
        Distribution that = (Distribution) o;
        return getId().equals(that.getId()) &&
                getAmount().equals(that.getAmount()) &&
                getStartDate().equals(that.getStartDate()) &&
                getEndDate().equals(that.getEndDate()) &&
                getCompanyId().equals(that.getCompanyId()) &&
                getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAmount(), getStartDate(), getEndDate(), getCompanyId(), getUserId());
    }
}
