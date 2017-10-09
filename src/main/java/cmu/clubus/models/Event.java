package cmu.clubus.models;

import java.util.Date;

/**
 * Basic Event class.
 */
public class Event {
    String eventId, clubId, eventName, eventInfo, picture;
    Date eventDateTime;

    /**
     * The constructor for Event.
     * @param clubId The club holding this event.
     * @param eventName The name of this event.
     * @param eventInfo The description of this event.
     * @param eventDateTime The date that this event holds.
     * @param picture The picture link of this event.
     */
    public Event(String clubId, String eventName, String eventInfo, Date eventDateTime, String picture){
        this.clubId = clubId;
        this.eventName = eventName;
        this.eventInfo = eventInfo;
        this.eventDateTime = eventDateTime;
        this.picture = picture;
    }

    /**
     * Set id in Event class.
     * @param eventId The id of Event class.
     */
    public void setId(String eventId)
    {
        this.eventId = eventId;
    }
}
