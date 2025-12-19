
package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);

    ShoppingCartItem getItemByUserAndProduct(int userId, int productId);

    void addProductToCart(int userId, int productId);

    void updateProductQuantity(int userId, int productId, int quantity);

    void clearCart(int userId);
}


