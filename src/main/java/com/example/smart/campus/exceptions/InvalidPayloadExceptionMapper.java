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
public class InvalidPayloadExceptionMapper implements ExceptionMapper<InvalidPayloadException> {

    @Override
    public Response toResponse(InvalidPayloadException exception) {
        ErrorMessage error = new ErrorMessage(exception.getMessage(), 400, "https://api.smartcampus.com/docs/errors/bad-request");
        return Response.status(Response.Status.BAD_REQUEST).entity(error).type(MediaType.APPLICATION_JSON).build();
    }
}
