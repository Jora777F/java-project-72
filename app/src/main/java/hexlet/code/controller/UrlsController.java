package hexlet.code.controller;

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

public final class UrlsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    public static final String FLASH_TYPE = "flashType";
    public static final String FLASH_MESSAGE = "flashMessage";
    public static final String DANGER_TYPE = "danger";
    public static final String SUCCESS_TYPE = "success";
    public static final String INFO_TYPE = "info";

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

    private static void setFlash(Context ctx, String flashType, String flashMessage) {
        ctx.sessionAttribute(FLASH_TYPE, flashType);
        ctx.sessionAttribute(FLASH_MESSAGE, flashMessage);
    }
}
