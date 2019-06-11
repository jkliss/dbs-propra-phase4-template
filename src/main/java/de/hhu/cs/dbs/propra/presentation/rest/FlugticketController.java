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
public class FlugticketController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets")
    public Response list_own_flight_tickets(@QueryParam("vorname") String vorname, @QueryParam("nachname") String nachname){
        try{
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT ft.* From Buchung b, Flugticket ft WHERE b.Reisebuero_Username = ? AND ft.Buchung_ID = b.ID ";
            if(vorname != null) stringStatement += " AND ft.Vorname LIKE \"%"+ vorname +"%\" ";
            if(nachname != null) stringStatement += " AND ft.Nachname LIKE \"%"+ nachname +"%\" ";
            stringStatement = stringStatement + ";";
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

    @POST
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets")
    public Response insert_flugticket(@FormDataParam("vorname") String vorname, @FormDataParam("nachname")  String nachname , @FormDataParam("geschlecht")  String geschlecht, @FormDataParam("gepaeck") String gepaeck, @FormDataParam("extragepaeck") String extragepaeck, @FormDataParam("flugid") String flugid, @FormDataParam("preis") String preis) {
        try{
            Connection connection = dataSource.getConnection();
            if (vorname == null || nachname == null || preis == null || flugid == null || geschlecht == null || gepaeck == null || extragepaeck == null){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            // Create Buchung
            System.out.println("Buchung");
            String stringStatement = "INSERT INTO Buchung(Preis, Reisebuero_Username) values(?,?);";
            System.out.println(stringStatement);
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, preis);
            preparedStatement.setObject(2, securityContext.getUserPrincipal().getName());
            int exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            // Get Buchung ID
            System.out.println("Buchung ID");
            stringStatement = "SELECT ID FROM Buchung WHERE Preis = ? AND Reisebuero_Username = ? ORDER BY rowid DESC LIMIT 1 ;";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, preis);
            preparedStatement.setObject(2, securityContext.getUserPrincipal().getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            Object buchung_id = resultSet.getObject(1);
            System.out.println(exit_code);
            resultSet.close();
            // Create Flugticket
            System.out.println("Insert Flugticket");
            stringStatement = "INSERT INTO Flugticket(Buchung_ID,Vorname, Nachname, Geschlecht, Gepaeck, Extragepaeck) values(?,?,?,?,?,?);";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, buchung_id);
            preparedStatement.setObject(2, vorname);
            preparedStatement.setObject(3, nachname);
            preparedStatement.setObject(4, geschlecht);
            preparedStatement.setBoolean(5, Boolean.getBoolean(gepaeck));
            preparedStatement.setBoolean(6, Boolean.getBoolean(extragepaeck));
            exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.status(Response.Status.CREATED).entity("CREATED.").build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("BAD REQUEST.").build();
        }
    }

    @Path("/flugtickets/{flugticketid}")
    @RolesAllowed({"OFFICE"})
    @DELETE // GET http://localhost:8080/foo/xyz
    public Response delete_flight_ticket(@PathParam("flugticketid") String flugticketid) {
        try{
            Connection connection = dataSource.getConnection();
            if (flugticketid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Flugticket ID").build();
            }
            // DELETE Flights of Ticket
            String stringStatement = "DELETE FROM Flugticket_beinhaltet_Flug WHERE Flugticket_Buchung_ID = ?";
            System.out.println(stringStatement);
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, flugticketid);
            int exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            // DELETE Ticket
            stringStatement = "DELETE FROM Flugticket WHERE Buchung_ID = ?";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, flugticketid);
            exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.status(Response.Status.OK).entity(exit_code).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NO_CONTENT).entity(ex.getMessage()).build();
        }
    }

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets/{flugticketid}/fluege")
    public Response list_own_flight_on_ticket(@PathParam("flugticketid") String flugticketid){
        try{
            if (flugticketid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Flugticket ID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT fbf.* From Flugticket ft, Flugticket_beinhaltet_Flug fbf WHERE ft.Buchung_ID = fbf.Flugticket_Buchung_ID AND ft.Buchung_ID = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,flugticketid);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("flugticketid", resultSet.getObject(1));
                entity.put("flugid", resultSet.getObject(2));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @POST
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets/{flugticketid}/fluege")
    public Response insert_flugticket(@PathParam("flugticketid") String flugticketid, @FormDataParam("flugid") String flugid) {
        try{
            if (flugticketid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Flugticket ID").build();
            }
            Connection connection = dataSource.getConnection();

            // Create Buchung
            System.out.println("Buchung");
            String stringStatement = "INSERT INTO Flugticket_beinhaltet_Flug(Flugticket_Buchung_ID,Flug_ID) values(?,?);";
            System.out.println(stringStatement);
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, flugticketid);
            preparedStatement.setObject(2,flugid);
            int exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.status(Response.Status.OK).entity(exit_code).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
}

