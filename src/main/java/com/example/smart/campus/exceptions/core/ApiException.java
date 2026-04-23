package com.example.smart.campus.exceptions.core;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Thusiru Kodithuwakku
 */
public class ApiException extends RuntimeException {

    private final int statusCode;
    private final String documentation;

    public ApiException(int statusCode, String message, String documentation) {
        super(message);
        this.statusCode = statusCode;
        this.documentation = documentation;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDocumentation() {
        return documentation;
    }
}
