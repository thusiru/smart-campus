/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.exceptions.core;

/**
 *
 * @author Thusiru Kodithuwakku
 */
public class DataNotFoundException extends ApiException {

    public DataNotFoundException(String message) {
        super(404, message, "https://api.smartcampus.com/docs/errors/not-found");
    }
}
