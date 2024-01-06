package org.example;

import Interfaces.Registration;
import org.example.Repository.DatabaseConnectionManager;
import org.example.Repository.UserRepository;
import org.example.Service.UserService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class App {
    public static void main(String[] args) {
        try {
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            UserRepository userRepository = new UserRepository(connectionManager.getMyConnection());
            Registration registrationService = new UserService(userRepository);


            Registry registry = LocateRegistry.createRegistry(1100);
            registry.rebind("RegistrationService", registrationService);
            System.out.println("Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}