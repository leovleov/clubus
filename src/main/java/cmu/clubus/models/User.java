package cmu.clubus.models;

/**
 *  Basic User class.
 */
public class User {
    String userId, userName, picture, emailFromFB, andrewEmail, emailSubscribed, phoneNumber, facebookId;
    boolean isSubscribed;

    /**
     * The contructor for User
     * @param userName User's name.
     * @param picture User's uploaded picture.
     * @param emailFromFB User's Facebook email.
     * @param andrewEmail User's CMU Andrew email.
     * @param emailSubscribed User's other email for subscription.
     * @param phoneNumber User's phone number.
     * @param isSubscribed Whether User want to subscribe email notificaiton or not.
     * @param facebookId User's Facebook Id.
     */
    public User(String userName, String picture, String emailFromFB, String andrewEmail, String emailSubscribed, String phoneNumber, boolean isSubscribed, String facebookId){
        this.userName = userName;
        this.picture = picture;
        this.emailFromFB = emailFromFB;
        this.andrewEmail = andrewEmail;
        this.emailSubscribed = emailSubscribed;
        this.phoneNumber = phoneNumber;
        this.isSubscribed = isSubscribed;
        this.facebookId = facebookId;
    }
    /**
     * Set id in User class.
     * @param userId The id of User class.
     */
    public void setId(String userId){
        this.userId = userId;
    }
}
