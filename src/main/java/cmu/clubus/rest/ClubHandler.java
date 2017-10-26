package cmu.clubus.rest;

import cmu.clubus.exceptions.*;
import cmu.clubus.helpers.DbConnection;
import cmu.clubus.helpers.*;
import cmu.clubus.models.Club;
import cmu.clubus.models.ClubUsers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.annotations.Api;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("clubs")
@Api
public class ClubHandler {
    DbConnection database;
    Connection connection;
    private ObjectWriter ow;

    public ClubHandler() throws Exception {
        database= new DbConnection();

        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getAllClubs(){
        ArrayList<Club> clubList = new ArrayList<>();
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubs");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Club club = new Club(rs.getString("clubName"), rs.getString("clubInfo"), rs.getString("clubLeaders"),
                        rs.getString("picture"));
                club.setId(rs.getString("clubId"));
                clubList.add(club);
            }
            connection.close();
            return new APPResponse(clubList);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get all clubs.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }


    @GET
    @Path("random3")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getRandom3Clubs(){
        ArrayList<Club> clubList = new ArrayList<>();
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubs ORDER BY RAND() LIMIT 3");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Club club = new Club(rs.getString("clubName"), rs.getString("clubInfo"), rs.getString("clubLeaders"),
                        rs.getString("picture"));
                club.setId(rs.getString("clubId"));
                clubList.add(club);
            }
            connection.close();
            return new APPResponse(clubList);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get 3 clubs.");
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
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubs WHERE clubId = '"+id+"'");
            ResultSet rs = ps.executeQuery();
            Club club = null;
            if(rs.next()) {
                club = new Club(rs.getString("clubName"), rs.getString("clubInfo"), rs.getString("clubLeaders"),
                        rs.getString("picture"));
                club.setId(rs.getString("clubId"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find an club.");
            }

            connection.close();
            return new APPResponse(club);
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to get a club.");
        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(404,"Failed to find a club.");
        } catch (Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}/users")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getClubUsers(@PathParam("id") String id){

        try{
            connection = database.getConnection();
            /*PreparedStatement ps = connection.prepareStatement("select c.clubId, c.clubName, group_concat(DISTINCT u.userId SEPARATOR ', ') as userIds " +
                    "from clubus.clubs c " +
                    "inner join clubus.userclubs uc on c.clubId = uc.clubId " +
                    "inner join clubus.users u on uc.userId = u.userId " +
                    "where c.clubId = '" + id + "' " +
                    "group by c.clubId, c.clubName");*/
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.clubsmembers where clubId = '" + id + "'");
            ResultSet rs = ps.executeQuery();
            ClubUsers clubUsers = null;
            if(rs.next()) {
                clubUsers = new ClubUsers(rs.getString("clubId"), rs.getString("clubName"), rs.getString("userIds"));
            }
            else{
                throw new APPNotFoundException(404,"Failed to find the users.");
            }

            connection.close();
            return new APPResponse(clubUsers);
        } catch(SQLException e) {
            throw new APPBadRequestException(404,"Failed to get the users.");
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
        if (!json.has("clubName"))
            throw new APPBadRequestException(55,"missing club name");
        if (!json.has("clubInfo"))
            throw new APPBadRequestException(55,"missing club description");
        if (!json.has("clubLeaders"))
            throw new APPBadRequestException(55,"missing club leader");
        try {
            String sql = "INSERT INTO clubus.clubs(clubName, clubInfo, clubLeaders, picture) VALUES(?,?,?,?)";
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, json.getString("clubName"));
            ps.setString(2, json.getString("clubInfo"));
            ps.setString(3, json.getString("clubLeaders"));
            if (json.has("picture"))
                ps.setString(4, json.getString("picture"));
            else
                ps.setString(4, null);
            ps.executeUpdate();
            connection.close();
            return new APPResponse(request);
        } catch (APPBadRequestException e) {
            throw new APPBadRequestException(55,"missing key parameters.");
        }catch (SQLException e) {
            throw new APPBadRequestException(33,"Failed to add a club.");
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

            String sql = "UPDATE clubus.clubs SET ";

            connection = database.getConnection();

            if(json.has("clubName"))
                sql = sql + "clubName = '" +  json.getString("clubName") + "', ";
            if(json.has("clubInfo"))
                sql = sql + "clubInfo = '" +  json.getString("clubInfo") + "', ";
            if(json.has("clubLeaders"))
                sql = sql + "clubLeaders = '" +  json.getString("clubLeaders") + "', ";
            if(json.has("picture"))
                sql = sql + "picture = '" +  json.getString("picture") + "', ";

            sql = sql.substring(0,sql.length()-2) + " WHERE clubId = '" + id + "'";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();

        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to update an club due to SQL.");
        } catch(APPBadRequestException e){
            throw new APPBadRequestException(33, "Some parameters can't be updated.");
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to update an club due to JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new APPResponse(request);
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@PathParam("id") String id) {
        String sql = "DELETE FROM clubus.clubs WHERE clubId = ?";
        try {
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            ps.execute();

            connection.close();
            return new APPResponse(new JSONObject());
        } catch(SQLException e) {
            throw new APPBadRequestException(33,"Failed to delete an club");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }

    }

}
