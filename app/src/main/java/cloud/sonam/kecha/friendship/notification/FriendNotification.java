/**
 * kecha-webmvc
 * Copyright Sonam Samdupkhangsar
 */
package cloud.sonam.kecha.friendship.notification;


import cloud.sonam.kecha.friendship.model.SeUserFriend;

import java.io.Serializable;

/**
 * @author sonamwangyalsamdupkhangsar
 *
 */
public class FriendNotification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Event event;
	private SeUserFriend seUserFriend;

	/*
	 * confirm : use confirm to indicate user has accepted friendship
	 * request: use request to indicate a user has requested friendship
	 * delete: user is not in a friendship with other user
	 * decline: don't send any event to friend
	 */
	public enum Event {
		CANCEL, CONFIRM, REQUEST, DELETE, DECLINE
	}
	
	/**
	 * 
	 */
	public FriendNotification() {
		super();
	}
	
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}

	public SeUserFriend getSeUserFriend() {
		return seUserFriend;
	}

	public void setSeUserFriend(SeUserFriend seUserFriend) {
		this.seUserFriend = seUserFriend;
	}

	@Override
	public String toString() {
		return "FriendNotification [event=" + event + ", seUserFriend=" + seUserFriend + "]";
	}

}
