package ejb;

import entities.UserAccount;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserAccountEJB handles all business logic related to user accounts.
 *
 * I implemented this EJB to separate the security and account management
 * logic from the presentation layer. The backing beans in the JSF layer call
 * this EJB for every user-related operation: registration, email verification,
 * login, and account recovery.
 *
 * Responsibilities of this EJB:
 * 1. Generating random verification and recovery codes
 * 2. Hashing passwords with SHA-512 (512-bit) as required by the assignment
 * 3. Persisting new UserAccount entities via JPA
 * 4. Validating login credentials against stored hashed passwords
 * 5. Updating passwords during account recovery
 * 6. Delegating email sending to the EmailService EJB
 */
@Stateless
public class UserAccountEJB {

    private static final Logger LOGGER = Logger.getLogger(UserAccountEJB.class.getName());

    // EntityManager is injected by the container using the persistence unit
    // defined in persistence.xml. All JPQL operations use this manager.
    @PersistenceContext(unitName = "ElectronicsStorePU")
    private EntityManager em;

    // Injecting EmailService EJB to send verification and recovery emails.
    @EJB
    private EmailService emailService;

    // Length of generated random codes for verification and recovery.
    private static final int CODE_LENGTH = 20;

    /**
     * Step 1 of registration: generates a verification code and sends it
     * to the provided email address. The code is temporarily stored in a
     * pending UserAccount that is NOT yet fully registered (verified = false).
     *
     * If the email already exists in the system, returns an error message.
     *
     * @param email the email address entered by the user on the verify page
     * @return the generated verification code on success, or null on failure
     */
    public String initiateRegistration(String email) {
        // Check if the email is already registered.
        if (isEmailTaken(email)) {
            LOGGER.log(Level.WARNING, "Registration attempted for existing email: {0}", email);
            return null;
        }

        // Generate a random code and send it to the user's email.
        String code = generateRandomCode();
        boolean sent = emailService.sendVerificationCode(email, code);

        if (!sent) {
            LOGGER.log(Level.SEVERE, "Could not send verification email to: {0}", email);
            return null;
        }

        return code;
    }

    /**
     * Step 2 of registration: creates and persists a new UserAccount after
     * the user provides the correct verification code along with their details.
     *
     * The password is hashed with SHA-512 before being stored. The account
     * is marked as verified = true since the user confirmed the email code.
     *
     * @param verificationCode the code the user retrieved from their email
     * @param expectedCode     the code generated in step 1 (stored in session)
     * @param firstName        user first name
     * @param lastName         user last name
     * @param username         chosen username for login
     * @param email            the verified email address
     * @param password         plain-text password entered by the user
     * @return null on success, or an error message string on failure
     */
    public String completeRegistration(String verificationCode, String expectedCode,
            String firstName, String lastName, String username,
            String email, String password) {

        // Validate the verification code matches what was emailed.
        if (!verificationCode.equals(expectedCode)) {
            return "Invalid verification code. Please check your email.";
        }

        // Check if username is already taken.
        if (isUsernameTaken(username)) {
            return "Username already exists. Please choose a different username.";
        }

        // Hash the password before storing it.
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return "System error during registration. Please try again.";
        }

        // Create and persist the new user account.
        UserAccount account = new UserAccount(firstName, lastName, username,
                email, hashedPassword, verificationCode);
        account.setVerified(true);

