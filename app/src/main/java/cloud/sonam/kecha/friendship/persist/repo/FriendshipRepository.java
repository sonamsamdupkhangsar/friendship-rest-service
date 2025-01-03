package cloud.sonam.kecha.friendship.persist.repo;


import cloud.sonam.kecha.friendship.persist.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends ReactiveCrudRepository<Friendship, UUID> {
    Mono<Boolean>   existsByUserIdAndFriendId(UUID userID, UUID friendId);
    Flux<Friendship> findByUserIdAndFriendId(UUID userID, UUID friendId);
    List<Friendship> findByRequestAcceptedIsFalseAndUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("select fs from Friendship fs where (fs.userId=:userId or fs.friendId =:userId) and fs.requestAccepted=true and" +
            " fs.responseSentDate is not null order by fs.responseSentDate desc")
    Mono<Page<Friendship>> findAcceptedFriendsForUser(@Param("userId")UUID userId, Pageable pageable);

    /**
     * retrieve friendship rows for user where it's the user that requested or was requested by another user
     * and it has not been rejected
     * @param userId
     * @return flux of friendships
     * @return
     */
    @Query("select fs from Friendship fs where (fs.userId=:userId or fs.friendId =:userId) and fs.requestAccepted==true and responseSentDate!=null  " +
            "  order by fs.requestSentDate desc")
    Flux<Friendship> findValidFriendshipForUser(@Param("userId")UUID userId);
    Mono<Page<Friendship>> findByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(UUID userId, UUID friendId, Pageable pageable);

    Mono<Void> deleteByUserId(UUID userId);
    Mono<Void> deleteByFriendId(UUID friendId);
}