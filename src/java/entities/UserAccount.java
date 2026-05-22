package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * This UserAccount class is an entity class for storing registered user
 * details.
 *
 * I created this class because the assignment requires user registration,
 * login, email verification, and account recovery. This class only stores the
 * account data in the database. The actual login and recovery logic will be
 * handled in the business layer.
 */
@Entity
@Table(name = "USER_ACCOUNT")
@NamedQueries({
    // This query is used to get all registered users from the database.
    @NamedQuery(name = "UserAccount.findAll", query = "SELECT u FROM UserAccount u"),

    // This query is used to find a user by user id.
    @NamedQuery(name = "UserAccount.findById", query = "SELECT u FROM UserAccount u WHERE u.userId = :userId"),

    // This query is used during login to find a user by username.
    @NamedQuery(name = "UserAccount.findByUsername", query = "SELECT u FROM UserAccount u WHERE u.username = :username"),

    // This query is used during registration and account recovery.
    @NamedQuery(name = "UserAccount.findByEmail", query = "SELECT u FROM UserAccount u WHERE u.email = :email")
})
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    // This is the primary key for the USER_ACCOUNT table.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    // First name of the registered user.
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    // Last name of the registered user.
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    // Username must be unique because it is used for login.
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be less than 100 characters")
    @Column(name = "USERNAME", nullable = false, unique = true, length = 100)
    private String username;

    // Email must be unique because it is used for verification and recovery.
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 150, message = "Email must be less than 150 characters")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    /*
     * The password should not be stored as plain text.
     * The business layer should hash the password before saving it here.
     */
    @NotBlank(message = "Password hash is required")
    @Size(max = 512, message = "Password hash must be less than 512 characters")
    @Column(name = "PASSWORD_HASH", nullable = false, length = 512)
    private String passwordHash;

    /*
     * This stores the email verification code during registration.
     * After successful verification, the business layer can clear or update it.
     */
    @Size(max = 20, message = "Verification code must be less than 20 characters")
    @Column(name = "VERIFICATION_CODE", length = 20)
    private String verificationCode;

    // This shows whether the user has completed email verification.
    @Column(name = "VERIFIED", nullable = false)
    private Boolean verified = false;

    /*
     * This stores the recovery code when the user forgets username or password.
     * The user must enter this code to reset the password.
     */
    @Size(max = 20, message = "Recovery code must be less than 20 characters")
    @Column(name = "RECOVERY_CODE", length = 20)
    private String recoveryCode;

    // This stores when the user account was created.
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE", nullable = false)
    private Date createdDate;

    /**
     * Empty constructor is required by JPA.
     */
    public UserAccount() {
        this.createdDate = new Date();
        this.verified = false;
    }

    /**
     * This constructor is used to create a new user account.
     *
     * @param firstName user first name
     * @param lastName user last name
     * @param username username for login
     * @param email user email address
     * @param passwordHash hashed password
     * @param verificationCode email verification code
     */
    public UserAccount(String firstName, String lastName, String username,
            String email, String passwordHash, String verificationCode) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verificationCode = verificationCode;
        this.verified = false;
        this.createdDate = new Date();
    }

    /**
     * Returns the user's full name for display.
     *
     * @return first name followed by last name
     */
    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }

    /**
     * Checks whether the user account has been verified.
     *
     * @return true if the account is verified
     */
    public boolean isAccountVerified() {
        return Boolean.TRUE.equals(verified);
    }

    /**
     * @return the user id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the user id to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the user first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the user first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the user last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the user last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the user email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the user email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordHash the hashed password to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return the verification code
     */
    public String getVerificationCode() {
        return verificationCode;
    }

    /**
     * @param verificationCode the verification code to set
     */
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    /**
     * @return true if the user account is verified
     */
    public Boolean getVerified() {
        return verified;
    }

    /**
     * @param verified the verified status to set
     */
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    /**
     * @return the recovery code
     */
    public String getRecoveryCode() {
        return recoveryCode;
    }

    /**
     * @param recoveryCode the recovery code to set
     */
    public void setRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    /**
     * @return the account created date
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate the account created date to set
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
