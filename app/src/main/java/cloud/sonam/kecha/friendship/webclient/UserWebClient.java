package cloud.sonam.kecha.friendship.webclient;

import cloud.sonam.kecha.friendship.model.SeUser;
import cloud.sonam.kecha.friendship.model.SeUserFriend;
import cloud.sonam.kecha.friendship.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public class UserWebClient {
    private static final Logger LOG = LoggerFactory.getLogger(UserWebClient.class);

    private final WebClient.Builder webClientBuilder;

    private final String userByAuthIdEp;

    public UserWebClient(WebClient.Builder webClientBuilder,
                         String userByAuthIdEp) {
        this.webClientBuilder = webClientBuilder;
        this.userByAuthIdEp = userByAuthIdEp;
    }

    public Mono<Map<String, String>> getUserByAuthenticationId(User user, SeUserFriend seUserFriend, String authenticationId) {
        final String userInfoEndpoint = userByAuthIdEp.replace("{authenticationId}", authenticationId);
        LOG.info("get user by authId endpoint: {}", userInfoEndpoint);

        WebClient.ResponseSpec responseSpec = webClientBuilder.build().get().uri(userInfoEndpoint)
                .retrieve();

        return responseSpec.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
        }).onErrorResume(throwable -> {
            LOG.error("failed to get user by authId endpoint '{}' with error: {}",
                    userInfoEndpoint, throwable.getMessage());
            return Mono.error(new RuntimeException("user info call failed, error: " + throwable.getMessage()));
        });
    }

    public Mono<User> findById(UUID id) {
        final String userEndpoint = userByAuthIdEp.replace("{id}", id.toString());
        LOG.info("get user by id endpoint: {}", userEndpoint);

        WebClient.ResponseSpec responseSpec = webClientBuilder.build().get().uri(userEndpoint)
                .retrieve();

        return responseSpec.bodyToMono(User.class).onErrorResume(throwable -> {
            LOG.error("failed to get user by id endpoint '{}' with error: {}",
                    userEndpoint, throwable.getMessage());
            return Mono.error(new RuntimeException("get user by id call failed, error: " + throwable.getMessage()));
        });
    }


}