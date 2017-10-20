package cmu.clubus.models;

public class ClubUsers {
    String  userIds, clubName, clubId;

    public ClubUsers(String clubId, String clubName, String userIds){
        this.clubId = clubId;
        this.userIds = userIds;
        this.clubName = clubName;
    }
}
