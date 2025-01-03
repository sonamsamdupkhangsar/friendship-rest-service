package cloud.sonam.kecha.friendship.model;

import java.util.UUID;

/*
getId());
            seUserFriend.setFullName(user.getFullName());
            seUserFriend.setProfilePhoto(user.getProfilePhoto());
 */
public class User {
    private UUID id;
    private String fullName;
    private String profileThumbailFileKey;
    private String firstName;

    public User(UUID id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    private String lastName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileThumbailFileKey() {
        return profileThumbailFileKey;
    }

    public void setProfileThumbailFileKey(String profileThumbailFileKey) {
        this.profileThumbailFileKey = profileThumbailFileKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
