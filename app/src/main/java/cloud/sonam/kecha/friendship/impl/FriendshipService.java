package cloud.sonam.kecha.friendship.impl;

import cloud.sonam.kecha.friendship.FriendshipException;
import cloud.sonam.kecha.friendship.model.SeUserFriend;
import cloud.sonam.kecha.friendship.model.User;
import cloud.sonam.kecha.friendship.persist.entity.Friendship;
import cloud.sonam.kecha.friendship.persist.repo.FriendshipRepository;
import cloud.sonam.kecha.friendship.util.UserFriendBuilder;
import cloud.sonam.kecha.friendship.webclient.UserWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private static final Logger LOG = LoggerFactory.getLogger(FriendshipService.class);

    @Autowired
    private UserFriendBuilder userFriendBuilder;

    private final UserWebClient userWebClient;
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(UserWebClient userWebClient, FriendshipRepository friendshipRepository) {
        this.userWebClient = userWebClient;
        this.friendshipRepository = friendshipRepository;
    }

    public Mono<Boolean> isFriends(UUID userId, UUID friendId) {
        Pageable pageable = PageRequest.of(0, 1);
        Mono<Page<Friendship>> pageMono = friendshipRepository.findByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(userId, friendId, pageable);

        return pageMono.flatMap(friendships -> {
            if (!friendships.isEmpty()) {
                return Mono.just(true);
            }
            else {
                Mono<Page<Friendship>> pageMono1 = friendshipRepository.findByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(friendId, userId, pageable);
                return pageMono1.flatMap(friendships1 -> {
                    if (!friendships.isEmpty()) {
                        return Mono.just(true);
                    }
                    else {
                        return Mono.just(false);
                    }
                });
            }
        });
    }

    public Flux<SeUserFriend> getFriendships(UUID userId) {
        LOG.info("find friendships for userId: {}", userId);

        Flux<Friendship> friendshipFlux = friendshipRepository.findValidFriendshipForUser(userId);

        return friendshipFlux.flatMap(friendship -> {
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

    public Mono<Friendship> requestFriendship(UUID userId, UUID friendId) {
        LOG.info("requesting friendship from user {} to friendId {}", userId, friendId);


        return userWebClient.findById(userId)
                .switchIfEmpty(Mono.error(new FriendshipException("failed to find user with id " + userId)))
                .zipWith(userWebClient.findById(friendId).switchIfEmpty(
                        Mono.error(new FriendshipException("failed to find friend with id " + friendId))))
                .flatMap(objects -> {
                    User user = objects.getT1();
                    User friend = objects.getT2();

                    LOG.debug("confirming friendship between user '{}' and friend '{}'",
                            user, friend);

                    LOG.debug("delete previous friendship rows where friendship has been declined");
                    List<Friendship> list = friendshipRepository
                            .findByRequestAcceptedIsFalseAndUserIdAndFriendId(user.getId(), friend.getId());

                    if (!list.isEmpty()) {
                        LOG.debug("deleting '{}' previous friendship requests that were declined", list.size());
                        for (Friendship fs : list) {
                            return friendshipRepository.delete(fs).thenReturn(objects);
                        }
                    }
                    return Mono.just(objects);
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
                                    return saveFriendship(user, friend);
                                }
                                else {
                                    return Mono.empty();
                                }
                            });
                });
    }


    private Mono<Friendship> saveFriendship(User user, User friend) {
        Friendship friendship = new Friendship();

        LOG.trace("set user in friendship");
        friendship.setUserId(user.getId());

        LOG.trace("user friend in friendship");
        friendship.setFriendId(friend.getId());

        LOG.trace("set current time as requestSentDate");
        friendship.setRequestSentDate(LocalDateTime.now());
        friendship.setRequestAccepted(false);

        LOG.debug("saving friendship entity");
        return friendshipRepository.save(friendship);
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

        return friendshipRepository.deleteById(friendshipId).thenReturn("friendship deleted by id");
    }
}