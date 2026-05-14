package ru.yandex.practicum.moviehub;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {

    private final HttpServer server;

    public MoviesServer() {

        try {

            server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/movies", new MoviesHandler());

        } catch (IOException e) {

            throw new RuntimeException("Не удалось создать сервер", e);
        }
    }

    public void start() {

        server.start();

        System.out.println("Server started on port 8080");
    }

    public void stop() {

        server.stop(0);
    }

    public static void main(String[] args) {

        MoviesServer server = new MoviesServer();

        server.start();
    }
}