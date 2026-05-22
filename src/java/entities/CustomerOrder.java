package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * This CustomerOrder class is an entity class for storing customer orders.
 *
 * I created this class because the assignment requires customer orders to be
 * persisted in the database. Each order belongs to one customer, and each order
 * is for one product item only.
 *
 * This entity also stores quantity and order date so the business layer can
 * manage stock when an order is created or deleted.
 */
@Entity
@Table(name = "CUSTOMER_ORDER")
@NamedQueries({
    // This query is used to get all orders from the database.
    @NamedQuery(name = "CustomerOrder.findAll", query = "SELECT o FROM CustomerOrder o"),

    // This query is used to find an order by order id.
    @NamedQuery(name = "CustomerOrder.findById", query = "SELECT o FROM CustomerOrder o WHERE o.orderId = :orderId"),

    // This query is used to find all orders for one customer.
    @NamedQuery(name = "CustomerOrder.findByCustomer", query = "SELECT o FROM CustomerOrder o WHERE o.customer.customerId = :customerId"),

    // This query is used to find all orders for one product.
    @NamedQuery(name = "CustomerOrder.findByProduct", query = "SELECT o FROM CustomerOrder o WHERE o.product.productId = :productId")
})
public class CustomerOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    // This is the primary key for the CUSTOMER_ORDER table.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Long orderId;

    /*
     * Many orders can belong to one customer.
     * This matches the assignment requirement that a customer can have multiple orders.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    /*
     * Each order is for one product item only.
     * The product can be a Tablet or Smartwatch because both extend Product.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    // Quantity ordered by the customer.
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    // Price at the time of ordering. This is useful if product price changes later.
    @Column(name = "ORDER_PRICE", nullable = false)
    private Double orderPrice;

    // Total amount = quantity * order price.
    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Double totalAmount;

    // Date when the order was created.
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_DATE", nullable = false)
    private Date orderDate;

    /**
     * Empty constructor is required by JPA.
     */
    public CustomerOrder() {
        this.orderDate = new Date();
    }

    /**
     * This constructor is used to create an order with customer, product, and
     * quantity details.
     *
     * @param customer customer who places the order
     * @param product product ordered by the customer
     * @param quantity quantity ordered
     */
    public CustomerOrder(Customer customer, Product product, Integer quantity) {
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.orderPrice = product.getPrice();
        this.totalAmount = this.orderPrice * quantity;
        this.orderDate = new Date();
    }

    /**
     * This method recalculates the total amount when price or quantity changes.
     */
    public void calculateTotalAmount() {
        if (orderPrice != null && quantity != null) {
            this.totalAmount = orderPrice * quantity;
        }
    }

    /**
     * @return the order id
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the order id to set
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the customer who placed the order
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * @return the product ordered
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * @return the quantity ordered
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
    }

    /**
     * @return the order price
     */
    public Double getOrderPrice() {
        return orderPrice;
    }

    /**
     * @param orderPrice the order price to set
     */
    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
        calculateTotalAmount();
    }

    /**
     * @return the total order amount
     */
    public Double getTotalAmount() {
        return totalAmount;
    }

    /**
     * @param totalAmount the total amount to set
     */
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * @return the order date
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * @param orderDate the order date to set
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}
