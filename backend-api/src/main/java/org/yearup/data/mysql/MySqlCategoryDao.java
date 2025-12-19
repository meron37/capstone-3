package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySqlCategoryDao is the MySQL implementation of CategoryDao.
 * This class is responsible ONLY for database access related to categories.
 * Controllers never talk directly to the database — they talk to DAOs.
 */
@Component // Marks this class as a Spring Bean so it can be injected
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    /**
     * Constructor
     * The DataSource is injected by Spring and passed to MySqlDaoBase.
     * MySqlDaoBase provides the getConnection() helper method.
     */
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /**
     * Returns ALL categories in the system.
     * Used when browsing categories on the website.
     */
    @Override
    public List<Category> getAllCategories()
    {
        // SQL query to fetch all categories in a stable order
        String sql = """
                SELECT category_id, name, description
                FROM categories
                ORDER BY category_id;
                """;

        List<Category> categories = new ArrayList<>();

        // Try-with-resources automatically closes connection, statement, and result set
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery())
        {
            // Loop through each database row
            while (row.next())
            {
                // Convert SQL row → Category object
                categories.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            // Wrap SQL exception in runtime exception for controller layer
            throw new RuntimeException("Error retrieving categories.", e);
        }

        return categories;
    }

    /**
     * Returns ONE category by its ID.
     * Used when filtering products by category.
     */
    @Override
    public Category getById(int categoryId)
    {
        String sql = """
                SELECT category_id, name, description
                FROM categories
                WHERE category_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            // Bind categoryId safely (prevents SQL injection)
            statement.setInt(1, categoryId);

            try (ResultSet row = statement.executeQuery())
            {
                // If a category is found, map it
                if (row.next())
                {
                    return mapRow(row);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(
                    "Error retrieving category id=" + categoryId,
                    e
            );
        }

        // If no category exists with that ID
        return null;
    }

    /**
     * Creates a new category.
     * Admin-only operation.
     */
    @Override
    public Category create(Category category)
    {
        String sql = """
                INSERT INTO categories (name, description)
                VALUES (?, ?);
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            // Bind values from Category object
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            // Execute INSERT
            statement.executeUpdate();

            // Retrieve generated category_id
            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (keys.next())
                {
                    category.setCategoryId(keys.getInt(1));
                }
            }

            // Return newly created category with ID populated
            return category;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating category.", e);
        }
    }

    /**
     * Updates an existing category.
     * IMPORTANT: This updates the row — it does NOT create a new one.
     */
    @Override
    public void update(int categoryId, Category category)
    {
        String sql = """
                UPDATE categories
                   SET name = ?,
                       description = ?
                 WHERE category_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            // Bind updated values
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            // Execute UPDATE
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(
                    "Error updating category id=" + categoryId,
                    e
            );
        }
    }

    /**
     * Deletes a category by ID.
     * Admin-only operation.
     */
    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(
                    "Error deleting category id=" + categoryId,
                    e
            );
        }
    }

    /**
     * Helper method:
     * Converts a ResultSet row into a Category object.
     * This keeps mapping logic in ONE place.
     */
    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        // Create and populate Category object
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(description);

        return category;
    }
}
