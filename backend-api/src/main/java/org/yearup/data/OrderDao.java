package org.yearup.data;

import org.yearup.models.Order;

public interface OrderDao
{
    // Creates an order for this user based on their current cart + profile
    Order createOrder(int userId);
}
