/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.exceptions.core;

/**
 *
 * @author Thusiru Kodithuwakku
 */
public class InvalidPayloadException extends ApiException {

    public InvalidPayloadException(String message) {
        super(400, message, "https://api.smartcampus.com/docs/errors/bad-request");
    }
}
