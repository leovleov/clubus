package cmu.clubus.rest;

import cmu.clubus.exceptions.*;
import cmu.clubus.helpers.DbConnection;
import cmu.clubus.helpers.*;
import cmu.clubus.models.Announcement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.ArrayList;

@Path("announcements")
public class AnnouncementHandler {
    DbConnection database;
    Connection connection;
    private ObjectWriter ow;

    public AnnouncementHandler() throws Exception {
        database= new DbConnection();

        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getAllClubs(){
        ArrayList<Announcement> annList = new ArrayList<>();
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.announcements");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Announcement ann = new Announcement(rs.getString("clubId"), rs.getString("announcementName"),
                        rs.getString("announcementInfo"), rs.getTimestamp("announcementDateTime"), rs.getString("picture"));
                ann.setId(rs.getString("announcementId"));
                annList.add(ann);
            }
            connection.close();
            return new APPResponse(annList);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get all announcements.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getOne(@PathParam("id") String id){

        try{
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.announcements WHERE announcementId = '"+id+"'");
            ResultSet rs = ps.executeQuery();
            Announcement ann = null;
            if(rs.next()) {
                ann = new Announcement(rs.getString("clubId"), rs.getString("announcementName"),
                        rs.getString("announcementInfo"), rs.getTimestamp("announcementDateTime"), rs.getString("picture"));
                ann.setId(rs.getString("announcementId"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find an announcement.");
            }

            connection.close();
            return new APPResponse(ann);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get an announcement.");
        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(404,"Failed to find an announcement.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create(Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        if (!json.has("clubId"))
            throw new APPBadRequestException(55,"Missing club id.");
        if (!json.has("announcementName"))
            throw new APPBadRequestException(55,"Missing name.");
        if (!json.has("announcementInfo"))
            throw new APPBadRequestException(55,"Missing description.");
        if (!json.has("announcementDateTime"))
            throw new APPBadRequestException(55,"Missing event time.");
        try {
            String sql = "INSERT INTO clubus.announcements(clubId, announcementName, announcementInfo, announcementDateTime, picture) VALUES(?,?,?,?,?)";
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, json.getString("clubId"));
            ps.setString(2, json.getString("announcementName"));
            ps.setString(3, json.getString("announcementInfo"));
            ps.setTimestamp(4, Timestamp.valueOf(json.getString("announcementDateTime") )); //new Timestamp(System.currentTimeMillis())
            if (json.has("picture"))
                ps.setString(5, json.getString("picture"));
            else
                ps.setString(5, null);
            ps.executeUpdate();
            connection.close();

            return new APPResponse(request);
        } catch (APPBadRequestException e) {
            throw e;
        }catch (SQLException e) {
            throw new APPBadRequestException(33,"Failed to add an announcement.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse update(@PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }


        try {
            if (json.has("clubId"))
                throw new APPBadRequestException(33, "Club can't be updated.");
            String sql = "UPDATE clubus.announcements SET ";

            connection = database.getConnection();

            if(json.has("announcementInfo"))
                sql = sql + "announcementInfo = '" +  json.getString("announcementInfo") + "', ";
            if(json.has("picture"))
                sql = sql + "picture = '" +  json.getString("picture") + "', ";
            if(json.has("announcementName"))
                sql = sql + "announcementName = '" +  json.getString("announcementName") + "', ";
            if(json.has("announcementDateTime"))
                sql = sql + "announcementDateTime = '" +  json.getString("announcementDateTime") + "', ";

            sql = sql.substring(0,sql.length()-2) + " WHERE announcementId = '" + id + "'";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();

        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to update an announcement due to SQL.");
        } catch(APPBadRequestException e){
            throw e;
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to update an announcement due to JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new APPResponse(request);
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@PathParam("id") String id) {
        String sql = "DELETE FROM clubus.announcements WHERE announcementId = ?";
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ps.execute();
            connection.close();

            return new APPResponse(new JSONObject());
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to delete an announcement");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }

    }
}
