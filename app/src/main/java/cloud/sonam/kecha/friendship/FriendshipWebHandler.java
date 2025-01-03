package cloud.sonam.kecha.friendship;

import cloud.sonam.kecha.friendship.impl.FriendshipService;
import cloud.sonam.kecha.friendship.model.SeUserFriend;
import cloud.sonam.kecha.friendship.notification.FriendNotification;
import cloud.sonam.kecha.friendship.util.UserFriendBuilder;
import cloud.sonam.kecha.friendship.webclient.NotificationWebClient;
import cloud.sonam.kecha.friendship.webclient.UserWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Service
public class FriendshipWebHandler implements FriendshipHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FriendshipWebHandler.class);

    private final FriendshipService friendshipService;
    private final UserWebClient userWebClient;
    private final NotificationWebClient notificationWebClient;
    private final UserFriendBuilder userFriendBuilder;

    public FriendshipWebHandler(FriendshipService friendshipService, UserWebClient userWebClient,
                                NotificationWebClient notificationWebClient, UserFriendBuilder userFriendBuilder) {
        this.friendshipService = friendshipService;
        this.userWebClient = userWebClient;
        this.notificationWebClient = notificationWebClient;
        this.userFriendBuilder = userFriendBuilder;
    }

    @Override
    public Mono<ServerResponse> isFriends(ServerRequest serverRequest) {
        UUID friendId = UUID.fromString(serverRequest.pathVariable("userId"));

        return getLoggedInUserId().flatMap(userId -> {
                    userWebClient.findById(friendId);
                   return friendshipService.isFriends(userId, friendId);
                }).flatMap(s ->  ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in isFriends method", throwable);
                    LOG.error("failed in isFriends method {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "isFriends() method failed with error: " + throwable.getMessage()));
                });
    }

    @Override
    public Mono<ServerResponse> requestFriendshipWith(ServerRequest serverRequest) {
        UUID friendId = UUID.fromString(serverRequest.pathVariable("userId"));

        LOG.debug("request friendship with {}", friendId);

         return createFriendship(friendId).flatMap(s ->  ServerResponse.created(URI.create("/friendships/"+ s.getFriendshipId()))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in creating Friendship", throwable);
                    LOG.error("requestFriendship failed {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "requestFriendship failed with error: " + throwable.getMessage()));
                });
    }

    private Mono<SeUserFriend> createFriendship(UUID friendId) {
        return getLoggedInUserId().flatMap(userId -> friendshipService.requestFriendship(userId, friendId)
                        .zipWith(Mono.just(userId))
                ).flatMap(objects ->
                    userWebClient.findById(objects.getT2()).zipWith(Mono.just(objects.getT1())))//userId, Friendship
                .flatMap(objects -> UserFriendBuilder.getUserFriend(objects.getT1(), objects.getT2())
                        .zipWith(Mono.just(objects.getT1())).zipWith(Mono.just(objects.getT2())))//SeUserFriend, User, Friendship objects
                .doOnNext(objects -> notificationWebClient.sendFriendNotification(objects.getT1().getT2(), objects.getT1().getT1(),
                        FriendNotification.Event.REQUEST))
                .flatMap(objects -> userWebClient.findById(objects.getT2().getFriendId()).zipWith(Mono.just(objects.getT2())))
                .flatMap(objects -> userFriendBuilder.createSeUserFriendOnRequest(objects.getT1(), objects.getT2()));
    }

    @Override
    public Mono<ServerResponse> declineFriendship(ServerRequest serverRequest) {
        UUID friendshipId = UUID.fromString(serverRequest.pathVariable("friendshipId"));
        LOG.debug("cancel friendship with friendship-id {}", friendshipId);

        return friendshipService.delete(friendshipId).flatMap(s ->  ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in declining Friendship", throwable);
                    LOG.error("declineFriendship failed {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "declineFriendship failed with error: " + throwable.getMessage()));
                });
    }

    private Mono<UUID> getLoggedInUserId() {
        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            org.springframework.security.core.Authentication authentication = securityContext.getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdString = jwt.getClaim("userId");
            UUID userId = UUID.fromString(userIdString);

            return Mono.just(userId);
        });
    }

    @Override
    public Mono<ServerResponse> acceptFriendship(ServerRequest serverRequest) {
        UUID friendshipId = UUID.fromString(serverRequest.pathVariable("friendshipId"));

        LOG.info("accept friendshipId {}", friendshipId);

        return acceptFriendship(friendshipId).flatMap(s ->  ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in accepting Friendship", throwable);
                    LOG.error("acceptFriendship failed {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "acceptFriendship failed with error: " + throwable.getMessage()));
                });
    }

    public Mono<SeUserFriend> acceptFriendship(UUID friendshipId) {
        LOG.info("get userFriend object from confirmFriendship");

        // the logged-in userId is the friend-id in the Friendship[userId, friendId]
        return getLoggedInUserId().flatMap(userId -> friendshipService.confirmFriendship(userId, friendshipId)
                        .zipWith(Mono.just(userId)))//Friendship, userId
                .flatMap(objects -> userWebClient.findById(objects.getT2()).zipWith(Mono.just(objects.getT1()))) //User, Friendship

                .flatMap(objects -> UserFriendBuilder.getUserFriend(objects.getT1(),
                        objects.getT2()).zipWith(Mono.just(objects.getT2())))//SeUserFriend, Friendship

                .flatMap(objects -> userWebClient.findById(objects.getT2().getUserId())//get user by the userId in friendship
                        .zipWith(Mono.just(objects.getT1())).zipWith(Mono.just(objects.getT2())))//User (userId),SeUserFriend, Friendhsip
                       // .zipWith(Mono.just(objects.getT1())))
                //send notification to the user in Friendship[userId] that the friend has accepted their friendship
                .flatMap(objects -> notificationWebClient.
                        sendFriendNotification(objects.getT1().getT1(), objects.getT1().getT2(), FriendNotification.Event.CONFIRM)
                        .thenReturn(objects.getT1().getT2()))
                .doOnNext(seUserFriend -> LOG.info("go userFiend object {}", seUserFriend));
    }

    /**
     * This method is called when either user or friend wants to terminate the
     * friendship
     * @param serverRequest
     * @return
     */
    @Override
    public Mono<ServerResponse> cancelFriendship(ServerRequest serverRequest) {
        UUID friendshipId = UUID.fromString(serverRequest.pathVariable("friendshipId"));

        LOG.info("cancel friendship for friendshipId {}", friendshipId);

        return friendshipService.delete(friendshipId).flatMap(s ->  ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in cancel Friendship", throwable);
                    LOG.error("cancelFriendship failed {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "cancelFriendship failed with error: " + throwable.getMessage()));
                });
    }

    /**
     * returns a stream of SeUserFriend
     * @param serverRequest
     * @return
     */
    @Override
    public Mono<ServerResponse> findFriends(ServerRequest serverRequest) {
        int page = Integer.parseInt(serverRequest.pathVariable("page"));
        int size = Integer.parseInt(serverRequest.pathVariable("size"));

        LOG.info("findFriends from pageNumber {} and size {}", page, size);
        return getLoggedInUserId().map(userId -> friendshipService.getFriendships(userId))
                .flatMap(s ->  ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON).bodyValue(Map.of("message", s)))
                .onErrorResume(throwable -> {
                    LOG.debug("exception occurred in cancel Friendship", throwable);
                    LOG.error("cancelFriendship failed {}", throwable.getMessage());
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "cancelFriendship failed with error: " + throwable.getMessage()));
                });
    }


}
