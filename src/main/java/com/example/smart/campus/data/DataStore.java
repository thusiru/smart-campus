/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.data;

import com.example.smart.campus.models.Room;
import com.example.smart.campus.models.Sensor;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Thusiru Kodithuwakku
 */
public class DataStore {

    public static final GenericDAO<Room> roomDAO = new GenericDAO<>(new CopyOnWriteArrayList<>());
    public static final GenericDAO<Sensor> sensorDAO = new GenericDAO<>(new CopyOnWriteArrayList<>());

    private DataStore() {
    }
}
