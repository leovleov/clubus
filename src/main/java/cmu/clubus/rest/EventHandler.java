package cmu.clubus.rest;

import cmu.clubus.exceptions.APPBadRequestException;
import cmu.clubus.exceptions.APPInternalServerException;
import cmu.clubus.exceptions.APPNotFoundException;
import cmu.clubus.helpers.DbConnection;
import cmu.clubus.models.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("events")
public class EventHandler {
    DbConnection database;
    Connection connection;
    private ObjectWriter ow;

    public EventHandler() throws Exception {
        database= new DbConnection();

        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public ArrayList<Event> getAllClubs(){
        ArrayList<Event> eventList = new ArrayList<>();
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.events");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getString("clubId"), rs.getString("eventName"), rs.getString("eventInfo"),
                        rs.getTimestamp("eventDateTime"), rs.getString("picture"));
                event.setId(rs.getString("eventId"));
                eventList.add(event);
            }
            connection.close();
            return eventList;
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get all events.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public Event getOne(@PathParam("id") String id){

        try{
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.events WHERE eventId = '"+id+"'");
            ResultSet rs = ps.executeQuery();
            Event event = null;
            if(rs.next()) {
                event = new Event(rs.getString("clubId"), rs.getString("eventName"), rs.getString("eventInfo"),
                        rs.getTimestamp("eventDateTime"), rs.getString("picture"));
                event.setId(rs.getString("eventId"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find an event.");
            }

            connection.close();
            return event;
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get an event.");
        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(404,"Failed to find an event.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }
}
