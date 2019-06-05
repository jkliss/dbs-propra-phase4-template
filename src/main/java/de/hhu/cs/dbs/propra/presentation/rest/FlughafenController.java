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

@Path("/flughaefen")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class FlughafenController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public Response list_flughaefen(@QueryParam("bezeichnung") String bezeichnung) {
        try{
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT rowid,* From Flughafen";
            if (bezeichnung != null) stringStatement = stringStatement + " WHERE Bezeichnung LIKE\"%"+ bezeichnung +"%\"";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("flughafenid", resultSet.getObject(1));
                entity.put("bezeichnung", resultSet.getObject(2));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorCode()).build();
        }
    }
}

