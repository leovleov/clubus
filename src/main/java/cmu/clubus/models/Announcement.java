package cmu.clubus.models;

import java.sql.Timestamp;

/**
 * The accouncement of a club.
 */
public class Announcement {
    String announcementId, clubId, announcementName, announcementInfo, picture;
    Timestamp announcementDateTime;

    /**
     *
     * @param clubId
     * @param announcementName
     * @param announcementInfo
     * @param announcementDateTime
     * @param picture
     */
    public Announcement(String clubId, String announcementName, String announcementInfo, Timestamp announcementDateTime, String picture){
        this.clubId = clubId;
        this.announcementName = announcementName;
        this.announcementInfo = announcementInfo;
        this.announcementDateTime = announcementDateTime;
        this.picture = picture;
    }

    /**
     * Set id in Announcement class.
     * @param announcementId The id of Announcement class.
     */
    public void setId(String announcementId)
    {
        this.announcementId = announcementId;
    }
}
