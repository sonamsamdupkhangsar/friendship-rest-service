package cloud.sonam.kecha.friendship.persist.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

public class Friendship implements Persistable<UUID> {

    @Id
    private UUID id;

    private LocalDateTime requestSentDate;

    private LocalDateTime responseSentDate;

    private Boolean requestAccepted;

    private UUID userId;

    private UUID friendId;

    @Transient
    private boolean newFriendship;

    public Friendship() {
        super();
    }


    public Friendship(LocalDateTime requestSentDate, LocalDateTime responseSentDate,
                      UUID userId, UUID friendId, Boolean requestAccepted) {
        this.id = UUID.randomUUID();
        this.requestSentDate = requestSentDate;
        this.responseSentDate = responseSentDate;
        this.userId = userId;
        this.friendId = friendId;
        this.requestAccepted = requestAccepted;
        this.newFriendship = true;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }


    public UUID getFriendId() {
        return friendId;
    }


    public void setFriendId(UUID friendId) {
        this.friendId = friendId;
    }


    public LocalDateTime getRequestSentDate() {
        return requestSentDate;
    }


    public void setRequestSentDate(LocalDateTime requestSentDate) {
        this.requestSentDate = requestSentDate;
    }


    public LocalDateTime getResponseSentDate() {
        return responseSentDate;
    }


    public void setResponseSentDate(LocalDateTime responseSentDate) {
        this.responseSentDate = responseSentDate;
    }

    public Boolean getRequestAccepted() {
        return requestAccepted;
    }


    public void setRequestAccepted(Boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public boolean isNew() {
        return this.newFriendship;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((friendId == null) ? 0 : friendId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime
                * result
                + ((responseSentDate == null) ? 0 : responseSentDate
                .hashCode());
        result = prime * result
                + ((requestSentDate == null) ? 0 : requestSentDate.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Friendship other = (Friendship) obj;
        if (friendId == null) {
            if (other.friendId != null)
                return false;
        } else if (!friendId.equals(other.friendId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (responseSentDate == null) {
            if (other.responseSentDate != null)
                return false;
        } else if (!responseSentDate.equals(other.responseSentDate))
            return false;
        if (requestSentDate == null) {
            if (other.requestSentDate != null)
                return false;
        } else if (!requestSentDate.equals(other.requestSentDate))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Friendship [id=" + id + ", requestSentDate=" + requestSentDate
                + ", responseSentDate=" + responseSentDate
                + ", requestAccepted=" + requestAccepted + ", userId=" + userId
                + ", friendId=" + friendId + "]";
    }

}