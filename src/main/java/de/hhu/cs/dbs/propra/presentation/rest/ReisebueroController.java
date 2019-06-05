package de.hhu.cs.dbs.propra.presentation.rest;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Path("/reisebuero")
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
    public Response list_reisebuero(@QueryParam("username") String username,@QueryParam("email") String email, List<String> names) throws SQLException {
        Connection connection = dataSource.getConnection();
        String stringStatement = "SELECT rowid,* From Reisebuero";
        if (username != null || email != null) stringStatement = stringStatement + " WHERE ";
        if (username != null) { stringStatement = stringStatement + " AND Username=" + username;}
        if (email != null) { stringStatement = stringStatement + " AND E_Mail=" + email;}
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

    @POST
    public Response insert_reisebuero(@QueryParam("username") String username, @QueryParam("email")  String email , @QueryParam("password")  String password, @QueryParam("adresseid") String addresseid, List<String> names) throws SQLException {
        Connection connection = dataSource.getConnection();
        if (username == null || email == null || password == null || addresseid == null){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String stringStatement = "INSERT INTO Reisebuero(Username, E_Mail, Passwort, Adresse_ID) values()";
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

}

