package ru.yandex.practicum.moviehub;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store = new MoviesStore();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        // GET /movies
        if (method.equals("GET") && path.equals("/movies")) {

            String query = ex.getRequestURI().getQuery();

            if (query != null && query.startsWith("year=")) {

                try {

                    int year = Integer.parseInt(query.substring(5));

                    List<Movie> movies = store.getByYear(year);

                    sendJson(ex, 200, gson.toJson(movies));

                } catch (NumberFormatException e) {

                    sendJson(ex, 400,
                            gson.toJson("Некорректный параметр 'year': должно быть число"));
                }

                return;
            }

            sendJson(ex, 200, gson.toJson(store.getAll()));
            return;
        }

        // POST /movies
        if (method.equals("POST") && path.equals("/movies")) {

            String contentType = ex.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null || !contentType.contains("application/json")) {

                sendJson(ex, 415,
                        gson.toJson("Неподдерживаемый тип содержимого. Ожидается application/json"));
                return;
            }

            Movie movie = gson.fromJson(
                    new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8),
                    Movie.class
            );

            List<String> errors = validate(movie);

            if (!errors.isEmpty()) {

                ErrorResponse err = new ErrorResponse(
                        "Ошибка валидации",
                        errors
                );

                sendJson(ex, 422, gson.toJson(err));
                return;
            }

            Movie created = store.add(movie);

            sendJson(ex, 201, gson.toJson(created));
            return;
        }

        // /movies/{id}
        if (path.startsWith("/movies/")) {

            String idStr = path.substring("/movies/".length());

            int id;

            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {

                sendJson(ex, 400,
                        gson.toJson("Некорректный ID: " + idStr + ". ID должен быть числом"));
                return;
            }

            if (method.equals("GET")) {

                Movie movie = store.getById(id);

                if (movie == null) {

                    sendJson(ex, 404,
                            gson.toJson("Фильм с ID " + id + " не найден"));
                    return;
                }

                sendJson(ex, 200, gson.toJson(movie));
                return;
            }

            if (method.equals("DELETE")) {

                boolean removed = store.delete(id);

                if (!removed) {

                    sendJson(ex, 404,
                            gson.toJson("Фильм с ID " + id + " не найден"));
                    return;
                }

                sendNoContent(ex);
                return;
            }
        }

        sendJson(ex, 405,
                gson.toJson("Метод " + method + " не поддерживается"));
    }

    private List<String> validate(Movie movie) {

        List<String> errors = new ArrayList<>();

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("Название фильма не должно быть пустым");
        }

        if (movie.getTitle() != null && movie.getTitle().length() > 100) {
            errors.add("Название фильма не должно быть длиннее 100 символов");
        }

        int currentYear = Year.now().getValue();

        if (movie.getYear() < 1888 || movie.getYear() > currentYear + 1) {
            errors.add("Год должен быть между 1888 и " + (currentYear + 1));
        }

        return errors;
    }
}