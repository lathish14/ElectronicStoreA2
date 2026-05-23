package managedbeans;

import ejb.CustomerEJB;
import entities.Customer;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.List;

/**
 * CustomerBean is the backing bean for all Customer-related JSF pages.
 *
 * I created this backing bean to handle: - newCustomer.xhtml : creating a new
 * customer - listCustomers.xhtml : displaying all customers with order count -
 * searchCustomer.xhtml: searching customers by name - customerDetail.xhtml:
 * displaying a customer's details and orders
 *
 * All data access is delegated to CustomerEJB.
 */
@Named("customerBean")
@RequestScoped
public class CustomerBean {

    @EJB
    private CustomerEJB customerEJB;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;

    private String searchName;

    private Long customerId;
    private Customer selectedCustomer;

    private List<Customer> customers;
    private List<Customer> foundCustomers;

    private String message;

    /**
     * Creates a new customer.
     *
     * @return navigation outcome
     */
    public String createCustomer() {
        Customer customer = new Customer(firstName, lastName, email, phone, address);
        String error = customerEJB.createCustomer(customer);

        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        message = "Successfully created the customer: " + firstName + " " + lastName;
        return "listCustomers?faces-redirect=true";
    }

    /**
     * Returns all customers for the list page.
     *
     * @return list of customers
     */
    public List<Customer> getCustomers() {
        if (customers == null) {
            customers = customerEJB.findAllCustomers();
        }
        return customers;
    }

    /**
     * Counts the orders for one customer. This is used by listCustomers.xhtml.
     *
     * @param customer selected customer
     * @return number of orders for the customer
     */
    public Long getOrderCount(Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return 0L;
        }

        return customerEJB.countOrdersByCustomer(customer.getCustomerId());
    }

    /**
     * Searches customers by name.
     *
     * @return null to stay on the same page
     */
    public String searchCustomers() {
        if (searchName == null || searchName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter a name to search.", null));
            return null;
        }

        foundCustomers = customerEJB.searchByName(searchName.trim());
        return null;
    }

    /**
     * Loads a customer's details by ID.
     *
     * @return null
     */
    public String loadCustomer() {
        if (customerId != null) {
            selectedCustomer = customerEJB.findCustomerById(customerId);
        }
        return null;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    public void setSelectedCustomer(Customer selectedCustomer) {
        this.selectedCustomer = selectedCustomer;
    }

    public List<Customer> getFoundCustomers() {
        return foundCustomers;
    }

    public void setFoundCustomers(List<Customer> foundCustomers) {
        this.foundCustomers = foundCustomers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
