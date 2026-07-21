package backend.demo.discovery.service;

import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class HttpPageContentFetcher implements PageContentFetcher {
    private final RestTemplate restTemplate;

    public HttpPageContentFetcher() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String fetch(String url) {
        try {
            String body = restTemplate.getForObject(url, String.class);
            return body == null ? "" : body;
        } catch (Exception ignored) {
            return "";
        }
    }
}
