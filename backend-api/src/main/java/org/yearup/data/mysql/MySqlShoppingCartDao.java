
package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private final ProductDao productDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();
        Map<Integer, ShoppingCartItem> items = new HashMap<>();

        String sql = """
                SELECT sc.product_id, sc.quantity
                FROM shopping_cart sc
                WHERE sc.user_id = ?
                ORDER BY sc.product_id
                """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int productId = rs.getInt("product_id");
                    int qty = rs.getInt("quantity");

                    Product product = productDao.getById(productId);
                    if (product == null) continue; // skip bad rows if any

                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(qty);

                    items.put(productId, item);
                }
            }

            cart.setItems(items);
            return cart;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving cart for userId=" + userId, e);
        }
    }

    @Override
    public void addProductToCart(int userId, int productId)
    {
        // if row exists -> quantity + 1, else insert with 1
        String sql = """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE quantity = quantity + 1
                """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error adding product to cart.", e);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity)
    {
        String updateSql = """
                UPDATE shopping_cart
                SET quantity = ?
                WHERE user_id = ?
                AND product_id = ?""";

        String deleteSql = """
                DELETE FROM shopping_cart
                WHERE user_id = ?
                AND product_id = ?""";

        try (Connection connection = getConnection())
        {
            if (quantity <= 0)
            {
                try (PreparedStatement ps = connection.prepareStatement(deleteSql))
                {
                    ps.setInt(1, userId);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }
            }
            else
            {
                try (PreparedStatement ps = connection.prepareStatement(updateSql))
                {
                    ps.setInt(1, quantity);
                    ps.setInt(2, userId);
                    ps.setInt(3, productId);
                    ps.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating product quantity.", e);
        }
    }

    @Override
    public ShoppingCartItem getItemByUserAndProduct(int userId, int productId)
    {
        String sql = """
                SELECT quantity
                FROM shopping_cart
                WHERE user_id = ?
                AND product_id = ?""";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setQuantity(rs.getInt("quantity"));
                    return item;
                }
            }
            return null;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving cart item.", e);
        }
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error clearing cart.", e);
        }
    }
}
