package de.hhu.cs.dbs.propra.presentation.rest;

import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.annotation.Retention;
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
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorCode()).build();
        }
    }

    @POST
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets")
    public Response insert_flugticket(@FormDataParam("vorname") String vorname, @FormDataParam("nachname")  String nachname , @FormDataParam("geschlecht")  String geschlecht, @FormDataParam("gepaeck") Boolean gepaeck, @FormDataParam("extragepaeck") Boolean extragepaeck, @FormDataParam("flugid") Integer flugid, @FormDataParam("preis") Integer preis) {
        try{
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            if (vorname == null || nachname == null || preis == null || flugid == null || geschlecht == null || gepaeck == null || extragepaeck == null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Parameter/s missing!").build();
            }
            // Create Buchung
            int new_id;
            String stringStatement = null;
            PreparedStatement preparedStatement = null;
            int exit_code = 0;
            try {
                System.out.println("Buchung");
                stringStatement = "INSERT INTO Buchung(Preis, Reisebuero_Username) values(?,?);";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, preis);
                preparedStatement.setObject(2, securityContext.getUserPrincipal().getName());
                exit_code = preparedStatement.executeUpdate();
                new_id = preparedStatement.getGeneratedKeys().getInt(1);
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // Create Flugticket
            try {
                System.out.println("Insert Flugticket");
                stringStatement = "INSERT INTO Flugticket(Buchung_ID,Vorname, Nachname, Geschlecht, Gepaeck, Extragepaeck) values(?,?,?,?,?,?);";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, new_id);
                preparedStatement.setObject(2, vorname);
                preparedStatement.setObject(3, nachname);
                preparedStatement.setObject(4, geschlecht);
                preparedStatement.setBoolean(5, gepaeck);
                preparedStatement.setBoolean(6, extragepaeck);
                exit_code = preparedStatement.executeUpdate();
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            connection.commit();
            connection.close();
            return Response.created(UriBuilder.fromUri("http://localhost:8080/flugtickets/"+new_id).build()).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("BAD REQUEST.").build();
        }
    }

    @Path("/flugtickets/{flugticketid}")
    @RolesAllowed({"OFFICE"})
    @DELETE // GET http://localhost:8080/foo/xyz
    public Response delete_flight_ticket(@PathParam("flugticketid") Integer flugticketid) {
        try{
            String stringStatement = null;
            PreparedStatement preparedStatement = null;
            Connection connection = dataSource.getConnection();
            if (flugticketid == null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Keine Flugticket ID").build();
            }
            // CHECK REISEBUERO AND REISE Vorhanden
            try {
                stringStatement = "SELECT b.ID FROM Buchung b, Flugticket f WHERE b.Reisebuero_Username = ? AND b.ID = ? AND f.Buchung_ID = b.ID;";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, securityContext.getUserPrincipal().getName());
                preparedStatement.setObject(2, flugticketid);
                if(!preparedStatement.executeQuery().next()){
                    return Response.status(Response.Status.FORBIDDEN).entity("Nicht von diesem Reisebuero durchgeführt/Kein Flugticket mit dieser ID angelegt").build();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            connection.setAutoCommit(false);

            // DELETE Unterkunft of Reise
            int exit_code = 0;
            try {
                stringStatement = "DELETE FROM Reise_belegt_Unterkunft WHERE Reise_Buchung_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // DELETE Tags of Reise
            exit_code = 0;
            try {
                stringStatement = "DELETE FROM Reise_hat_Tag WHERE Reise_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // DELETE Unterkunft of Reise
            try {
                stringStatement = "DELETE FROM Reise_belegt_Unterkunft WHERE Reise_Buchung_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // DELETE Reise
            try {
                stringStatement = "DELETE FROM Reise WHERE Buchung_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
                System.out.println(exit_code);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // DELETE Flights of Ticket
            try {
                stringStatement = "DELETE FROM Flugticket_beinhaltet_Flug WHERE Flugticket_Buchung_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // DELETE Ticket
            try {
                stringStatement = "DELETE FROM Flugticket WHERE Buchung_ID = ?";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            try {
                stringStatement = "DELETE FROM Buchung WHERE ID = ?";
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, flugticketid);
                exit_code = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

            }
            connection.commit();
            connection.close();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/flugtickets/{flugticketid}/fluege")
    public Response list_own_flight_on_ticket(@PathParam("flugticketid") Integer flugticketid){
        try{
            if (flugticketid == null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Keine Flugticket ID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement;
            PreparedStatement preparedStatement;
            // CHECK REISEBUERO AND TICKET Vorhanden
            try {
                stringStatement = "SELECT Buchung_ID FROM Flugticket ft, Buchung b WHERE b.Reisebuero_Username = ? AND b.ID = ? AND ft.Buchung_ID = b.ID;";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, securityContext.getUserPrincipal().getName());
                preparedStatement.setObject(2, flugticketid);
                if(!preparedStatement.executeQuery().next()){
                    return Response.status(Response.Status.FORBIDDEN).entity("Nicht von diesem Reisebuero durchgeführt oder TicketNr nicht vorhanden").build();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            stringStatement = "SELECT fbf.* From Flugticket ft, Flugticket_beinhaltet_Flug fbf WHERE ft.Buchung_ID = fbf.Flugticket_Buchung_ID AND ft.Buchung_ID = ?;";
            preparedStatement = connection.prepareStatement(stringStatement);
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
    public Response insert_flugticket(@PathParam("flugticketid") Integer flugticketid, @FormDataParam("flugid") Integer flugid) {
        try{
            if (flugticketid == null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Keine Flugticket ID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement;
            PreparedStatement preparedStatement;
            // CHECK REISEBUERO AND TICKET Vorhanden
            try {
                stringStatement = "SELECT Buchung_ID FROM Flugticket ft, Buchung b WHERE b.Reisebuero_Username = ? AND b.ID = ? AND ft.Buchung_ID = b.ID;";
                System.out.println(stringStatement);
                preparedStatement = connection.prepareStatement(stringStatement);
                preparedStatement.closeOnCompletion();
                preparedStatement.setObject(1, securityContext.getUserPrincipal().getName());
                preparedStatement.setObject(2, flugticketid);
                if(!preparedStatement.executeQuery().next()){
                    return Response.status(Response.Status.FORBIDDEN).entity("Nicht von diesem Reisebuero durchgeführt oder TicketNr nicht vorhanden").build();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
            }
            // Create Buchung
            System.out.println("Buchung");
            stringStatement = "INSERT INTO Flugticket_beinhaltet_Flug(Flugticket_Buchung_ID,Flug_ID) values(?,?);";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, flugticketid);
            preparedStatement.setObject(2,flugid);
            int exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.created(UriBuilder.fromUri("http://localhost:8080/flugtickets/"+flugticketid+"/fluege/"+flugid).build()).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
}

