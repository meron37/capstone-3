package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.User;

import java.security.Principal;

@CrossOrigin
@RestController
@RequestMapping("/orders")

// Only authenticated (logged-in) users can access /orders
// Spring Security checks the JWT token before allowing access
@PreAuthorize("isAuthenticated()")
public class OrderController
{
    // DAO used to create orders in the database
    private final OrderDao orderDao;

    // DAO used to look up the logged-in user
    private final UserDao userDao;

    // Constructor injection (Spring injects these automatically)
    public OrderController(OrderDao orderDao, UserDao userDao)
    {
        this.orderDao = orderDao;
        this.userDao = userDao;
    }

    // POST /orders
    // This endpoint represents "checkout"
    @PostMapping

    // If successful, return HTTP 201 Created
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(Principal principal)
    {
        try
        {
            // The Principal object comes from the JWT token
            // principal.getName() == username stored in the token
            String username = principal.getName();

            // Look up the user in the database
            User user = userDao.getByUserName(username);

            // If the user does not exist, reject the request
            if (user == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Create the order for this user
            // IMPORTANT:
            // orderDao.createOrder(userId) must:
            //   1. Read cart items
            //   2. Create order + order_items
            //   3. CLEAR the shopping cart
            return orderDao.createOrder(user.getId());
        }
        catch (ResponseStatusException e)
        {
            // Pass through known HTTP errors (401, 404, etc.)
            throw e;
        }
        catch (Exception e)
        {
            // Catch any unexpected errors
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }
}
