/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.resources;

import com.example.smart.campus.data.DataStore;
import com.example.smart.campus.exceptions.DataConflictException;
import com.example.smart.campus.exceptions.InvalidPayloadException;
import com.example.smart.campus.exceptions.LinkedResourceNotFoundException;
import com.example.smart.campus.models.Room;
import com.example.smart.campus.models.Sensor;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Thusiru Kodithuwakku
 */
@Path("/sensors")
public class SensorResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Ensure request body is not empty
        if (sensor == null) {
            throw new InvalidPayloadException("Request body is empty or malformed JSON.");
        }

        // Ensure the sensor has an ID
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId(UUID.randomUUID().toString());
        }

        // Prevent duplicate sensor IDs
        if (DataStore.sensorDAO.getById(sensor.getId()) != null) {
            throw new DataConflictException("Sensor with ID '" + sensor.getId() + "' already exists.");
        }

        // Verify the roomId was actually provided in the JSON payload
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new InvalidPayloadException("A valid room id must be provided.");
        }

        // Verify the roomId actually exists in the system
        Room room = DataStore.roomDAO.getById(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Cannot create sensor. Room ID '" + sensor.getRoomId() + "' does not exist.");
        }

        // Save the sensor
        DataStore.sensorDAO.add(sensor);

        // Add the sensor ID to the room's list
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = DataStore.sensorDAO.getAll();

        // If no type was provided, return the whole list
        if (type == null || type.trim().isEmpty()) {
            return Response.ok(allSensors).build();
        }

        // If a type WAS provided (e.g., ?type=CO2), filter the list using Java Streams
        List<Sensor> filteredSensors = allSensors.stream().filter(sensor -> type.equalsIgnoreCase(sensor.getType())).collect(Collectors.toList());

        return Response.ok(filteredSensors).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