        try {
            em.persist(account);
            LOGGER.log(Level.INFO, "New user registered: {0}", username);
            return null; // null = success
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to persist new user account", ex);
            return "Registration failed. The username or email may already be in use.";
        }
    }

    /**
     * Validates login credentials by looking up the username and comparing
     * the hashed password against what is stored in the database.
     *
     * @param username the username entered on the login page
     * @param password the plain-text password entered on the login page
     * @return the UserAccount if credentials are valid, or null if not
     */
    public UserAccount login(String username, String password) {
        try {
            UserAccount account = (UserAccount) em
                    .createNamedQuery("UserAccount.findByUsername")
                    .setParameter("username", username)
                    .getSingleResult();

            // Hash the entered password and compare with stored hash.
            String hashedInput = hashPassword(password);
            if (hashedInput != null && hashedInput.equals(account.getPasswordHash())) {
                LOGGER.log(Level.INFO, "User logged in: {0}", username);
                return account;
            }

            LOGGER.log(Level.WARNING, "Invalid password for user: {0}", username);
            return null;

        } catch (NoResultException ex) {
            LOGGER.log(Level.WARNING, "Login attempt for unknown username: {0}", username);
            return null;
        }
    }

    /**
     * Step 1 of account recovery: verifies the email exists in the system,
     * generates a recovery code, saves it on the account, and emails it.
     *
     * @param email the registered email address entered on the recovery page
     * @return the UserAccount if found, or null if the email is not registered
     */
    public UserAccount initiateRecovery(String email) {
        try {
            UserAccount account = (UserAccount) em
                    .createNamedQuery("UserAccount.findByEmail")
                    .setParameter("email", email)
                    .getSingleResult();

            // Generate and save a recovery code on the account.
            String recoveryCode = generateRandomCode();
            account.setRecoveryCode(recoveryCode);
            em.merge(account);

            // Send the recovery code to the user's email.
            boolean sent = emailService.sendRecoveryCode(email, recoveryCode);
            if (!sent) {
                LOGGER.log(Level.SEVERE, "Could not send recovery email to: {0}", email);
                return null;
            }

            return account;

        } catch (NoResultException ex) {
            LOGGER.log(Level.WARNING, "Recovery attempted for unknown email: {0}", email);
            return null;
        }
    }

    /**
     * Step 2 of account recovery: validates the recovery code and resets
     * the user's password to the new value provided.
     *
     * @param userId          the id of the account being recovered
     * @param recoveryCode    the code the user retrieved from their email
     * @param newPassword     the new plain-text password chosen by the user
     * @param confirmPassword the confirmation entry (must match newPassword)
     * @return null on success, or an error message string on failure
     */
    public String completeRecovery(Long userId, String recoveryCode,
            String newPassword, String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            return "Passwords do not match. Please re-enter.";
        }

        try {
            UserAccount account = em.find(UserAccount.class, userId);
            if (account == null) {
                return "Account not found.";
            }

            // Validate the recovery code against what was stored.
            if (!recoveryCode.equals(account.getRecoveryCode())) {
                return "Invalid recovery code. Please check your email.";
            }

            // Hash the new password and update the account.
            String newHash = hashPassword(newPassword);
            if (newHash == null) {
                return "System error. Please try again.";
            }

            account.setPasswordHash(newHash);
            account.setRecoveryCode(null); // Clear the code after use.
            em.merge(account);

            LOGGER.log(Level.INFO, "Password reset for userId: {0}", userId);
            return null; // null = success

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to complete account recovery", ex);
            return "Recovery failed. Please try again.";
        }
    }

    /**
     * Retrieves all registered user accounts from the database.
     * Used for administrative queries.
     *
     * @return list of all UserAccount entities
     */
    @SuppressWarnings("unchecked")
    public List<UserAccount> findAllUsers() {
        return em.createNamedQuery("UserAccount.findAll").getResultList();
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------

    /**
     * Checks whether an email address is already registered in the system.
     *
     * @param email the email to check
     * @return true if the email is already in use
     */
    private boolean isEmailTaken(String email) {
        try {
            em.createNamedQuery("UserAccount.findByEmail")
                    .setParameter("email", email)
                    .getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    /**
     * Checks whether a username is already registered in the system.
     *
     * @param username the username to check
     * @return true if the username is already taken
     */
    private boolean isUsernameTaken(String username) {
        try {
            em.createNamedQuery("UserAccount.findByUsername")
                    .setParameter("username", username)
                    .getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    /**
     * Hashes a plain-text password using SHA-512 and returns a hex string.
     * The assignment screenshot shows a 512-bit hash stored in the database.
     *
     * @param password the plain-text password to hash
     * @return the SHA-512 hex hash string, or null on algorithm error
     */
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, "SHA-512 algorithm not found", ex);
            return null;
        }
    }

    /**
     * Generates a random alphanumeric code used for email verification and
     * account recovery. The code uses SecureRandom for cryptographic safety.
     *
     * @return a random code string of CODE_LENGTH characters
     */
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[15];
        random.nextBytes(bytes);
        // Base64 URL encoding gives ~20 characters without padding issues.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, CODE_LENGTH);
    }
}
