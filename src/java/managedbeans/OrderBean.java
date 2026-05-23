package managedbeans;

import ejb.CustomerEJB;
import ejb.OrderEJB;
import ejb.SmartwatchEJB;
import ejb.TabletEJB;
import entities.Customer;
import entities.CustomerOrder;
import entities.Product;
import entities.Smartwatch;
import entities.Tablet;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderBean is the backing bean for all Order-related JSF pages.
 *
 * I created this backing bean to handle:
 * - newOrder.xhtml   : creating a new order with customer + product dropdowns
 * - listOrders.xhtml : displaying all orders with Delete links
 * - searchOrder.xhtml: searching orders by order number
 *
 * The most important design decision here is how the product dropdown works.
 * Both Tablets and Smartwatches are Products, so the dropdown must show all
 * products (both types) combined. I used SelectItem lists populated from both
 * TabletEJB and SmartwatchEJB and combined them into a single dropdown list.
 *
 * The selected product ID is passed to OrderEJB.createOrder() which handles
 * the stock decrement atomically with the order creation in one JTA transaction.
 */
@Named("orderBean")
@RequestScoped
public class OrderBean {

    @EJB
    private OrderEJB orderEJB;

    @EJB
    private CustomerEJB customerEJB;

    @EJB
    private TabletEJB tabletEJB;

    @EJB
    private SmartwatchEJB smartwatchEJB;

    // Fields bound to the Create a New Order form.
    private Long selectedCustomerId;
    private Long selectedProductId;
    private Integer quantity;

    // For the search page.
    private Long searchOrderId;

    // Result data.
    private List<CustomerOrder> orders;
    private CustomerOrder foundOrder;

    // Dropdown option lists.
    private List<SelectItem> customerItems;
    private List<SelectItem> productItems;

    // Success/error message.
    private String message;

    /**
     * Action method triggered when the user clicks "Create an order".
     * Delegates to OrderEJB which handles stock decrement and order creation.
     *
     * @return navigation to listOrders on success, null to stay on form
     */
    public String createOrder() {
        if (selectedCustomerId == null || selectedProductId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select both a customer and a product.", null));
            return null;
        }

        String error = orderEJB.createOrder(selectedCustomerId, selectedProductId, quantity);

        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        message = "Order created successfully.";
        return "listOrders?faces-redirect=true";
    }

    /**
     * Action method for the Delete link on the order list page.
     * Delegates to OrderEJB which restores the stock quantity first.
     *
     * @param orderId the ID of the order to delete
     * @return null to refresh the same list page
     */
    public String deleteOrder(Long orderId) {
        String error = orderEJB.deleteOrder(orderId);
        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
        } else {
            message = "Order deleted successfully.";
        }
        // Refresh the order list.
        orders = null;
        return null;
    }

    /**
     * Returns all orders for the list page.
     *
     * @return list of all CustomerOrder entities with customers and products loaded
     */
    public List<CustomerOrder> getOrders() {
        if (orders == null) {
            orders = orderEJB.findAllOrders();
        }
        return orders;
    }

    /**
     * Searches for an order by its order number (ID) on the search page.
     *
     * @return null to stay on page and display the found order
     */
    public String searchOrder() {
        if (searchOrderId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter an order number.", null));
            return null;
        }
        foundOrder = orderEJB.findOrderById(searchOrderId);
        if (foundOrder == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No order found with number: " + searchOrderId, null));
        }
        return null;
    }

    /**
     * Builds the customer dropdown (SelectItem list) for the order creation form.
     * Each item shows the customer full name with their ID as the value.
     *
     * @return list of SelectItem for the customer h:selectOneMenu
     */
    public List<SelectItem> getCustomerItems() {
        if (customerItems == null) {
            customerItems = new ArrayList<>();
            List<Customer> customers = customerEJB.findAllCustomers();
            for (Customer c : customers) {
                customerItems.add(new SelectItem(c.getCustomerId(), c.getFullName()));
            }
        }
        return customerItems;
    }

    /**
     * Builds the product dropdown combining Tablets and Smartwatches.
     * Format: "Brand Model - $Price" as shown in the demo order creation page.
     * Both product types are included since each order can be for any product.
     *
     * @return list of SelectItem for the device h:selectOneMenu
     */
    public List<SelectItem> getProductItems() {
        if (productItems == null) {
            productItems = new ArrayList<>();

            // Add all tablets first.
            List<Tablet> tablets = tabletEJB.findAllTablets();
            for (Tablet t : tablets) {
                String label = t.getBrand() + " " + t.getModel() + " - $" + t.getPrice();
                productItems.add(new SelectItem(t.getProductId(), label));
            }

            // Add all smartwatches.
            List<Smartwatch> smartwatches = smartwatchEJB.findAllSmartwatches();
            for (Smartwatch s : smartwatches) {
                String label = s.getBrand() + " " + s.getModel() + " - $" + s.getPrice();
                productItems.add(new SelectItem(s.getProductId(), label));
            }
        }
        return productItems;
    }

    // Getters and setters.
    public Long getSelectedCustomerId() { return selectedCustomerId; }
    public void setSelectedCustomerId(Long selectedCustomerId) { this.selectedCustomerId = selectedCustomerId; }

    public Long getSelectedProductId() { return selectedProductId; }
    public void setSelectedProductId(Long selectedProductId) { this.selectedProductId = selectedProductId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getSearchOrderId() { return searchOrderId; }
    public void setSearchOrderId(Long searchOrderId) { this.searchOrderId = searchOrderId; }

    public CustomerOrder getFoundOrder() { return foundOrder; }
    public void setFoundOrder(CustomerOrder foundOrder) { this.foundOrder = foundOrder; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
