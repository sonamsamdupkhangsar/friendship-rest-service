package cloud.sonam.kecha.friendship.util;

import cloud.sonam.kecha.friendship.model.KechaPage;
import cloud.sonam.kecha.friendship.model.SeUserFriend;
import cloud.sonam.kecha.friendship.model.User;
import cloud.sonam.kecha.friendship.persist.entity.Friendship;
import cloud.sonam.kecha.friendship.persist.repo.FriendshipRepository;
import cloud.sonam.kecha.friendship.webclient.UserWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserFriendBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(UserFriendBuilder.class);

    private final UserWebClient userWebClient;

    private final FriendshipRepository friendshipRepository;

    public UserFriendBuilder(FriendshipRepository friendshipRepository, UserWebClient userWebClient) {
        this.friendshipRepository = friendshipRepository;
        this.userWebClient = userWebClient;
    }

    public Mono<SeUserFriend> buildUserFriendByFriendship(UUID loggedInUserId, Friendship friendship) {
        SeUserFriend seUserFriend = new SeUserFriend();

        seUserFriend.setFriendId(friendship.getFriendId());
        seUserFriend.setUserId(friendship.getUserId());

        Mono<User> userMono = Mono.empty();

        //store in userId of the other user, not the logged-in user
        if(friendship.getUserId().equals(loggedInUserId)) {
            userMono = userWebClient.findById(friendship.getFriendId());
        }
        else {
            userMono = userWebClient.findById(friendship.getUserId());
        }

        return userMono.flatMap(user -> {
                seUserFriend.setFullName(user.getFullName());
                seUserFriend.setProfilePhoto(user.getProfileThumbailFileKey());
                return Mono.just(seUserFriend);
            }).doOnNext(seUserFriend1 -> {
                if(friendship.getRequestAccepted() && friendship.getResponseSentDate() != null) {
                    seUserFriend.setFriend(true);
                    seUserFriend.setFriendshipId(friendship.getId());

                }
                else if(friendship.getRequestSentDate() != null) {
                    seUserFriend.setFriendshipId(friendship.getId());
                }
        }).thenReturn(seUserFriend);
    }

    public Mono<SeUserFriend> createSeUserFriendOnRequest(User friend, Friendship friendship) {
        SeUserFriend seUserFriend = new SeUserFriend();

        seUserFriend.setUserId(friendship.getUserId());
        seUserFriend.setFullName(friend.getFullName());
        setFriendship(friendship, seUserFriend);
        return Mono.just(seUserFriend);
    }

    public static Mono<SeUserFriend> getUserFriend(User user, Friendship friendship) {

        SeUserFriend seUserFriend = new SeUserFriend();

        seUserFriend.setUserId(user.getId());
        seUserFriend.setFullName(user.getFullName());

        setFriendship(friendship, seUserFriend);

        return Mono.just(seUserFriend);
    }

    public static void setFriendship(Friendship friendship, SeUserFriend seUserFriend) {
        seUserFriend.setFriendId(friendship.getFriendId());

        if(friendship.getRequestAccepted() && friendship.getResponseSentDate() != null) {
            seUserFriend.setFriend(true);
            seUserFriend.setFriendshipId(friendship.getId());

        }
        else if(friendship.getRequestSentDate() != null) {
            seUserFriend.setFriendshipId(friendship.getId());
        }
    }
}