package cmu.clubus.rest;

import cmu.clubus.exceptions.APPBadRequestException;
import cmu.clubus.exceptions.APPInternalServerException;
import cmu.clubus.helpers.APPResponse;
import cmu.clubus.helpers.DbConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.annotations.Api;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Path("clubuser")
@Api
public class UserClubInterface {
    DbConnection database;
    Connection connection;
    private ObjectWriter ow;

    public UserClubInterface() throws Exception {
        database= new DbConnection();

        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
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
            throw new APPBadRequestException(55,"missing club");
        if (!json.has("userId"))
            throw new APPBadRequestException(55,"missing user");
        try {
            String sql = "INSERT INTO clubus.userclubs(clubId, userId) VALUES(?,?)";
            connection = database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, json.getString("clubId"));
            ps.setString(2, json.getString("userId"));
            ps.executeUpdate();
            connection.close();
            return new APPResponse(request);
        } catch (APPBadRequestException e) {
            throw e;
        }catch (SQLException e) {
            throw new APPBadRequestException(33,"Failed to add a clubuser.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }
}
