package de.hhu.cs.dbs.propra.presentation.rest;

import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Path("/reisebueros")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class ReisebueroController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public Response list_reisebuero(@QueryParam("username") String username,@QueryParam("email") String email) throws SQLException {
        Connection connection = dataSource.getConnection();
        String stringStatement = "SELECT rowid,* From Reisebuero";
        if (username != null || email != null) stringStatement = stringStatement + " WHERE 1=1 ";
        if (username != null) { stringStatement = stringStatement + " AND Username LIKE \"%" + username + "%\"";}
        if (email != null) { stringStatement = stringStatement + " AND E_Mail LIKE \"%" + email + "%\"";}
        System.out.println(stringStatement + ";");
        PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("reisebueroid", resultSet.getObject(1));
            entity.put("username", resultSet.getObject(2));
            entity.put("email", resultSet.getObject(3));
            entity.put("passwort", resultSet.getObject(4));
            entity.put("adresseid", resultSet.getObject(5));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return Response.status(Response.Status.OK).entity(entities).build();
    }

    //curl -X POST "http://localhost:8080/reisebueros" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "username=pers" -F "email=em@go.ml" -F "passwort=asdfs23131" -F "adresseid=2"
    @POST
    public Response insert_reisebuero(@FormDataParam("username") String username, @FormDataParam("email")  String email , @FormDataParam("passwort")  String password, @FormDataParam("adresseid") String addresseid) throws SQLException {
        System.out.println("Start!");
        try{
            Connection connection = dataSource.getConnection();
            if (username == null || email == null || password == null || addresseid == null){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String stringStatement = "INSERT INTO Reisebuero(Username, E_Mail, Passwort, Adresse_ID) values(\""+username+"\",\""+email+"\",\""+password+"\","+addresseid+")";
            System.out.println(stringStatement +";");
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
            preparedStatement.closeOnCompletion();
            System.out.println(username);
            preparedStatement.setObject(1, username);
            preparedStatement.setObject(2, email);
            preparedStatement.setObject(3, password);
            preparedStatement.setObject(4, addresseid);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("reisebueroid", resultSet.getObject(1));
                entity.put("username", resultSet.getObject(2));
                entity.put("email", resultSet.getObject(3));
                entity.put("passwort", resultSet.getObject(4));
                entity.put("adresseid", resultSet.getObject(5));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Error in SQL").build();
    }
}

