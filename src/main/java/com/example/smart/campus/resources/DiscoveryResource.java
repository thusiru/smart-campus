/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.resources;

/**
 *
 * @author Thusiru Kodithuwakku
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0");
        metadata.put("admin_contact", "admin@smartcampus.ac.uk");

        Map<String, String> collections = new HashMap<>();
        collections.put("rooms", "/api/v1/rooms");
        collections.put("sensors", "/api/v1/sensors");

        metadata.put("collections", collections);
        metadata.put("version", "1.0");
        metadata.put("admin_contact", "admin@smartcampus.ac.uk");

        return Response.ok(metadata).build();
    }
}
