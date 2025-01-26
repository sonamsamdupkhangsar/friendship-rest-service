package cloud.sonam.kecha.friendship.impl;

import cloud.sonam.kecha.friendship.FriendshipException;


import cloud.sonam.kecha.friendship.persist.entity.Friendship;
import cloud.sonam.kecha.friendship.persist.repo.FriendshipRepository;
import cloud.sonam.kecha.friendship.util.UserFriendBuilder;
import me.sonam.webclients.friendship.FriendNotification;
import me.sonam.webclients.friendship.SeUserFriend;
import me.sonam.webclients.notification.NotificationWebClient;
import me.sonam.webclients.user.User;
import me.sonam.webclients.user.UserWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private static final Logger LOG = LoggerFactory.getLogger(FriendshipService.class);

    private final UserWebClient userWebClient;
    private final FriendshipRepository friendshipRepository;
    private final NotificationWebClient notificationWebClient;
    private final UserFriendBuilder userFriendBuilder;

    public FriendshipService(UserWebClient userWebClient, FriendshipRepository friendshipRepository,
                             NotificationWebClient notificationWebClient, UserFriendBuilder userFriendBuilder) {
        this.userWebClient = userWebClient;
        this.friendshipRepository = friendshipRepository;
        this.notificationWebClient = notificationWebClient;
        this.userFriendBuilder = userFriendBuilder;
    }

    public Mono<Boolean> isFriends(UUID friendId) {
        return getLoggedInUserId().flatMap(userId -> {
            userWebClient.findById(friendId);
//            return friendshipService.isFriends(userId, friendId);

            Mono<Boolean> booleanMono = friendshipRepository.existsByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(userId, friendId);

            return booleanMono.flatMap(isFriends -> {
                if (isFriends) {
                    return Mono.just(true);
                }
                else {
                    return friendshipRepository.existsByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(friendId, userId);
                }
            });

        });

      /*  Mono<Boolean> booleanMono = friendshipRepository.existsByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(userId, friendId);

        return booleanMono.flatMap(isFriends -> {
            if (isFriends) {
                return Mono.just(true);
            }
            else {
                return friendshipRepository.existsByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(friendId, userId);
            }
        });*/
    }


    public Flux<SeUserFriend> getFriendships(UUID userId) {
        LOG.info("find friendships for userId");

        return friendshipRepository.findAcceptedFriendsForUser(userId)
                .flatMap(friendship -> {
                    LOG.info("adding friendship {}", friendship);
                    return userFriendBuilder.buildUserFriendByFriendship(userId, friendship);
                });
    }

    public Mono<Friendship> confirmFriendship(UUID userId, UUID friendshipId) {
        Mono<Friendship> monoFriendship = friendshipRepository.findById(friendshipId);

        return monoFriendship.switchIfEmpty(
                Mono.error(new FriendshipException("failed to find friendship entity for id " + friendshipId)))
                .doOnNext(friendship ->
                    LOG.debug("user '{}' accepting friendship request from friend '{}'", friendship.getUserId(),
                            friendship.getFriendId()))
                .filter(friendship -> userId.equals(friendship.getFriendId()))
                .switchIfEmpty(Mono.error(new FriendshipException("only friend can confirm the friendship")))
                .flatMap(friendship -> {
                    LOG.debug("accepting friendship request from user w/id {} to friend w/id {} sent on {}",
                                friendship.getUserId(),
                                friendship.getFriendId(),
                                friendship.getRequestSentDate());
                        friendship.setResponseSentDate(LocalDateTime.now());
                        friendship.setRequestAccepted(true);

                        LOG.debug("saving friendship entity");
                        return friendshipRepository.save(friendship);
                });
    }

    public Mono<UUID> getLoggedInUserId() {
        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            org.springframework.security.core.Authentication authentication = securityContext.getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdString = jwt.getClaim("userId");
            LOG.debug("claims: {}, jwt: {}, security context userId: {}", jwt.getClaims(), jwt,
                    userIdString);

            UUID userId = UUID.fromString(userIdString);

            return Mono.just(userId);
        });
    }

    public Mono<SeUserFriend> createFriendship(UUID friendId) {
        return getLoggedInUserId().flatMap(userId -> requestFriendship(userId, friendId)
                        .zipWith(Mono.just(userId))
                ).flatMap(objects -> {
                    LOG.info("get user by id {}, frienship: {}", objects.getT2(), objects.getT1());
                    return           userWebClient.findById(objects.getT2()).zipWith(Mono.just(objects.getT1()));
                })//userId, Friendship
                .flatMap(objects -> UserFriendBuilder.getUserFriend(objects.getT1(), objects.getT2())
                        .zipWith(Mono.just(objects.getT1())).zipWith(Mono.just(objects.getT2())))//SeUserFriend, User, Friendship objects
                .doOnNext(objects -> LOG.info("send friend request notification, friendship {}, seUserFriend: {}, user: {}",
                        objects.getT2(),
                        objects.getT1().getT1(), objects.getT1().getT2()))
                .flatMap(objects ->
                        notificationWebClient.sendFriendNotification(objects.getT1().getT2(), objects.getT1().getT1(),
                                FriendNotification.Event.REQUEST).thenReturn(objects)
                )
                .flatMap(objects -> {
                    LOG.info("after notification, get friend by id: {}", objects.getT2().getFriendId());
                    return  userWebClient.findById(objects.getT2().getFriendId()).zipWith(Mono.just(objects.getT2()));
                })
                .flatMap(objects -> {
                    LOG.info("get seUserFriend on request for userId: {} friendship: {}", objects.getT1(), objects.getT2());
                    return userFriendBuilder.createSeUserFriendOnRequest(objects.getT1(), objects.getT2());
                });
    }

    public Mono<SeUserFriend> acceptFriendship(UUID friendshipId) {
        LOG.info("get userFriend object from confirmFriendship");

        return getLoggedInUserId().flatMap(userId -> confirmFriendship(userId, friendshipId).zipWith(Mono.just(userId))) //Friendship, loggedInUserId
                .flatMap(friendshipLoggedInUserIdTuple -> {
                    return userWebClient.findById(friendshipLoggedInUserIdTuple.getT2())
                            .flatMap(loggedInUser -> UserFriendBuilder.getUserFriend(loggedInUser, friendshipLoggedInUserIdTuple.getT1()))
                            .flatMap(seUserFriend -> userWebClient.findById(friendshipLoggedInUserIdTuple.getT1().getUserId()).zipWith(Mono.just(seUserFriend)))
                            .flatMap(userSeUserFriendTuple2 ->
                                    notificationWebClient.sendFriendNotification(userSeUserFriendTuple2.getT1(),
                                                    userSeUserFriendTuple2.getT2(), FriendNotification.Event.CONFIRM)
                                            .flatMap(user -> UserFriendBuilder.getUserFriend(userSeUserFriendTuple2.getT1(), friendshipLoggedInUserIdTuple.getT1())));
                        });
    }

    public Mono<Friendship> requestFriendship(UUID userId, UUID friendId) {
        LOG.info("requesting friendship from user {} to friendId {}", userId, friendId);


        return userWebClient.findById(userId)
                .switchIfEmpty(Mono.error(new FriendshipException("failed to find user with id " + userId)))
                .flatMap(user -> Mono.just(user).zipWith(userWebClient.findById(friendId).switchIfEmpty(
                       Mono.error(new FriendshipException("failed to find friend with id "+ friendId)))))
                .flatMap(objects -> {
                    User user = objects.getT1();
                    User friend = objects.getT2();

                    LOG.debug("confirming friendship between user '{}' and friend '{}'",
                            user, friend);

                    LOG.debug("delete previous friendship rows where friendship has been declined");
                    return friendshipRepository
                            .deleteByRequestAcceptedIsFalseAndUserIdAndFriendId(user.getId(), friend.getId())
                            .doOnNext(integer -> LOG.info("deleted {} friendship requests that were declined previously where userId {} and friendId {}",
                                    integer, user.getId(), friend.getId()))
                            .thenReturn(objects);
            }).flatMap(objects -> {
                    User user = objects.getT1();
                    User friend = objects.getT2();


                    return friendshipRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())
                            .doOnNext(aBoolean -> {
                                if (aBoolean) {
                                    LOG.debug("there is already a friendship row between user {} and friend {}",
                                            user, friend);
                                }
                                else {
                                    LOG.debug("no friendship from user to friend");
                                }
                            })
                            .filter(aBoolean -> !aBoolean)
                            .switchIfEmpty(friendshipRepository.existsByUserIdAndFriendId(friend.getId(), user.getId()))
                            .doOnNext(aBoolean -> {
                                if (aBoolean) {
                                    LOG.debug("there is already a friendship row between friend {} and user {}",
                                           friend, user);
                                }
                                else {
                                    LOG.debug("no friendship from friend to user");
                                }
                            })
                            .filter(aBoolean -> !aBoolean)
                            .flatMap(aBoolean -> {
                                if (!aBoolean) {
                                    Friendship friendship = new Friendship(LocalDateTime.now(), null, user.getId(), friend.getId(), false);

                                    LOG.debug("saving friendship entity saveFriendship(), {}", friendship.isNew());
                                    return friendshipRepository.save(friendship);
                                }
                                else {
                                    return Mono.empty();
                                }
                            });
                });
    }

    public Mono<Friendship> declineFriendship(UUID userId, UUID friendshipId) {
        Mono<User> optionalUser = userWebClient.findById(userId);
        Mono<Friendship> friendshipMono = friendshipRepository.findById(friendshipId);

        return friendshipMono.switchIfEmpty(Mono.error(new FriendshipException("failed to find friendship with id " + friendshipId)))
                        .filter(friendship -> userId.equals(friendship.getFriendId()))
                                .switchIfEmpty(Mono.error(
                                        new FriendshipException("only friend can decline friendship request")))
                                        .flatMap(friendship -> {
                                            LOG.debug("deny friendship request from user w/id {} sent on {}", friendship.getUserId(),
                                                    friendship.getRequestSentDate());
                                            LOG.info("setting requestAccepted to false to indicate friendship not accepted");
                                            friendship.setRequestAccepted(false);
                                            friendship.setResponseSentDate(LocalDateTime.now());

                                            LOG.debug("update friendship entity");
                                            return friendshipRepository.save(friendship);
                                        });
    }

    private void checkEntityExists(Object object, String errorMessageIfNull) {
        if(object == null) {
            LOG.error(errorMessageIfNull);
            throw new FriendshipException(errorMessageIfNull);
        }
    }

    public Mono<String> delete(UUID friendshipId) {
        LOG.debug("delete friendship by id {}", friendshipId);

        return friendshipRepository.deleteById(friendshipId).flatMap(unused -> {
            LOG.info("got deleted");
            return Mono.just(unused);
        }).thenReturn("friendship deleted by id");

    }
}