package managedbeans;

import ejb.UserAccountEJB;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * RegisterBean is the backing bean for the register.xhtml page.
 *
 * This bean handles Step 2 of the two-step registration process.
 * The user enters the verification code retrieved from their email,
 * along with their first name, last name, username, and password.
 * The bean calls UserAccountEJB.completeRegistration() which validates
 * the code, hashes the password (SHA-512), and persists the new account.
 *
 * It relies on EmailVerificationBean (session-scoped) to get the email
 * address and the expected verification code that were set in step 1.
 */
@Named("registerBean")
@RequestScoped
public class RegisterBean {

    @EJB
    private UserAccountEJB userAccountEJB;

    // Form fields bound to register.xhtml inputs.
    private String verificationCode;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String passwordVerify;

    /**
     * Action method triggered when the user clicks "Create" on the register page.
     * Validates the code and creates the user account.
     *
     * The EmailVerificationBean from the session is accessed to retrieve the
     * expected code and email address stored during the previous step.
     *
     * @return "login" on success to redirect to the login page, null to stay
     */
    public String register() {
        // Retrieve the email verification state from the session-scoped bean.
        FacesContext ctx = FacesContext.getCurrentInstance();
        EmailVerificationBean evBean = ctx.getApplication()
                .evaluateExpressionGet(ctx, "#{emailVerificationBean}",
                        EmailVerificationBean.class);

        if (evBean == null || evBean.getGeneratedCode() == null || evBean.getEmail() == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Session expired. Please start registration again.", null));
            return null;
        }

        // Validate that the two password fields match before calling the EJB.
        if (!password.equals(passwordVerify)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Passwords do not match. Please re-enter.", null));
            return null;
        }

        // Delegate to the business layer.
        String error = userAccountEJB.completeRegistration(
                verificationCode,
                evBean.getGeneratedCode(),
                firstName, lastName, username,
                evBean.getEmail(), password);

        if (error != null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        // Clear the session bean after successful registration.
        evBean.setGeneratedCode(null);
        evBean.setEmail(null);

        // Navigate to login page after successful registration.
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters and setters.
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordVerify() { return passwordVerify; }
    public void setPasswordVerify(String passwordVerify) { this.passwordVerify = passwordVerify; }
}
