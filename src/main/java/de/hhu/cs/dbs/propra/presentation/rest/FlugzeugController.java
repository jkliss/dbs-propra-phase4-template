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

@Path("/flugzeuge")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class FlugzeugController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET // GET http://localhost:8080
    public Response list_flugzeuge(@QueryParam("flugzeugid") String flugzeugid,@QueryParam("modell") String modell, @QueryParam("baujahr") String baujahr,@QueryParam("passagieranzahl") String passagieranzahl, @QueryParam("crewanzahl") String crewanzahl, List<String> names) throws SQLException {
        Connection connection = dataSource.getConnection();
        String stringStatement = "SELECT * From Flugzeug";
        if(flugzeugid != null || modell != null || baujahr != null || passagieranzahl != null || crewanzahl != null){ stringStatement = stringStatement += " WHERE 1=1"; }
        if (flugzeugid != null) stringStatement = stringStatement + " AND Flugzeug_ID="+ flugzeugid +" ";
        if (modell != null) stringStatement = stringStatement + " AND Modellbezeichnung=\""+ modell +"\" ";
        if (baujahr != null) stringStatement = stringStatement + " AND Baujahr="+ baujahr +" ";
        if (passagieranzahl != null) stringStatement = stringStatement + " AND Passagiere>="+ passagieranzahl +" ";
        if (crewanzahl != null) stringStatement = stringStatement + " AND Crewmitglieder>="+ crewanzahl +" ";
        System.out.println(stringStatement +";");
        PreparedStatement preparedStatement = connection.prepareStatement(stringStatement + ";");
        preparedStatement.closeOnCompletion();
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> entities = new ArrayList<>();
        Map<String, Object> entity;
        while (resultSet.next()) {
            entity = new LinkedHashMap<>();
            entity.put("flugzeugid", resultSet.getObject(1));
            entity.put("modellbezeichnung", resultSet.getObject(2));
            entity.put("baujahr", resultSet.getObject(3));
            entity.put("passagieranzahl", resultSet.getObject(4));
            entity.put("crewanzahl", resultSet.getObject(5));
            entities.add(entity);
        }
        resultSet.close();
        connection.close();
        return Response.status(Response.Status.OK).entity(entities).build();
    }

}

