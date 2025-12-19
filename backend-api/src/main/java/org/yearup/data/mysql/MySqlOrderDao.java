package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MySqlOrderDao handles CHECKOUT.
 * Checkout means:
 * 1) Read items from the shopping cart
 * 2) Create an order
 * 3) Create order line items (with quantity)
 * 4) Clear the cart
 */
@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao
{
    // Needed to read cart items during checkout
    private final ShoppingCartDao shoppingCartDao;

    // Needed to get shipping address for the order
    private final ProfileDao profileDao;

    /**
     * Constructor
     * DataSource is for DB access
     * shoppingCartDao gives us cart contents
     * profileDao gives us address info
     */
    public MySqlOrderDao(DataSource dataSource,
                         ShoppingCartDao shoppingCartDao,
                         ProfileDao profileDao)
    {
        super(dataSource);
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
    }

    /**
     * Creates an order for the given user.
     * This is called when the user clicks "Checkout".
     */
    @Override
    public Order createOrder(int userId)
    {
        // Get the user's shopping cart
        ShoppingCart cart = shoppingCartDao.getByUserId(userId);

        // Cart must exist and contain items
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty())
        {
            throw new RuntimeException("Cart is empty.");
        }

        // Get user's profile (for shipping address)
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null)
        {
            throw new RuntimeException("Profile not found for user.");
        }

        // SQL to insert order record
        String insertOrderSql =
                """
                INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        // SQL to insert order line items (one row per product)
        String insertLineSql =
                """
                INSERT INTO order_line_items
                (order_id, product_id, sales_price, quantity, discount)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection connection = getConnection())
        {
            // Turn OFF auto-commit so everything succeeds or fails together
            connection.setAutoCommit(false);

            int orderId;
            LocalDate today = LocalDate.now();
            BigDecimal shipping = BigDecimal.ZERO;


            //  CREATE ORDER HEADER

            try (PreparedStatement ps =
                         connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS))
            {
                ps.setInt(1, userId);
                ps.setDate(2, Date.valueOf(today));
                ps.setString(3, profile.getAddress());
                ps.setString(4, profile.getCity());
                ps.setString(5, profile.getState());
                ps.setString(6, profile.getZip());
                ps.setBigDecimal(7, shipping);

                ps.executeUpdate();

                // Get generated order_id
                try (ResultSet keys = ps.getGeneratedKeys())
                {
                    if (!keys.next())
                        throw new SQLException("Failed to create order (no ID returned).");

                    orderId = keys.getInt(1);
                }
            }


            //  CREATE LINE ITEMS

            List<OrderLineItem> lineItems = new ArrayList<>();

            try (PreparedStatement psLine =
                         connection.prepareStatement(insertLineSql, Statement.RETURN_GENERATED_KEYS))
            {
                // Loop through CART ITEMS
                for (ShoppingCartItem cartItem : cart.getItems().values())
                {
                    int productId = cartItem.getProduct().getProductId();
                    BigDecimal price = cartItem.getProduct().getPrice();
                    int qty = cartItem.getQuantity();
                    BigDecimal discount = cartItem.getDiscountPercent(); // usually 0.00

                    // Insert one row per product (quantity matters here!)
                    psLine.setInt(1, orderId);
                    psLine.setInt(2, productId);
                    psLine.setBigDecimal(3, price);
                    psLine.setInt(4, qty);
                    psLine.setBigDecimal(5, discount);

                    psLine.executeUpdate();

                    // Build OrderLineItem object
                    OrderLineItem oli = new OrderLineItem();

                    try (ResultSet keys = psLine.getGeneratedKeys())
                    {
                        if (keys.next())
                            oli.setOrderLineItemId(keys.getInt(1));
                    }

                    oli.setOrderId(orderId);
                    oli.setProductId(productId);
                    oli.setSalesPrice(price);
                    oli.setQuantity(qty);
                    oli.setDiscount(discount);

                    lineItems.add(oli);
                }
            }


            // CLEAR CART
            // VERY IMPORTANT:
            // After checkout, cart must be empty
            shoppingCartDao.clearCart(userId);

            // Commit transaction (order + items + cart clear)
            connection.commit();

            // =========================
            // BUILD RETURN OBJECT
            // =========================
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setDate(today);
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setShippingAmount(shipping);
            order.setItems(lineItems);

            return order;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error creating order.", e);
        }
    }
}
