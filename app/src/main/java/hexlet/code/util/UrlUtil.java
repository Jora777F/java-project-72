package hexlet.code.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class UrlUtil {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private UrlUtil() {
        throw new UnsupportedOperationException("This is a service class, "
                + "the creation of instances is prohibited.");
    }

    public static String normalizeUrl(String urlString) throws URISyntaxException, MalformedURLException {
        if (!urlString.startsWith(HTTP) && !urlString.startsWith(HTTPS)) {
            urlString = HTTP + urlString;
        }

        URI uri = new URI(urlString);
        URL url = uri.toURL();

        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();

        // Стандартные порты не включаем в URL
        if (port == -1 || (protocol.equals("http") && port == 80)
                || (protocol.equals("https") && port == 443)) {
            return protocol + "://" + host;
        }

        return protocol + "://" + host + ":" + port;
    }
}
