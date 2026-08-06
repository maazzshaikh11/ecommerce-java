package com.ecommerce.repository;

import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.ProductVariant;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CartItemRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CartItem> getCartItemsByCustomer(Integer customerId) {
        String sql = """
            SELECT ci.Id AS id, ci.SessionId AS session_id, ci.ProductId AS product_id,
                   ci.VariantId AS variant_id, ci.CustomerId AS customer_id,
                   ci.Quantity AS quantity, ci.UnitPrice AS unit_price, ci.CreatedDate AS created_date,
                   p.Name AS product_name, p.ThumbnailUrl AS thumbnail_url, p.ImageUrl AS image_url,
                   pv.VariantName AS variant_name, pv.VariantValue AS variant_value
            FROM CartItem ci
            LEFT JOIN Product p ON ci.ProductId = p.Id
            LEFT JOIN ProductVariant pv ON ci.VariantId = pv.Id
            WHERE ci.CustomerId = :customerId
            """;

        return jdbcTemplate.query(sql, Map.of("customerId", customerId), new CartItemRowMapper());
    }

    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            CartItem item = new CartItem();
            item.setId(rs.getInt("id"));
            item.setSessionId(rs.getString("session_id"));
            item.setProductId(rs.getInt("product_id"));
            item.setVariantId(rs.getObject("variant_id", Integer.class));
            item.setCustomerId(rs.getObject("customer_id", Integer.class));
            item.setQuantity(rs.getInt("quantity"));
            item.setUnitPrice(rs.getBigDecimal("unit_price"));
            item.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());

            Product product = new Product();
            product.setId(item.getProductId());
            product.setName(rs.getString("product_name"));
            product.setThumbnailUrl(rs.getString("thumbnail_url"));
            product.setImageUrl(rs.getString("image_url"));
            item.setProduct(product);

            if (item.getVariantId() != null && rs.getString("variant_name") != null) {
                ProductVariant variant = new ProductVariant();
                variant.setId(item.getVariantId());
                variant.setVariantName(rs.getString("variant_name"));
                variant.setVariantValue(rs.getString("variant_value"));
                item.setVariant(variant);
            }

            return item;
        }
    }
}
