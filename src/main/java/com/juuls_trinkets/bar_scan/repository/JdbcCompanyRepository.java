package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.model.Company;

@Repository
public class JdbcCompanyRepository implements CompanyRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcCompanyRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<Company> findById(UUID id) {
        List<Company> companies = template.query(
                "select * from companies where id = :id",
                Map.of("id", id.toString()),
                (result, rowNumber) -> new Company(
                        UUID.fromString(result.getString("id")),
                        result.getString("name"),
                        result.getInt("allowed_user_count"),
                        result.getString("location_name")
                )
        );
        return companies.stream().findFirst();
    }
}
