package com.juuls_trinkets.bar_scan.Dao;

import java.util.List;

import com.juuls_trinkets.bar_scan.ProductMapper;
import com.juuls_trinkets.bar_scan.models.Product;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDaoImpl implements ProductDao {

    public ProductDaoImpl(NamedParameterJdbcTemplate template) {  
        this.template = template;  
    }  
    NamedParameterJdbcTemplate template; 

	@Override
	public List<Product> findAll() {
		return template.query("select * from Products", new ProductMapper());
	}

	@Override
	public int insertProduct(Product product) {
		final String sql = "insert into Products(id, barcode, shelfoforigin, amountcounted, counteddate) values(:id,:barcode,:shelfOfOrigin,:amountCounted, :countedDate)";

        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("id", product.Id)
            .addValue("barcode", product.Barcode)
            .addValue("shelfOfOrigin", product.ShelfOfOrigin)
            .addValue("amountCounted", product.AmountCounted)
            .addValue("countedDate", product.CountedDate);

        return template.update(sql,param, holder);
	}

	@Override
	public int updateProduct(Product product) {
		final String sql = "update Products set barcode=:barcode, shelfOfOrigin=:shelfOfOrigin, amountCounted=:amountCounted, countedDate=:countedDate where id=:id";

        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("id", product.Id)
            .addValue("barcode", product.Barcode)
            .addValue("shelfOfOrigin", product.ShelfOfOrigin)
            .addValue("amountCounted", product.AmountCounted)
            .addValue("countedDate", product.CountedDate);

        return template.update(sql,param, holder);
	}

	@Override
	public int executeUpdateProduct(Product product) {
		final String sql = "update Products set barcode=:barcode, shelfOfOrigin=:shelfOfOrigin, amountCounted=:amountCounted, countedDate=:countedDate where id=:id";

        Map<String,Object> map=new HashMap<String,Object>();  
            map.put("id", product.Id);
            map.put("barcode", product.Barcode);
            map.put("shelfOfOrigin", product.ShelfOfOrigin);
            map.put("amountCounted", product.AmountCounted);
            map.put("countedDate", product.CountedDate);

        template.execute(sql,map,new PreparedStatementCallback<Object>() {  
            @Override  
            public Object doInPreparedStatement(PreparedStatement ps)  
                    throws SQLException, DataAccessException {  
                return ps.executeUpdate();  
            }  
        });

        return 1;
	}

	@Override
	public void deleteProduct(Product product) {
		final String sql = "delete from Products where id=:id";


        Map<String,Object> map=new HashMap<String,Object>();  
            map.put("id", product.Id);

        template.execute(sql,map,new PreparedStatementCallback<Object>() {  
            @Override  
            public Object doInPreparedStatement(PreparedStatement ps)  
                    throws SQLException, DataAccessException {  
                return ps.executeUpdate();  
            }  
        });  

	}

}
