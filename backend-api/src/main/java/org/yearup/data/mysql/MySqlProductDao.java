
package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory)
    {
        // We build the SQL dynamically so ONLY the filters the user passes are applied.
        // This avoids bugs where null filters accidentally return wrong results.
        List<Product> products = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // If categoryId is provided: only return products from that category
        if (categoryId != null)
        {
            sql.append(" AND category_id = ? ");
            params.add(categoryId);
        }

        // If minPrice is provided: only return products with price >= minPrice
        if (minPrice != null)
        {
            sql.append(" AND price >= ? ");
            params.add(minPrice);
        }

        // If maxPrice is provided: only return products with price <= maxPrice
        if (maxPrice != null)
        {
            sql.append(" AND price <= ? ");
            params.add(maxPrice);
        }

        // If subCategory is provided: filter by subcategory
        // Using LIKE lets searches like "red" match "red", "dark red", etc.
        if (subCategory != null && !subCategory.isBlank())
        {
            sql.append(" AND subcategory LIKE ? ");
            params.add("%" + subCategory.trim() + "%");
        }

        // Keep results stable/predictable for tests
        sql.append(" ORDER BY product_id;");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString()))
        {
            // Put the params into the prepared statement in the correct order
            for (int i = 0; i < params.size(); i++)
            {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet row = statement.executeQuery())
            {
                while (row.next())
                {
                    products.add(mapRow(row));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error searching products.", e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        // Return all products that match ONE category id
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY product_id;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);

            try (ResultSet row = statement.executeQuery())
            {
                while (row.next())
                {
                    products.add(mapRow(row));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error listing products for categoryId=" + categoryId, e);
        }

        return products;
    }

    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, productId);

            try (ResultSet row = statement.executeQuery())
            {
                if (row.next())
                {
                    return mapRow(row);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving product id=" + productId, e);
        }

        return null;
    }

    @Override
    public Product create(Product product)
    {
        // Insert a new product into the database
        String sql = """
                INSERT INTO products(name, price, category_id, description, subcategory, image_url, stock, featured)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            statement.executeUpdate();

            // Get the newly created product_id
            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (keys.next())
                {
                    int newId = keys.getInt(1);
                    return getById(newId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating product.", e);
        }

        return null;
    }

    @Override
    public void update(int productId, Product product)
    {
        // IMPORTANT: This must UPDATE the existing row.
        // If you accidentally call create() here, you will create duplicates (Bug 2).
        String sql = """
                UPDATE products
                   SET name = ?,
                       price = ?,
                       category_id = ?,
                       description = ?,
                       subcategory = ?,
                       image_url = ?,
                       stock = ?,
                       featured = ?
                 WHERE product_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating product id=" + productId, e);
        }
    }

    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting product id=" + productId, e);
        }
    }

    // Convert a SQL row into a Product object
    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}
