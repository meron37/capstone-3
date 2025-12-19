package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

/**
 * This controller handles ALL shopping cart operations:
 * - Viewing the cart
 * - Adding products to the cart
 * - Updating quantities
 * - Clearing the cart
 *
 * All endpoints require the user to be logged in.
 */
@RestController
@RequestMapping("/cart")        // Base URL: /cart
@CrossOrigin                   // Allows frontend (browser) to call this API
@PreAuthorize("isAuthenticated()") // Every endpoint requires a valid JWT token
public class ShoppingCartController
{
    // DAO for cart database operations
    private final ShoppingCartDao shoppingCartDao;

    // DAO for user lookups
    private final UserDao userDao;

    // DAO for product validation
    private final ProductDao productDao;

    // Constructor injection (Spring provides these automatically)
    public ShoppingCartController(
            ShoppingCartDao shoppingCartDao,
            UserDao userDao,
            ProductDao productDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    /**
     * GET /cart
     * Returns the current logged-in user's shopping cart.
     *
     * The JWT token is read automatically and converted into a Principal.
     */
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // Username comes from the JWT token
            String userName = principal.getName();

            // Look up the user in the database
            User user = userDao.getByUserName(userName);

            // If user doesn't exist, block access
            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Return the user's cart (even if empty)
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            // Any unexpected error → 500
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    /**
     * POST /cart/products/{productId}
     * Adds ONE unit of a product to the cart.
     * If the product already exists in the cart,
     * the DAO will INCREMENT the quantity.
     */
    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addProductToCart(
            @PathVariable int productId,
            Principal principal)
    {
        try
        {
            // Get logged-in user's username from token
            String userName = principal.getName();

            // Look up user
            User user = userDao.getByUserName(userName);
            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Validate product exists before adding
            if (productDao.getById(productId) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Add product to cart (or increase quantity)
            shoppingCartDao.addProductToCart(user.getId(), productId);

            // Return updated cart so UI/tests can see changes immediately
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    /**
     * PUT /cart/products/{productId}
     * Updates the quantity of a specific product in the cart.
     * quantity = 0 → item is removed from cart
     */
    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCartItem(
            @PathVariable int productId,
            @RequestBody ShoppingCartItem item,
            Principal principal)
    {
        try
        {
            // Get logged-in user
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);

            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Validate quantity
            int qty = item.getQuantity();
            if (qty < 0)
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantity must be >= 0"
                );

            // Update quantity or delete item if qty == 0
            shoppingCartDao.updateProductQuantity(
                    user.getId(),
                    productId,
                    qty
            );
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    /**
     * DELETE /cart
     * Removes ALL items from the user's cart.
     * Used during checkout or when user clicks "Clear Cart".
     */
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal)
    {
        try
        {
            // Identify user via JWT
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);

            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Remove all cart rows for this user
            shoppingCartDao.clearCart(user.getId());

            // Return empty cart (important for tests/UI)
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }
}
