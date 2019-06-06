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

@Path("/unterkuenfte")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class UnterkunftController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public Response list_unterkunft(@QueryParam("bezeichnung") String bezeichnung,@QueryParam("sterne") String sterne, @QueryParam("top") String top) {
        try{
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT * From Unterkunft";
            if(bezeichnung != null || sterne != null || top != null){ stringStatement = stringStatement += " WHERE 1=1"; }
            if (bezeichnung != null) stringStatement = stringStatement + " AND Bezeichnung LIKE \"%"+ bezeichnung +"%\" ";
            if (sterne != null) stringStatement = stringStatement + " AND Hotelsterne="+ sterne;
            if (top != null) stringStatement = stringStatement + " AND ID IN (SELECT Unterkunft_ID FROM Reise_belegt_Unterkunft WHERE Unterkunft_ID IN (SELECT ID FROM ("+stringStatement+")) GROUP BY Unterkunft_ID ORDER BY count(*) DESC LIMIT "+ top + ")";
            System.out.println(stringStatement +";");
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("unterkunftid", resultSet.getObject(1));
                entity.put("bezeichnung", resultSet.getObject(2));
                entity.put("sterne", resultSet.getObject(3));
                entity.put("adresseid", resultSet.getObject(4));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        }  catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorCode()).build();
        }
    }
}

