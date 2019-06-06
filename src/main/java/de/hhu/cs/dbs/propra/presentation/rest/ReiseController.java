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
public class ReiseController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;


    @Path("/reisen/{reiseid}")
    @RolesAllowed({"OFFICE"})
    @DELETE
    public Response delete_reise(@PathParam("reiseid") String reiseid) {
        try{
            Connection connection = dataSource.getConnection();
            if (reiseid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Flugticket ID").build();
            }
            // DELETE Flights of Ticket
            String stringStatement = "DELETE FROM Reise_belegt_Unterkunft WHERE Reise_Buchung_ID = ?";
            System.out.println(stringStatement);
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, reiseid);
            int exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            // DELETE Ticket
            stringStatement = "DELETE FROM Reise WHERE Buchung_ID = ?";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, reiseid);
            exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.status(Response.Status.OK).entity(exit_code).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Error in SQL").build();
        }
    }

    @Path("/reisen")
    @GET
    @RolesAllowed({"OFFICE"})
    public Response list_reisen(@QueryParam("rowid") String rowid, @QueryParam("titel") String titel, @QueryParam("startdatum") String startdatum, @QueryParam("tags") String tags){
        try{
            Connection connection = dataSource.getConnection();
            // Checken ob wirklich Tage berechnet sind!
            String stringStatement = "SELECT DISTINCT r.*,datetime(CAST(strftime('%s',r.Startzeitpunkt) as INTEGER)+(r.Dauer*60*60*24),'unixepoch') From Reise r, Reise_belegt_Unterkunft rbu WHERE r.Buchung_ID = rbu.Reise_Buchung_ID ";
            if (rowid != null) stringStatement = stringStatement + " AND r.Buchung_ID = ? ";
            if (titel != null) stringStatement = stringStatement + " AND r.Titel LIKE \"%"+ titel +"%\"";
            if (startdatum != null) {
                String[] splits = startdatum.split(" ");
                String day_start = " CAST(strftime('%s',\"" + splits[0] + " 00:00:00\") as INTEGER) ";
                String day_end = " CAST(strftime('%s',\"" + splits[0] + " 23:59:59\") as INTEGER) ";
                String start_point = " CAST(strftime('%s',r.Startzeitpunkt) as INTEGER) ";
                stringStatement = stringStatement + " AND "+ start_point +">="+ day_start + " AND "+start_point+"<=" + day_end;
            }
            if (tags != null){
                //Reise_hat_Tag
                stringStatement = stringStatement + "";
                String[] fields = tags.split("AND");
                for (int i = 0; i < fields.length; i++) {
                    stringStatement = stringStatement + " AND r.Buchung_ID IN (SELECT Reise_ID FROM Reise_hat_Tag WHERE Tag_Bezeichnung = \""+ fields[i] +"\")";
                }
            }
            stringStatement = stringStatement + ";";
            System.out.println(stringStatement);
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("reiseid", resultSet.getObject(1));
                entity.put("titel", resultSet.getObject(4));
                entity.put("startdatum", resultSet.getObject(2));
                entity.put("rueckkehrdatum", resultSet.getObject(5));
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
    @Path("/reisen")
    public Response insert_reise(@FormDataParam("startzeitpunkt") String startzeitpunkt, @FormDataParam("dauer") String dauer, @FormDataParam("titel") String titel, @FormDataParam("unterkunftid") String unterkunftid, @FormDataParam("preis") String preis) {
        try{
            if (startzeitpunkt == null || dauer == null || titel == null || unterkunftid == null || preis == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Nicht alle Parameter gesetzt").build();
            }
            Connection connection = dataSource.getConnection();
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
            stringStatement = "SELECT ID FROM Buchung WHERE Preis = ? AND Reisebuero_Username = ? ORDER BY rowid DESC LIMIT 1;";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, preis);
            preparedStatement.setObject(2, securityContext.getUserPrincipal().getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            Object buchung_id = resultSet.getObject(1);
            System.out.println(exit_code);
            resultSet.close();
            // Create Reise
            System.out.println("Insert Reise");
            stringStatement = "INSERT INTO Reise(Buchung_ID,Startzeitpunkt,Dauer,Titel) values(?,?,?,?);";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, buchung_id);
            preparedStatement.setObject(2, startzeitpunkt);
            preparedStatement.setObject(3, dauer);
            preparedStatement.setObject(4, titel);
            exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            // Add Reise -> Unterkunft
            System.out.println("Insert Reise -> Unterkunft");
            stringStatement = "INSERT INTO Reise_belegt_Unterkunft(Reise_Buchung_ID,Unterkunft_ID) values(?,?);";
            System.out.println(stringStatement);
            preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1, buchung_id);
            preparedStatement.setObject(2, unterkunftid);
            exit_code = preparedStatement.executeUpdate();
            System.out.println(exit_code);
            connection.close();
            return Response.status(Response.Status.CREATED).entity(exit_code).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Error in SQL").build();
        }
    }

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/reisen/{reiseid}/unterkuenfte")
    public Response list_own_flight_on_ticket(@PathParam("reiseid") String reiseid){
        try{
            if (reiseid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Reise ID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT b.ID,r.Titel,u.ID,u.Bezeichnung FROM Buchung b, Unterkunft u, Reise r, Reise_belegt_Unterkunft rbu WHERE u.ID = rbu.Unterkunft_ID AND r.Buchung_ID = rbu.Reise_Buchung_ID AND b.ID = ? AND b.Reisebuero_Username = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,reiseid);
            preparedStatement.setObject(2,securityContext.getUserPrincipal().getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("reiseid", resultSet.getObject(1));
                entity.put("titel", resultSet.getObject(2));
                entity.put("unterkunftid", resultSet.getObject(3));
                entity.put("unterkunft_bez", resultSet.getObject(4));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("SQL Error").build();
        }
    }


    @POST
    @RolesAllowed({"OFFICE"})
    @Path("/reisen/{reiseid}/tags")
    public Response insert_reise_tags(@PathParam("reiseid") String reiseid, @FormDataParam("tag") String tag) {
        try{
            if (reiseid == null || tag == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Nicht alle Parameter gesetzt").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT * FROM Buchung WHERE ID = ? AND Reisebuero_Username = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,reiseid);
            preparedStatement.setObject(2,securityContext.getUserPrincipal().getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                resultSet.close();
                // Add Tags
                String[] fields = tag.split("AND");
                for (int i = 0; i < fields.length; i++) {
                    String stringInsertStatement = "INSERT INTO Reise_hat_Tag(Reise_ID,Tag_Bezeichnung) values(?,?);";
                    System.out.println(stringInsertStatement);
                    PreparedStatement preparedInsertStatement = connection.prepareStatement(stringInsertStatement);
                    preparedInsertStatement.setObject(1,reiseid);
                    preparedInsertStatement.setObject(2,fields[i]);
                    preparedInsertStatement.closeOnCompletion();
                    int exit_insert_code = preparedInsertStatement.executeUpdate();
                    preparedInsertStatement.close();
                    System.out.println(exit_insert_code);
                }
                connection.close();
                return Response.status(Response.Status.CREATED).entity("Alle Tags eingetragen").build();

            } else {
                connection.close();
                return Response.status(Response.Status.FORBIDDEN).entity("Nicht die eigene Buchung").build();
            }
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Error in SQL").build();
        }
    }

    @GET
    @RolesAllowed({"OFFICE"})
    @Path("/reisen/{reiseid}/tags")
    public Response list_tags_von_reise(@PathParam("reiseid") String reiseid){
        try{
            if (reiseid == null){
                return Response.status(Response.Status.NO_CONTENT).entity("Keine Reise ID").build();
            }
            Connection connection = dataSource.getConnection();
            String stringStatement = "SELECT Tag_Bezeichnung FROM Reise_hat_Tag WHERE Reise_ID = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(stringStatement);
            preparedStatement.closeOnCompletion();
            preparedStatement.setObject(1,reiseid);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new LinkedHashMap<>();
                entity.put("bezeichnung", resultSet.getObject(1));
                entities.add(entity);
            }
            resultSet.close();
            connection.close();
            return Response.status(Response.Status.OK).entity(entities).build();
        } catch (SQLException ex){
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("SQL Error").build();
        }
    }

}
