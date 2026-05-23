package managedbeans;

import ejb.UserAccountEJB;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * EmailVerificationBean is the backing bean for the email.xhtml page.
 *
 * This bean handles Step 1 of the two-step registration process.
 * The user enters their email address and clicks "Create Verification Code".
 * The bean calls UserAccountEJB to generate a code and email it via FakeSMTP.
 * The generated code and email are stored in session scope so that the
 * register.xhtml page (step 2) can validate the code the user enters.
 *
 * Session scope is used here so that the verification code persists between
 * the email.xhtml page and the register.xhtml page navigation.
 */
@Named("emailVerificationBean")
@SessionScoped
public class EmailVerificationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserAccountEJB userAccountEJB;

    // The email address entered on the email.xhtml page.
    private String email;

    // The generated verification code stored temporarily in session.
    private String generatedCode;

    /**
     * Action method triggered when the user clicks "Create Verification Code".
     * Sends the code to the user's email via FakeSMTP and navigates to register.
     *
     * @return navigation outcome: "register" on success, null to stay
     */
    public String sendVerificationCode() {
        if (email == null || email.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter your email address.", null));
            return null;
        }

        generatedCode = userAccountEJB.initiateRegistration(email.trim());

        if (generatedCode == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not send verification email. The email may already be registered "
                            + "or the mail server is unavailable.", null));
            return null;
        }

        // Navigate to the registration form page.
        return "register?faces-redirect=true";
    }

    // Getters and setters.
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGeneratedCode() { return generatedCode; }
    public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }
}
