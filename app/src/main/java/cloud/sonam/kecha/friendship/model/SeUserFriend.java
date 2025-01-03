package cloud.sonam.kecha.friendship.model;

import java.util.UUID;

/**
 * @author sonamwangyalsamdupkhangsar
 *
 */
public class SeUserFriend {
    private UUID userId;
    private UUID friendId;
    private String fullName;
    private boolean friend;
    private UUID friendshipId;
    private String profilePhoto;

    public SeUserFriend() {
    }

    public SeUserFriend(UUID userId, UUID friendId, String fullName, boolean friend, UUID friendshipId) {
        this.userId = userId;
        this.friendId = friendId;
        this.fullName = fullName;
        this.friend = friend;
        this.friendshipId = friendshipId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public UUID getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(UUID friendshipId) {
        this.friendshipId = friendshipId;
    }

    public UUID getFriendId() {
        return friendId;
    }

    public void setFriendId(UUID friendId) {
        this.friendId = friendId;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    @Override
    public String toString() {
        return "SeUserFriend [userId=" + userId + ", friendId=" + friendId + ", fullName=" + fullName + ", friend="
                + friend + ", friendshipId=" + friendshipId + ", profilePhoto: " + profilePhoto + "]";
    }

}