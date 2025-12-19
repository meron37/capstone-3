package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;

import javax.sql.DataSource;
import java.sql.*;

/*
 This DAO is responsible for reading and writing profile data
 to the `profiles` table in MySQL.
*/
@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    /*
     Constructor receives the DataSource and passes it to the base DAO
     so we can open database connections.
    */
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /*
     Retrieve a user's profile using their userId.
     This is used when the user views their profile.
    */
    @Override
    public Profile getByUserId(int userId)
    {
        String sql =
                "SELECT user_id, first_name, last_name, phone, email, address, city, state, zip " +
                        "FROM profiles " +
                        "WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            // Bind the userId to the SQL query
            ps.setInt(1, userId);

            try (ResultSet row = ps.executeQuery())
            {
                // If a row exists, convert it to a Profile object
                if (row.next())
                {
                    return mapRow(row);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving profile.", e);
        }

        // No profile found for this user
        return null;
    }

    /*
     Create a blank profile row when a user registers.
     This ensures every user has a profile record.
    */
    @Override
    public Profile create(Profile profile)
    {
        String sql =
                "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            // Insert profile values into the database
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();
            return profile;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating profile.", e);
        }
    }

    /*
     Update an existing profile for the current user.
     This is used when the user edits their profile.
    */
    @Override
    public void update(Profile profile)
    {
        String sql =
                "UPDATE profiles " +
                        "SET first_name = ?, " +
                        "    last_name = ?, " +
                        "    phone = ?, " +
                        "    email = ?, " +
                        "    address = ?, " +
                        "    city = ?, " +
                        "    state = ?, " +
                        "    zip = ? " +
                        "WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            // Update profile fields
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getAddress());
            ps.setString(6, profile.getCity());
            ps.setString(7, profile.getState());
            ps.setString(8, profile.getZip());
            ps.setInt(9, profile.getUserId());

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating profile.", e);
        }
    }

    /*
     Convert a database row into a Profile object.
     This keeps database logic separate from business logic.
    */
    private Profile mapRow(ResultSet row) throws SQLException
    {
        Profile profile = new Profile();
        profile.setUserId(row.getInt("user_id"));
        profile.setFirstName(row.getString("first_name"));
        profile.setLastName(row.getString("last_name"));
        profile.setPhone(row.getString("phone"));
        profile.setEmail(row.getString("email"));
        profile.setAddress(row.getString("address"));
        profile.setCity(row.getString("city"));
        profile.setState(row.getString("state"));
        profile.setZip(row.getString("zip"));
        return profile;
    }
}
