//package org.yearup.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//import org.yearup.data.ProductDao;
//import org.yearup.models.Product;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@RestController
//@RequestMapping("/products")
//@CrossOrigin
//public class ProductsController
//{
//    private final ProductDao productDao;
//
//    // Inject ProductDao
//    @Autowired
//    public ProductsController(ProductDao productDao)
//    {
//        this.productDao = productDao;
//    }
//
//    // GET /products
//    // Search products by optional filters:
//    // cat, minPrice, maxPrice, subCategory
//    @GetMapping
//    @PreAuthorize("permitAll()")
//    public List<Product> search(
//            @RequestParam(name = "cat", required = false) Integer categoryId,
//            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
//            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
//            @RequestParam(name = "subCategory", required = false) String subCategory
//    )
//    {
//        try
//        {
//            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
//        }
//        catch (Exception ex)
//        {
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Oops... our bad."
//            );
//        }
//    }
//
//    // GET /products/{id}
//    // Get a single product by id
//    @GetMapping("/{id}")
//    @PreAuthorize("permitAll()")
//    public Product getById(@PathVariable int id)
//    {
//        try
//        {
//            Product product = productDao.getById(id);
//
//            // If product does not exist, return 404
//            if (product == null)
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//            return product;
//        }
//        catch (ResponseStatusException ex)
//        {
//            throw ex;
//        }
//        catch (Exception ex)
//        {
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Oops... our bad."
//            );
//        }
//    }
//
//    // POST /products
//    // Admin only: add a new product
//    @PostMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @ResponseStatus(HttpStatus.CREATED)
//    public Product addProduct(@RequestBody Product product)
//    {
//        try
//        {
//            return productDao.create(product);
//        }
//        catch (Exception ex)
//        {
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Oops... our bad."
//            );
//        }
//    }
//
//    // PUT /products/{id}
//    // Admin only: update an existing product
//    // IMPORTANT: this must UPDATE, not INSERT (fixes duplicate bug)
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void updateProduct(@PathVariable int id, @RequestBody Product product)
//    {
//        try
//        {
//            // Check if product exists first
//            Product existing = productDao.getById(id);
//
//            if (existing == null)
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//            // Perform UPDATE (not create)
//            productDao.update(id, product);
//        }
//        catch (ResponseStatusException ex)
//        {
//            throw ex;
//        }
//        catch (Exception ex)
//        {
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Oops... our bad."
//            );
//        }
//    }
//
//    // DELETE /products/{id}
//    // Admin only: delete a product
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteProduct(@PathVariable int id)
//    {
//        try
//        {
//            // Check if product exists first
//            Product existing = productDao.getById(id);
//
//            if (existing == null)
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//
//            productDao.delete(id);
//        }
//        catch (ResponseStatusException ex)
//        {
//            throw ex;
//        }
//        catch (Exception ex)
//        {
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Oops... our bad."
//            );
//        }
//    }
//}


package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    private final ProductDao productDao;

    // Inject ProductDao
    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    // GET /products
    // Search products by optional filters:
    // cat, minPrice, maxPrice, subCategory
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name = "cat", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "subCategory", required = false) String subCategory
    )
    {
        try
        {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // GET /products/{id}
    // Get a single product by id
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        try
        {
            Product product = productDao.getById(id);

            // If product does not exist, return 404
            if (product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            return product;
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

    // POST /products
    // Admin only: add a new product
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@RequestBody Product product)
    {
        try
        {
            return productDao.create(product);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }

    // PUT /products/{id}
    // Admin only: update an existing product
    // IMPORTANT: this must UPDATE, not INSERT (fixes duplicate bug)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            // Check if product exists first
            Product existing = productDao.getById(id);

            if (existing == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // IMPORTANT: ensure the object uses the id from the URL
            product.setProductId(id);

            // Perform UPDATE (not create)
            productDao.update(id, product);
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

    // DELETE /products/{id}
    // Admin only: delete a product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable int id)
    {
        try
        {
            // Check if product exists first
            Product existing = productDao.getById(id);

            if (existing == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            productDao.delete(id);
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
