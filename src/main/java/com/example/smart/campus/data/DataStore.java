/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smart.campus.data;

import com.example.smart.campus.models.Room;
import com.example.smart.campus.models.Sensor;
import java.util.ArrayList;
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

    static {
        Room library = new Room("LIB-301", "Library Quiet Study", 50, new ArrayList<>());
        Room lab = new Room("LAB-104", "Advanced Computing Lab", 25, new ArrayList<>());
        Room lectureHall = new Room("LEC-A", "Main Lecture Hall", 200, new ArrayList<>());

        roomDAO.add(library);
        roomDAO.add(lab);
        roomDAO.add(lectureHall);

        Sensor tempSensor = new Sensor("SENS-TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor co2Sensor = new Sensor("SENS-CO2-042", "CO2", "ACTIVE", 410.0, "LIB-301");
        Sensor motionSensor = new Sensor("SENS-MOT-099", "Motion", "MAINTENANCE", 0.0, "LAB-104");
        Sensor humiditySensor = new Sensor("SENS-HUM-012", "Humidity", "OFFLINE", 55.2, "LEC-A");

        sensorDAO.add(tempSensor);
        sensorDAO.add(co2Sensor);
        sensorDAO.add(motionSensor);
        sensorDAO.add(humiditySensor);

        library.getSensorIds().add(tempSensor.getId());
        library.getSensorIds().add(co2Sensor.getId());
        lab.getSensorIds().add(motionSensor.getId());
        lectureHall.getSensorIds().add(humiditySensor.getId());
    }
}
