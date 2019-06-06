package de.hhu.cs.dbs.propra.presentation.rest;

import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
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
public class BuchungenController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/buchung")
    public Response list_own_bookings(@QueryParam("rowid") String rowid){
        try{
            if(rowid == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No RowID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT * From Buchung WHERE Reisebuero_Username = ? AND rowid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,securityContext.getUserPrincipal().getName());
            preparedStatement.setObject(2,rowid);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("buchungid", resultSet.getObject(1));
                entity.put("preis", resultSet.getObject(3));
                entity.put("datum", resultSet.getObject(2));
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
