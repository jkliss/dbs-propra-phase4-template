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

@Path("/fluege")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class FlugController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;


    // Kapitel 3 7 16 JERSEY!


    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public Response list_fluege(@QueryParam("startzeitpunkt") String startzeitpunkt,@QueryParam("startflughafen") String startflughafen,@QueryParam("zielflughafen") String zielflughafen,@QueryParam("flugzeugid") String flugzeugid, List<String> names) throws SQLException {
        Connection connection = dataSource.getConnection();
        String stringStatement = "SELECT f.*,fdf1.Flughafen_Bezeichnung,fdf2.Flughafen_Bezeichnung FROM Flug f, Flug_durchgefuehrt_Flughafen fdf1, Flug_durchgefuehrt_Flughafen fdf2 WHERE f.ID = fdf1.Flug_ID AND fdf2.Zielflughafen = true AND f.ID = fdf1.Flug_ID AND fdf1.Zielflughafen = false";
        if (startflughafen != null) stringStatement = stringStatement + " AND fdf1.Flughafen_Bezeichnung=\""+ startflughafen +"\" ";
        if (startzeitpunkt != null) {
            String[] splits = startzeitpunkt.split(" ");
            String day_start = " CAST(strftime('%s',\"" + splits[0] + " 00:00:00\") as INTEGER) ";
            String day_end = " CAST(strftime('%s',\"" + splits[0] + " 23:59:59\") as INTEGER) ";
            String start_point = " CAST(strftime('%s',f.Flugstart) as INTEGER) ";
            stringStatement = stringStatement + " AND "+ start_point +">="+ day_start + " AND "+start_point+"<=" + day_end;
        }
        if (zielflughafen != null) stringStatement = stringStatement + " AND fdf2.Flughafen_Bezeichnung=\""+ zielflughafen +"\" ";
        if (flugzeugid != null) stringStatement = stringStatement + " AND Flugzeug_ID="+ flugzeugid;
        System.out.println(stringStatement +";");
        PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("flugid", resultSet.getObject(1));
            entity.put("startzeitpunkt", resultSet.getObject(2));
            entity.put("dauer", resultSet.getObject(3));
            entity.put("flugzeug", resultSet.getObject(4));
            entity.put("fluggesellschaft", resultSet.getObject(5));
            entity.put("startflughafen", resultSet.getObject(6));
            entity.put("zielflughafen", resultSet.getObject(7));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return Response.status(Response.Status.OK).entity(entities).build();
    }

}

