package ejb;

import entities.Customer;
import entities.CustomerOrder;
import entities.Product;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OrderEJB is a stateless session bean that handles all business logic for
 * order management.
 *
 * This is the most complex EJB in the system because order creation and
 * deletion both require updating the product stock quantity. The assignment
 * explicitly requires stock number management:
 * - When an order is created, stock is decremented by the quantity ordered.
 * - When an order is deleted, stock is restored by the quantity of that order.
 *
 * I implemented this EJB to ensure all stock management happens in the
 * business layer and not in the JSF backing beans. The EntityManager JTA
 * transaction guarantees that the order and the stock update are atomic.
 */
@Stateless
public class OrderEJB {

    private static final Logger LOGGER = Logger.getLogger(OrderEJB.class.getName());

    @PersistenceContext(unitName = "ElectronicsStorePU")
    private EntityManager em;

    /**
     * Creates a new order for a given customer and product, then decrements
     * the product stock by the ordered quantity.
     *
     * The assignment requires: "after creating the order for the laptop:
     * Dell G7 17, the number of the laptop in stock is 98" (from 100 with qty 2).
     *
     * @param customerId the ID of the customer placing the order
     * @param productId  the ID of the product (Tablet or Smartwatch) being ordered
     * @param quantity   the number of units ordered
     * @return null on success, or an error message string on failure
     */
    public String createOrder(Long customerId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return "Quantity must be greater than zero.";
        }

        Customer customer = em.find(Customer.class, customerId);
        if (customer == null) {
            return "Customer not found.";
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            return "Product not found.";
        }

        // Check sufficient stock before creating the order.
        if (product.getStockQuantity() < quantity) {
            return "Insufficient stock. Available: " + product.getStockQuantity();
        }

        // Decrement stock quantity as required by the assignment.
        product.setStockQuantity(product.getStockQuantity() - quantity);
        em.merge(product);

        // Create and persist the order.
        CustomerOrder order = new CustomerOrder(customer, product, quantity);
        em.persist(order);

        LOGGER.log(Level.INFO, "Order created: customer={0}, product={1}, qty={2}",
                new Object[]{customer.getFullName(), product.getModel(), quantity});
        return null; // null = success
    }

    /**
     * Deletes an existing order and restores the product stock quantity.
     *
     * The assignment requires: "after deleting the order for the laptop:
     * Dell G7 17, the number of the laptop in stock is back to 100."
     *
     * @param orderId the primary key of the order to delete
     * @return null on success, or an error message string on failure
     */
    public String deleteOrder(Long orderId) {
        try {
            CustomerOrder order = em.find(CustomerOrder.class, orderId);
            if (order == null) {
                return "Order not found.";
            }

            // Restore the stock quantity before removing the order.
            Product product = order.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
                em.merge(product);
            }

            em.remove(order);
            LOGGER.log(Level.INFO, "Order deleted: orderId={0}", orderId);
            return null; // null = success

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to delete order: " + orderId, ex);
            return "Failed to delete order. Please try again.";
        }
    }

    /**
     * Retrieves all orders from the database for the order list page.
     * Orders are joined with customer and product so they display correctly
     * in the JSF data table.
     *
     * @return list of all CustomerOrder entities
     */
    @SuppressWarnings("unchecked")
    public List<CustomerOrder> findAllOrders() {
        return em.createQuery(
                "SELECT o FROM CustomerOrder o JOIN FETCH o.customer JOIN FETCH o.product ORDER BY o.orderId",
                CustomerOrder.class)
                .getResultList();
    }

    /**
     * Finds a single order by its order ID.
     * Used on the Search for an Order page and to display the Found Order.
     *
     * @param orderId the primary key of the order
     * @return the CustomerOrder entity, or null if not found
     */
    public CustomerOrder findOrderById(Long orderId) {
        try {
            return (CustomerOrder) em.createQuery(
                    "SELECT o FROM CustomerOrder o JOIN FETCH o.customer JOIN FETCH o.product WHERE o.orderId = :orderId",
                    CustomerOrder.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Finds all orders belonging to a specific customer.
     * Used when loading the Customer Detail page to show that customer's orders.
     *
     * @param customerId the customer whose orders should be retrieved
     * @return list of CustomerOrder entities for that customer
     */
    @SuppressWarnings("unchecked")
    public List<CustomerOrder> findOrdersByCustomer(Long customerId) {
        return em.createNamedQuery("CustomerOrder.findByCustomer")
                .setParameter("customerId", customerId)
                .getResultList();
    }

    /**
     * Returns the total number of orders in the database.
     * Displayed at the bottom of the order list page.
     *
     * @return total order count
     */
    public Long countOrders() {
        return (Long) em.createQuery("SELECT COUNT(o) FROM CustomerOrder o").getSingleResult();
    }

    /**
     * Builds the combined product label used in the order form dropdown.
     * Format: "Brand Model - $Price" (e.g. "Dell Dell G7 17 - $1819.0")
     *
     * This is a utility method used by the OrderBean backing bean to populate
     * the device selection dropdown on the Create a New Order page.
     *
     * @param product the product entity
     * @return formatted label string
     */
    public String buildProductLabel(Product product) {
        return product.getBrand() + " " + product.getModel() + " - $" + product.getPrice();
    }
}
