package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.model.Shop;

@Repository
public class JdbcShopRepository implements ShopRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcShopRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Shop> findAllByCompanyId(UUID companyId) {
        return template.query(
                "select * from shops where company_id = :companyId order by name",
                Map.of("companyId", companyId.toString()),
                (result, rowNumber) -> new Shop(
                        UUID.fromString(result.getString("id")),
                        UUID.fromString(result.getString("company_id")),
                        result.getString("name")
                )
        );
    }

    @Override
    public Optional<Shop> findById(UUID id) {
        List<Shop> shops = template.query(
                "select * from shops where id = :id",
                Map.of("id", id.toString()),
                (result, rowNumber) -> new Shop(
                        UUID.fromString(result.getString("id")),
                        UUID.fromString(result.getString("company_id")),
                        result.getString("name")
                )
        );
        return shops.stream().findFirst();
    }

    @Override
    public Shop create(Shop shop) {
        template.update(
                """
                insert into shops(id, company_id, name)
                values(:id, :companyId, :name)
                """,
                Map.of(
                        "id", shop.getId().toString(),
                        "companyId", shop.getCompanyId().toString(),
                        "name", shop.getName()
                )
        );
        return shop;
    }
}
