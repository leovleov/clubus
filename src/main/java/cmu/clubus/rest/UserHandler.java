package cmu.clubus.rest;

import cmu.clubus.helpers.*;
import cmu.clubus.models.Event;
import cmu.clubus.models.UserClubs;
import cmu.clubus.models.User;
import cmu.clubus.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.annotations.Api;
import org.json.JSONObject;
import org.json.JSONException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("users")
@Api
public class UserHandler {

    DbConnection database;
    Connection connection;
    private ObjectWriter ow;

    public UserHandler() throws Exception {
        database= new DbConnection();

        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getAllUsers(){
        ArrayList<User> userList = new ArrayList<User>();
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.users");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(rs.getString("userName"), rs.getString("picture"), rs.getString("emailFromFB"),
                        rs.getString("andrewEmail"), rs.getString("emailSubscribed"), rs.getString("phoneNumber"),
                        rs.getBoolean("isSubscribed"), rs.getString("facebookId"));
                user.setId(rs.getString("userId"));
                userList.add(user);
            }
            connection.close();
            return new APPResponse(userList);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get all users.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("fb/{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getOneFb(@PathParam("id") String id){

        try{
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.users WHERE facebookId = '"+id+"'");
            ResultSet rs = ps.executeQuery();
            User user = null;
            if(rs.next()) {
                user = new User(rs.getString("userName"), rs.getString("picture"), rs.getString("emailFromFB"),
                        rs.getString("andrewEmail"), rs.getString("emailSubscribed"), rs.getString("phoneNumber"),
                        rs.getBoolean("isSubscribed"), rs.getString("facebookId"));
                user.setId(rs.getString("userId"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find an user.");
            }
            connection.close();
            //if(user == null)
            //    return new APPResponse(user,false);
            //else
                return new APPResponse(user);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get an user.");
        }  catch(APPNotFoundException e) {
            throw new APPNotFoundException(404,"Failed to find an user.");
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
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.users WHERE userId = '"+id+"'");
            ResultSet rs = ps.executeQuery();
            User user = null;
            if(rs.next()) {
                user = new User(rs.getString("userName"), rs.getString("picture"), rs.getString("emailFromFB"),
                        rs.getString("andrewEmail"), rs.getString("emailSubscribed"), rs.getString("phoneNumber"),
                        rs.getBoolean("isSubscribed"), rs.getString("facebookId"));
                user.setId(rs.getString("userId"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find an user.");
            }

            connection.close();
            return new APPResponse(user);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get an user.");
        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(404,"Failed to find an user.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}/clubs")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getUserClubs(@PathParam("id") String id){

        try{
            connection = database.getConnection();
            /*PreparedStatement ps = connection.prepareStatement("select u.userId, u.userName, group_concat(DISTINCT c.clubId SEPARATOR ', ') as clubIds " +
                    "from clubus.users u " +
                    "inner join clubus.userclubs uc on u.userId = uc.userId " +
                    "inner join clubus.clubs c on uc.clubId = c.clubId " +
                    "where u.userId = '" + id + "' " +
                    "group by u.userId, u.userName");*/
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubsinusers WHERE userId = '" + id + "'");
            ResultSet rs = ps.executeQuery();
            UserClubs userClubs = null;
            if(rs.next()) {
                userClubs = new UserClubs(rs.getString("userId"), rs.getString("userName"), rs.getString("clubIds"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find the clubs.");
            }

            connection.close();
            return new APPResponse(userClubs);
        } catch(SQLException e) {
            throw new APPBadRequestException(404,"Failed to get the clubs.");
        } catch(APPNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}/events")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getUserEvents(@PathParam("id") String id){
        try{
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubsinusers WHERE userId = '" + id + "'");
            ResultSet rs = ps.executeQuery();
            ArrayList<Event> eventList = new ArrayList<>();
            String clublist = null;
            if(rs.next()) {
                clublist = rs.getString("clubIds");
            }
            else{
                throw new APPNotFoundException(404,"Failed to find the clubs.");
            }
            ps = connection.prepareStatement("SELECT * FROM clubus.events where clubId in ("+ clublist + ")");
            rs = ps.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getString("clubId"), rs.getString("eventName"), rs.getString("eventInfo"),
                        rs.getTimestamp("eventDateTime"), rs.getString("picture"));
                event.setId(rs.getString("eventId"));
                eventList.add(event);
            }
            connection.close();
            return new APPResponse(eventList);
        } catch(SQLException e) {
            throw new APPBadRequestException(404,"Failed to get the clubs.");
        } catch(APPNotFoundException e) {
            throw e;
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
        if (!json.has("emailFromFB"))
            throw new APPBadRequestException(55,"missing Facebook email");
        if (!json.has("userName"))
            throw new APPBadRequestException(55,"missing name");
        if (!json.has("andrewEmail"))
            throw new APPBadRequestException(55,"missing Andrew email");
        if (!json.has("facebookId"))
            throw new APPBadRequestException(55,"missing Facebook Id");
        try {
            String sql = "INSERT INTO clubus.users(userName, emailFromFB, andrewEmail, facebookId, picture, emailSubscribed, phoneNumber, isSubscribed) VALUES(?,?,?,?,?,?,?,?)";
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, json.getString("userName"));
            ps.setString(2, json.getString("emailFromFB"));
            ps.setString(3, json.getString("andrewEmail"));
            ps.setString(4, json.getString("facebookId"));
            if (json.has("picture"))
                ps.setString(5, json.getString("picture"));
            else
                ps.setString(5, null);
            if (json.has("emailSubscribed"))
                ps.setString(6, json.getString("emailSubscribed"));
            else
                ps.setString(6, null);
            if (json.has("phoneNumber"))
                ps.setString(7, json.getString("phoneNumber"));
            else
                ps.setString(7, null);
            if (json.has("isSubscribed"))
                ps.setBoolean(8, json.getBoolean("isSubscribed"));
            else
                ps.setInt(8, 1);
            ps.executeUpdate();
            connection.close();

            return new APPResponse(request);
        } catch (APPBadRequestException e) {
            throw new APPBadRequestException(55,"missing key parameters.");
        }catch (SQLException e) {
            throw new APPBadRequestException(33,"Failed to add an user.");
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
            if (json.has("emailFromFB") || json.has("andrewEmail") || json.has("facebookId"))
                throw new APPBadRequestException(33, "Some parameters can't be updated.");
            String sql = "UPDATE clubus.users SET ";

            connection = database.getConnection();

            if(json.has("userName"))
                sql = sql + "userName = '" +  json.getString("userName") + "', ";
                //ps.setString(1, json.getString("userName"));
            if(json.has("picture"))
                sql = sql + "picture = '" +  json.getString("picture") + "', ";
                //ps.setString(2, json.getString("picture"));
            if(json.has("emailSubscribed"))
                sql = sql + "emailSubscribed = '" +  json.getString("emailSubscribed") + "', ";
                //ps.setString(3, json.getString("emailSubscribed"));
            if(json.has("phoneNumber"))
                sql = sql + "phoneNumber = '" +  json.getString("phoneNumber") + "', ";
                //ps.setString(4, json.getString("phoneNumber"));
            if(json.has("isSubscribed"))
                sql = sql + "isSubscribed = " +  json.getBoolean("isSubscribed") + ", ";
                //ps.setString(5, json.getString("isSubscribed"));

            sql = sql.substring(0,sql.length()-2) + " WHERE userId = '" + id + "'";
            //ps.setString(6, json.getString("userId"));

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();

        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to update an user due to SQL.");
        } catch(APPBadRequestException e){
            throw new APPBadRequestException(33, "Some parameters can't be updated.");
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to update an user due to JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new APPResponse(request);
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@PathParam("id") String id) {
        String sql = "DELETE FROM clubus.users WHERE userId = ?";
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ps.execute();
            connection.close();

            return new APPResponse(new JSONObject());
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to delete an user");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }

    }
}
