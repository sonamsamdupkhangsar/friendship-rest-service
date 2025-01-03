package cloud.sonam.kecha.friendship.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class SeUser implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String sex;

    private String errorMessage;
    private String authenticationId;
    private String password;
    private String profilePhotoMd5;
    private String profilePhoto;


    /**
     *
     */
    public SeUser() {
    }

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param authenticationId
     */
    public SeUser(UUID id, String firstName, String lastName, String sex, String email,
                  String authenticationId, String password) {
        super();
        this.userId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.authenticationId = authenticationId;
        this.password = password;
        this.sex = sex;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public String getProfilePhotoMd5() {
        return profilePhotoMd5;
    }

    public void setProfilePhotoMd5(String profilePhotoMd5) {
        this.profilePhotoMd5 = profilePhotoMd5;
    }



    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }


    @Override
    public String toString() {
        return "SeUser [userId=" + userId + ", firstName=" + firstName
                + ", lastName=" + lastName + ", sex=" + sex +", dob=" + dob + ", email="
                + email + ", authenticationId=" + authenticationId
                + ", password=" + password + ", profilePhotoMd5=" + profilePhotoMd5
                + ", profilePhoto=" + profilePhoto + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeUser seUser = (SeUser) o;

        return userId != null ? userId.equals(seUser.userId) : seUser.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}