package cmu.clubus.rest;

import cmu.clubus.exceptions.APPBadRequestException;
import cmu.clubus.exceptions.APPInternalServerException;
import cmu.clubus.exceptions.APPNotFoundException;
import cmu.clubus.helpers.DbConnection;
import cmu.clubus.helpers.PATCH;
import cmu.clubus.models.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;
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

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public Object create(Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        if (!json.has("clubId"))
            throw new APPBadRequestException(55,"missing club id");
        if (!json.has("eventName"))
            throw new APPBadRequestException(55,"missing name");
        if (!json.has("eventInfo"))
            throw new APPBadRequestException(55,"missing description");
        if (!json.has("eventDateTime"))
            throw new APPBadRequestException(55,"missing event time");
        try {
            String sql = "INSERT INTO clubus.events(clubId, eventName, eventInfo, eventDateTime, picture) VALUES(?,?,?,?,?)";
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, json.getString("clubId"));
            ps.setString(2, json.getString("eventName"));
            ps.setString(3, json.getString("eventInfo"));
            ps.setTimestamp(4, Timestamp.valueOf(json.getString("eventDateTime") )); //new Timestamp(System.currentTimeMillis())
            if (json.has("picture"))
                ps.setString(5, json.getString("picture"));
            else
                ps.setString(5, null);
            ps.executeUpdate();
            connection.close();

            return request;
        } catch (APPBadRequestException e) {
            throw new APPBadRequestException(55,"missing key parameters.");
        }catch (SQLException e) {
            throw new APPBadRequestException(33,"Failed to add an event.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public Object update(@PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }


        try {
            if (json.has("clubId"))
                throw new APPBadRequestException(33, "Some parameters can't be updated.");
            String sql = "UPDATE clubus.events SET ";

            connection = database.getConnection();

            if(json.has("eventInfo"))
                sql = sql + "eventInfo = '" +  json.getString("eventInfo") + "', ";
            if(json.has("picture"))
                sql = sql + "picture = '" +  json.getString("picture") + "', ";
            if(json.has("eventName"))
                sql = sql + "eventName = '" +  json.getString("eventName") + "', ";
            if(json.has("eventDateTime"))
                sql = sql + "eventDateTime = '" +  json.getString("eventDateTime") + "', ";

            sql = sql.substring(0,sql.length()-2) + " WHERE eventId = '" + id + "'";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();

        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to update an event due to SQL.");
        } catch(APPBadRequestException e){
            throw new APPBadRequestException(33, "Some parameters can't be updated.");
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to update an event due to JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public Object delete(@PathParam("id") String id) {
        String sql = "DELETE FROM clubus.events WHERE eventId = ?";
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ps.execute();
            connection.close();

            return new JSONObject();
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to delete an event");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }

    }
}
