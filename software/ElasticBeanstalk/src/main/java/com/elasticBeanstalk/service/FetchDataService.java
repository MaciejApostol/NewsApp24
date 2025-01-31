package com.elasticBeanstalk.service;

import com.elasticBeanstalk.dao.News;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class FetchDataService {
    public final SecretsService secretsService = SecretsService.getSecrets();

    private final String cityHost = "api.openweathermap.org";
    private final String cityPath = "/geo/1.0/direct";
    private final String cityApiKey = secretsService.CITY_API_KEY;
    public final HashMap<String, String> cityApiUriParams = new HashMap<>(Map.of(
            "q", "",
            "appid", cityApiKey
    ));

    private final String newsHost = "api.bing.microsoft.com";
    private final String newsPath = "/v7.0/news/search";
    private final String newsApiKey = secretsService.NEWS_API_KEY;
    private final HashMap<String, String> newsApiUriParams = new HashMap<>(Map.of(
            "q", "",
            "count", "25",
            "mkt", "en-US",
            "originalImg", "true",
            "setLang", "en-US",
            "sortBy", "Relevance"
    ));
    private final HashMap<String, String> newsApiUriHeaders = new HashMap<>(Map.of(
            "Ocp-Apim-Subscription-Key", newsApiKey));

    public static final String countryCode = "US";
    public static final String trending = "trending";

    private final WebClient webClient;

    public FetchDataService(WebClient webClient) {
        this.webClient = webClient;
    }

    public WebClient.ResponseSpec getResponse(String host, String path, Map<String, String> params,
                                              Map<String, String> headers) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(host)
                .path(path);

        if (params != null) {
            params.forEach(uriComponentsBuilder::queryParam);
        }

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = webClient.get()
                .uri(uriComponentsBuilder
                        .build()
                        .toUriString());
        if (headers != null) {
            headers.forEach(requestHeadersSpec::header);
        }
        return requestHeadersSpec.retrieve();
    }

    public Mono<News> fetchCity(String query) {
        cityApiUriParams.put("q", query);
        return getResponse(cityHost, cityPath, cityApiUriParams, null)
                .bodyToFlux(News.class).single();
    }

    public Mono<News> fetchNews(News news) {
        String query;
        if (news.getCityName().equals(trending)) {
            newsApiUriParams.put("category", countryCode);
            query = "usa news";
        } else {
            query = String.join(",", List.of(news.getCityName(), news.getState()));
        }
        newsApiUriParams.put("q", query);

        return
                getResponse(newsHost, newsPath, newsApiUriParams, newsApiUriHeaders)
                        .bodyToMono(News.class)
                        .map(fetchedNews -> {
                            fetchedNews.setCityName(news.getCityName());
                            fetchedNews.setState(news.getState());
                            return fetchedNews;
                        })
                        .filter(fetchedNews -> !fetchedNews.getArticles().isEmpty());
    }
}
