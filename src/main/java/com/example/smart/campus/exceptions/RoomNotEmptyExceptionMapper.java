/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.exceptions;

import com.example.smart.campus.models.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Thusiru Kodithuwakku
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorMessage error = new ErrorMessage(
                exception.getMessage(),
                409,
                "https://api.smartcampus.com/docs/errors/room-not-empty"
        );

        return Response.status(Response.Status.CONFLICT).entity(error).type(MediaType.APPLICATION_JSON).build();
    }
}
