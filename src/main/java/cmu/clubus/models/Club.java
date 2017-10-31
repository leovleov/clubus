package cmu.clubus.models;

/**
 *  Basic Club class.
 */
public class Club {
    String clubId, clubName, clubInfo, clubLeaders, picture, pictureFullRes;

   /**
     *
     * @param clubName The Name of the club.
     * @param clubInfo The club's description.
     * @param clubLeaders The club organizers.
     * @param picture The picture link of a club.
     */
    public Club( String clubName, String clubInfo, String clubLeaders, String picture, String pictureFullRes){
        this.clubName = clubName;
        this.clubInfo = clubInfo;
        this.clubLeaders = clubLeaders;
        this.picture = picture;
        this.pictureFullRes = pictureFullRes;
    }

    /**
     * Set id in Club class.
     * @param clubId The id of Club class.
     */
    public void setId(String clubId)
    {
        this.clubId = clubId;
    }
}
