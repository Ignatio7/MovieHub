package ru.yandex.practicum.moviehub;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store = new MoviesStore();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/movies")) {

            List<Movie> movies = store.getAll();

            sendJson(ex, 200, gson.toJson(movies));
            return;
        }

        if (method.equals("POST") && path.equals("/movies")) {

            Movie movie = gson.fromJson(
                    new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8),
                    Movie.class
            );

            Movie created = store.add(movie);

            sendJson(ex, 201, gson.toJson(created));
            return;
        }

        if (path.startsWith("/movies/")) {

            String idStr = path.substring("/movies/".length());

            int id;

            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                sendJson(ex, 400, gson.toJson("Некорректный ID"));
                return;
            }

            if (method.equals("GET")) {

                Movie movie = store.getById(id);

                if (movie == null) {
                    sendJson(ex, 404, gson.toJson("Фильм не найден"));
                    return;
                }

                sendJson(ex, 200, gson.toJson(movie));
                return;
            }

            if (method.equals("DELETE")) {

                boolean removed = store.delete(id);

                if (!removed) {
                    sendJson(ex, 404, gson.toJson("Фильм не найден"));
                    return;
                }

                sendNoContent(ex);
                return;
            }
        }

        sendJson(ex, 405, gson.toJson("Метод не поддерживается"));
    }
}