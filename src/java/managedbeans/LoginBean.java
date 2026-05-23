package managedbeans;

import ejb.UserAccountEJB;
import entities.UserAccount;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

/**
 * LoginBean is the backing bean for the login.xhtml page.
 *
 * I created this backing bean to handle user authentication for the
 * login form. When the user clicks Login, this bean calls UserAccountEJB
 * to verify the credentials. On success, the UserAccount is stored in
 * the HTTP session under "loggedInUser" so AuthFilter can check it.
 *
 * The "Forget your user name and/or password" link navigates to the
 * email recovery page as shown in the demo document.
 */
@Named("loginBean")
@RequestScoped
public class LoginBean {

    @EJB
    private UserAccountEJB userAccountEJB;

    // Fields bound to the login form inputs.
    private String username;
    private String password;

    /**
     * Action method triggered when the user clicks the Login button.
     * Validates credentials via UserAccountEJB and stores the user in session.
     *
     * @return navigation outcome: "default" on success, null to stay on login page
     */
    public String login() {
        UserAccount account = userAccountEJB.login(username, password);

        if (account != null) {
            // Store the logged-in user in the HTTP session.
            FacesContext ctx = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) ctx.getExternalContext().getSession(true);
            session.setAttribute("loggedInUser", account);
            return "default?faces-redirect=true";
        }

        // Invalid credentials - show error message on the login page.
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Invalid username or password. Please try again.", null));
        return null;
    }

    /**
     * Action method for the Logout link on the main page.
     * Invalidates the session and redirects to the login page.
     *
     * @return navigation outcome back to login page
     */
    public String logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters and setters for form binding.
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
