/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.resources;

import com.example.smart.campus.data.DataStore;
import com.example.smart.campus.exceptions.core.DataNotFoundException;
import com.example.smart.campus.exceptions.core.InvalidPayloadException;
import com.example.smart.campus.exceptions.domain.SensorUnavailableException;
import com.example.smart.campus.models.Sensor;
import com.example.smart.campus.models.SensorReading;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Thusiru Kodithuwakku
 */
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReading() {
        Sensor sensor = DataStore.sensorDAO.getById(sensorId);

        if (sensor == null) {
            throw new DataNotFoundException("Sensor '" + sensorId + "' not found.");
        }

        return Response.ok(sensor.getReadings()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading sensorReading) {
        // Ensure request body is not empty
        if (sensorReading == null) {
            throw new InvalidPayloadException("Request body is empty or malformed JSON.");
        }

        Sensor sensor = DataStore.sensorDAO.getById(sensorId);

        if (sensor == null) {
            throw new DataNotFoundException("Cannot add reading. Sensor '" + sensorId + "' not found.");
        }

        if (sensorReading.getId() == null || sensorReading.getId().trim().isEmpty()) {
            sensorReading.setId(UUID.randomUUID().toString());
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings.");
        }

        sensor.getReadings().add(sensorReading);

        sensor.setCurrentValue(sensorReading.getValue());

        return Response.status(Response.Status.CREATED).entity(sensorReading).build();
    }
}
