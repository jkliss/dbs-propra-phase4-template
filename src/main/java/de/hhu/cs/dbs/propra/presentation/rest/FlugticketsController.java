package de.hhu.cs.dbs.propra.presentation.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class FlugticketsController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets")
    public Response list_own_flight_tickets(){
        try{
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT ft.* From Buchung b, Flugticket ft WHERE b.Reisebuero_Username = ? AND ft.Buchung_ID = b.ID;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,securityContext.getUserPrincipal().getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("ticketid", resultSet.getObject(1));
                entity.put("vorname", resultSet.getObject(2));
                entity.put("nachname", resultSet.getObject(3));
                entity.put("geschlecht", resultSet.getObject(4));
                entity.put("gepaeck", resultSet.getObject(5).equals(1));
                entity.put("extragepaeck", resultSet.getObject(6).equals(1));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(ex.getErrorCode()).build();
        }
    }
}

