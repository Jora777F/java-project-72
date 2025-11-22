package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.Messages;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class UrlsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";
    private static final String SUCCESS_TYPE = "success";
    private static final String INFO_TYPE = "info";

    private UrlsController() {
        throw new UnsupportedOperationException("This is a service class, "
                + "the creation of instances is prohibited.");
    }

    public static void create(Context ctx, UrlRepository urlRepository) throws SQLException {
        String urlString = ctx.formParam("url");
        LOGGER.debug("Initial URL input: '{}'", urlString);

        if (urlString == null || urlString.isBlank()) {
            setFlash(ctx, DANGER_TYPE, Messages.INCORRECT_URL);
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        String normalizedUrl;
        try {
            normalizedUrl = UrlUtil.normalizeUrl(urlString);
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("URL normalization failed for '{}': {}", urlString, e.getMessage());
            setFlash(ctx, DANGER_TYPE, Messages.INCORRECT_URL);
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        LOGGER.debug("Checking if URL exists: '{}'", normalizedUrl);
        var existingUrl = urlRepository.findByName(normalizedUrl);

        if (existingUrl.isPresent()) {
            setFlash(ctx, INFO_TYPE, Messages.FOUND);
            ctx.redirect(NamedRoutes.urlPath(existingUrl.get().getId()));
            return;
        }

        Url url = new Url(normalizedUrl, new Timestamp(System.currentTimeMillis()));
        urlRepository.save(url);
        LOGGER.info("Successfully created URL, ID: {}", url.getId());

        setFlash(ctx, SUCCESS_TYPE, Messages.SUCCESS);
        ctx.redirect(NamedRoutes.urlsPath());
    }

    public static void index(Context ctx, UrlRepository urlRepository) throws SQLException {
        List<Url> urls = urlRepository.getEntities();
        UrlsPage page = new UrlsPage(urls);

        // Читаем flash-сообщения из сессии
        populateFlash(ctx, page);

        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx, UrlRepository urlRepository) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();

        Optional<Url> urlOptional = urlRepository.findById(id);

        if (urlOptional.isEmpty()) {
            ctx.status(404).result("URL not found");
            return;
        }

        Url url = urlOptional.get();
        UrlPage page = new UrlPage(url);
        populateFlash(ctx, page);

        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }

    private static void populateFlash(Context ctx, BasePage page) {
        String flashType = ctx.consumeSessionAttribute(FLASH_TYPE);
        String flashMessage = ctx.consumeSessionAttribute(FLASH_MESSAGE);

        page.setFlashType(flashType);
        page.setFlashMessage(flashMessage);
    }

    private static void setFlash(Context ctx, String flashType, String flashMessage) {
        ctx.sessionAttribute(FLASH_TYPE, flashType);
        ctx.sessionAttribute(FLASH_MESSAGE, flashMessage);
    }
}
