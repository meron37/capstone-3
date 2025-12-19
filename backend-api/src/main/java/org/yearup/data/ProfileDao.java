
package org.yearup.data;

import org.yearup.models.Profile;

public interface ProfileDao
{
    Profile getByUserId(int userId);

    // create blank profile at registration time
    Profile create(Profile profile);

    // update profile for the current user
    void update(Profile profile);
}
