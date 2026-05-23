package managedbeans;

import ejb.UserAccountEJB;
import entities.UserAccount;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * RecoveryBean is the backing bean for emailRecovery.xhtml and recovery.xhtml.
 *
 * This bean handles the two-step account recovery process described in section 3
 * of the demo document:
 * Step 1 (emailRecovery.xhtml): User enters their registered email address.
 *         The system sends a recovery code to that email via FakeSMTP.
 * Step 2 (recovery.xhtml): User enters the recovery code from their email,
 *         and the system automatically fills in first name, last name, and
 *         username from the account. The user then sets a new password.
 *
 * Session scope is used so that the account details persist between the
 * two recovery pages.
 */
@Named("recoveryBean")
@SessionScoped
public class RecoveryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserAccountEJB userAccountEJB;

    // Step 1 field: the email entered on emailRecovery.xhtml.
    private String email;

    // The UserAccount found in step 1; auto-populates the recovery form.
    private UserAccount foundAccount;

    // Step 2 fields: entered on recovery.xhtml.
    private String recoveryCode;
    private String newPassword;
    private String newPasswordVerify;

    /**
     * Action method for Step 1: sends the recovery code to the user's email.
     * If the email is found, the account details (first name, last name,
     * username) are pre-filled on the recovery.xhtml page as shown in the demo.
     *
     * @return "recovery" on success, null to stay on emailRecovery page
     */
    public String sendRecoveryCode() {
        if (email == null || email.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter your registered email address.", null));
            return null;
        }

        foundAccount = userAccountEJB.initiateRecovery(email.trim());

        if (foundAccount == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Email address not found. Please enter your registered email.", null));
            return null;
        }

        return "recovery?faces-redirect=true";
    }

    /**
     * Action method for Step 2: validates the recovery code and resets the
     * password. After successful recovery the user is redirected to login.
     *
     * @return "/login" on success, null to stay on recovery page
     */
    public String resetPassword() {
        if (foundAccount == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Session expired. Please start the recovery process again.", null));
            return null;
        }

        String error = userAccountEJB.completeRecovery(
                foundAccount.getUserId(),
                recoveryCode,
                newPassword,
                newPasswordVerify);

        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        // Clear session data after successful recovery.
        foundAccount = null;
        email = null;

        return "/login.xhtml?faces-redirect=true";
    }

    // Getters and setters.
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserAccount getFoundAccount() { return foundAccount; }
    public void setFoundAccount(UserAccount foundAccount) { this.foundAccount = foundAccount; }

    public String getRecoveryCode() { return recoveryCode; }
    public void setRecoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getNewPasswordVerify() { return newPasswordVerify; }
    public void setNewPasswordVerify(String newPasswordVerify) { this.newPasswordVerify = newPasswordVerify; }
}
