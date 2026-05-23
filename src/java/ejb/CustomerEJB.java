package ejb;

import entities.Customer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CustomerEJB is a stateless session bean that handles all business logic for
 * customer management.
 *
 * I implemented this EJB to separate customer CRUD operations from the JSF
 * presentation layer. The JSF backing beans call this EJB for creating
 * customers, listing all customers, searching by name, viewing customer
 * details, and counting customer orders.
 */
@Stateless
public class CustomerEJB {

    private static final Logger LOGGER = Logger.getLogger(CustomerEJB.class.getName());

    @PersistenceContext(unitName = "ElectronicsStorePU")
    private EntityManager em;

    /**
     * Persists a new Customer entity to the database.
     *
     * @param customer the Customer entity populated from the JSF form
     * @return null on success, or an error message string on failure
     */
    public String createCustomer(Customer customer) {
        try {
            em.persist(customer);
            LOGGER.log(Level.INFO, "Created customer: {0}", customer.getFullName());
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to create customer", ex);
            return "Failed to create customer. The email address may already be in use.";
        }
    }

    /**
     * Retrieves all customers from the database.
     *
     * @return list of all Customer entities
     */
    @SuppressWarnings("unchecked")
    public List<Customer> findAllCustomers() {
        return em.createNamedQuery("Customer.findAll").getResultList();
    }

    /**
     * Finds a single customer by primary key.
     *
     * @param customerId the primary key of the customer
     * @return the Customer entity with orders loaded, or null if not found
     */
    public Customer findCustomerById(Long customerId) {
        try {
            Customer customer = (Customer) em.createNamedQuery("Customer.findById")
                    .setParameter("customerId", customerId)
                    .getSingleResult();

            customer.getOrders().size();

            return customer;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Searches customers by first name, last name, or email.
     *
     * @param name the name or partial name to search for
     * @return list of matching Customer entities
     */
    public List<Customer> searchByName(String name) {
        return em.createQuery(
                "SELECT c FROM Customer c "
                + "WHERE LOWER(c.firstName) LIKE LOWER(:keyword) "
                + "OR LOWER(c.lastName) LIKE LOWER(:keyword) "
                + "OR LOWER(c.email) LIKE LOWER(:keyword)",
                Customer.class)
                .setParameter("keyword", "%" + name + "%")
                .getResultList();
    }

    /**
     * Returns the total number of customers in the database.
     *
     * @return total customer count
     */
    public Long countCustomers() {
        return em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class)
                .getSingleResult();
    }

    /**
     * Counts how many orders belong to a selected customer. This is used on the
     * List of Customers page.
     *
     * @param customerId selected customer id
     * @return number of orders placed by the customer
     */
    public Long countOrdersByCustomer(Long customerId) {
        if (customerId == null) {
            return 0L;
        }

        return em.createQuery(
                "SELECT COUNT(o) FROM CustomerOrder o "
                + "WHERE o.customer.customerId = :customerId",
                Long.class)
                .setParameter("customerId", customerId)
                .getSingleResult();
    }
}
