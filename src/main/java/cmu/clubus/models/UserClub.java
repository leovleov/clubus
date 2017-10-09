package cmu.clubus.models;

/**
 * This class hold information about how many clubs  a user joins in.
 */
public class UserClub {
    String userClubId, userId, clubId;

    /**
     * The constructor for UsesClub
     * @param userId The user's ID.
     * @param clubId A club that the user joins in.
     */
    public UserClub(String userId, String clubId){
        this.userId = userId;
        this.clubId = clubId;
    }

    /**
     * Set id in UserClub class.
     * @param userClubId The id of Event class.
     */
    public void setId(String userClubId)
    {
        this.userClubId = userClubId;
    }
}
