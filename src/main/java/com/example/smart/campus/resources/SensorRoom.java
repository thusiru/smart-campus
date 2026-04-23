/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.resources;

import com.example.smart.campus.data.DataStore;
import com.example.smart.campus.exceptions.DataNotFoundException;
import com.example.smart.campus.exceptions.RoomNotEmptyException;
import com.example.smart.campus.models.Room;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Thusiru Kodithuwakku
 */
@Path("/rooms")
public class SensorRoom {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        return Response.ok(DataStore.roomDAO.getAll()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        // Ensure request body is not empty
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Request body is empty or malformed JSON.\"}").build();
        }

        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }

        if (DataStore.roomDAO.getById(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT).entity("{\"error\":\"A room with this ID already exists\"}").build();
        }

        DataStore.roomDAO.add(room);

        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.roomDAO.getById(roomId);

        if (room == null) {
            throw new DataNotFoundException("Room '" + roomId + "' not found.");
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.roomDAO.getById(roomId);

        if (room == null) {
            throw new DataNotFoundException("Room '" + roomId + "' not found.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room: " + roomId + " because it still contains active sensors.");
        }

        DataStore.roomDAO.delete(roomId);

        return Response.noContent().build();
    }
}
