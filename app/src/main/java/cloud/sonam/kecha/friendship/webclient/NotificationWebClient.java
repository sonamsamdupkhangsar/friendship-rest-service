package cloud.sonam.kecha.friendship.webclient;

import cloud.sonam.kecha.friendship.model.SeUserFriend;
import cloud.sonam.kecha.friendship.model.User;
import cloud.sonam.kecha.friendship.notification.FriendNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public class NotificationWebClient {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationWebClient.class);

    private final WebClient.Builder webClientBuilder;

    private final String notificationEndpoint;

    public NotificationWebClient(WebClient.Builder webClientBuilder,
                                 String notificationEndpoint) {
        this.webClientBuilder = webClientBuilder;
        this.notificationEndpoint = notificationEndpoint;
    }

    public Mono<Map<String, String>> sendFriendNotification(User user, SeUserFriend seUserFriend, FriendNotification.Event event) {
        final String userInfoEndpoint = notificationEndpoint;
        LOG.info("get user by authId endpoint: {}", userInfoEndpoint);

        WebClient.ResponseSpec responseSpec = webClientBuilder.build().get().uri(userInfoEndpoint)
                .retrieve();

        return responseSpec.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
        }).onErrorResume(throwable -> {
            LOG.error("failed to call notification endpoint '{}' with error: {}",
                    userInfoEndpoint, throwable.getMessage());
            return Mono.error(new RuntimeException("notification call failed, error: " + throwable.getMessage()));
        });
    }



}