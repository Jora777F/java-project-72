package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.Messages;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.sql.SQLException;
import java.sql.Timestamp;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UrlCheckController {

    private UrlCheckController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx, UrlRepository urlRepository,
                              UrlCheckRepository urlCheckRepository) throws SQLException {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();
        Url url = urlRepository.findById(urlId).orElseThrow(() -> new NotFoundResponse("Страница не найдена."));

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = doc.title();

            Element h1Element = doc.selectFirst("h1");
            var h1 = h1Element == null ? "" : h1Element.text();

            var descriptionElement = doc.selectFirst("meta[name=description]");
            var description = descriptionElement == null ? "" : descriptionElement.attr("content");

            var urlCheck = new UrlCheck(statusCode, title, h1, description, urlId,
                    new Timestamp(System.currentTimeMillis()));

            urlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flashType", "success");
            ctx.sessionAttribute("flashMessage", Messages.CHECK_SUCCESS);
        } catch (Exception e) {
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Не удалось проверить страницу");
        }

        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
