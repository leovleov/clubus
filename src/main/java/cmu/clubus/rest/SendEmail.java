package cmu.clubus.rest;

import cmu.clubus.exceptions.APPBadRequestException;
import cmu.clubus.exceptions.APPInternalServerException;
import cmu.clubus.exceptions.APPNotFoundException;
import cmu.clubus.helpers.DbConnection;
import cmu.clubus.models.ClubUsers;
import cmu.clubus.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.*;

//@Path("sendemail")
//@Api
public class SendEmail extends HttpServlet {
    DbConnection database;
    Connection connection;

    public SendEmail() throws Exception {
        database= new DbConnection();

    }


//    @POST
//    @Produces({MediaType.APPLICATION_JSON})
//    public APPResponse doSendEmail() {
    public void init() throws ServletException{

        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {

                try{
                    final String username = "postmaster@mg.clubus.me";
                    final String password = "ac63db35e25b11aa2c5f18055d43a0ef";



                    // Sender's email ID needs to be mentioned
                    String from = "cmu.clubus@gmail.com";

                    // Assuming you are sending email from localhost
                    String host = "smtp.mailgun.org";

                    // Get system properties
                    Properties properties = System.getProperties();

                    // Setup mail server
                    properties.setProperty("mail.smtp.host", host);
                    properties.setProperty("mail.smtp.auth", "true");
                    properties.setProperty("mail.smtp.host", "smtp.mailgun.org");
                    properties.setProperty("mail.transport.protocol", "smtp");
                    properties.setProperty("mail.smtp.port", "2525");

                    // Get the default Session object.
                    //Session session = Session.getDefaultInstance(properties);
                    Session session = Session.getInstance(properties,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, password);
                                }
                            });

                    connection = database.getConnection();
                    PreparedStatement ps = connection.prepareStatement("SELECT * FROM clubus.events WHERE eventDateTime <  NOW() + INTERVAL 1 DAY AND eventDateTime > NOW() AND isNotified = 0");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
//                        Event event = new Event(rs.getString("clubId"), rs.getString("eventName"), rs.getString("eventInfo"),
//                                rs.getTimestamp("eventDateTime"), rs.getString("picture"), rs.getString("eventLocation"));
//                        event.setId(rs.getString("eventId"));

                        PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM clubus.clubsmembers where clubId = '" + rs.getString("clubId") + "'");
                        ResultSet rs2 = ps2.executeQuery();
                        ClubUsers clubUsers = null;
                        if(rs2.next()) {
//                            clubUsers = new ClubUsers(rs.getString("clubId"), rs.getString("clubName"), rs.getString("userIds"));
                            String userIds = rs2.getString("userIds");
                            String[] userIdsArray = userIds.split(", ");
                            for(int i = 0 ; i < userIdsArray.length ; i++){
                                PreparedStatement ps3 = connection.prepareStatement("SELECT * FROM clubus.users WHERE userId = '"+ userIdsArray[i] +"' AND isSubscribed = 1" );
                                ResultSet rs3 = ps3.executeQuery();
                                User user = null;

                                if(rs3.next()) {
//                                    user = new User(rs.getString("userName"), rs.getString("picture"), rs.getString("emailFromFB"),
//                                            rs.getString("andrewEmail"), rs.getString("emailSubscribed"), rs.getString("phoneNumber"),
//                                            rs.getBoolean("isSubscribed"), rs.getString("facebookId"));
//                                    user.setId(rs.getString("userId"));

                                    // Recipient's email ID needs to be mentioned.
                                    String to = rs3.getString("emailFromFB");
                                    try {
                                        // Create a default MimeMessage object.
                                        MimeMessage message = new MimeMessage(session);

                                        // Set From: header field of the header.
                                        message.setFrom(new InternetAddress(from));

                                        // Set To: header field of the header.
                                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

                                        // Set Subject: header field
                                        message.setSubject("Notification for an upcoming event from your club " + rs2.getString("clubName") + "!!");

                                        // Now set the actual message
                                        message.setText("Hi " + rs3.getString("userName") + ","
                                                + "\nThis is a notification for the event \"" + rs.getString("eventName") + "\"."
                                                + "\nEvent Info : " + rs.getString("eventInfo")
                                                + "\nEvent Time : " + rs.getTimestamp("eventDateTime").toString()
                                                + "\nEvent Location : " + rs.getString("eventLocation")
                                                + "\n\nSent by ClubUs");

                                        // Send message
                                        Transport.send(message);

                                        //System.out.println("Sent message successfully....");
                                    } catch (MessagingException mex) {
                                        mex.printStackTrace();
                                    }
                                }
                            }
                        }
                        String sql = "UPDATE clubus.events SET isNotified = 1 WHERE eventId = '" + rs.getString("eventId") + "'";
                        PreparedStatement ps4 = connection.prepareStatement(sql);
                        ps4.execute();
                    }



                    connection.close();
                } catch(SQLException e) {
                    throw new APPBadRequestException(33,"Failed to get an event.");
                } catch(APPNotFoundException e) {
                    throw new APPNotFoundException(404,"Failed to find an event.");
                } catch (Exception e) {
                    throw new APPInternalServerException(99,"Something happened at server side!");
                }




            }
        };

        timer.schedule (hourlyTask, 0l, 1000*60*60);
        //return new APPResponse("Success!!");




    }

}
