package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;

/**
 * This Product class is the parent entity class for the products in the
 * electronics store.
 *
 * I used this class to keep the common product details in one place because
 * both Tablet and Smartwatch have shared attributes such as brand, model,
 * display size, weight, operating system, connectivity, and Wi-Fi capability.
 *
 * The assignment requires inheritance mapping using Joined-Subclass Strategy,
 * so I used @Inheritance(strategy = InheritanceType.JOINED).
 */
@Entity
@Table(name = "PRODUCT")
@Inheritance(strategy = InheritanceType.JOINED)
/*
 * These named queries are used by the EJB layer to retrieve and search
 * product records without writing the same JPQL again in multiple places.
 */
@NamedQueries({
    // This query is used to get all products from the database.
    @NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"),

    // This query is used to find one product by its product id.
    @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.productId = :productId"),

    // This query is used to search products by brand name.
    @NamedQuery(name = "Product.findByBrand", query = "SELECT p FROM Product p WHERE p.brand = :brand"),

    // This query is used to search products by model name.
    @NamedQuery(name = "Product.findByModel", query = "SELECT p FROM Product p WHERE p.model = :model")
})
public abstract class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * This entity stores only the attributes that are common to all product
     * types. Tablet and Smartwatch store their own specific attributes in
     * separate subclass tables because the assignment requires joined
     * inheritance mapping.
     */
    // This is the primary key for the PRODUCT table.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    // Brand is required because every product must have a brand.
    @NotBlank(message = "Brand is required")
    @Column(name = "BRAND", nullable = false, length = 100)
    private String brand;

    // Model is required because every product must have a model.
    @NotBlank(message = "Model is required")
    @Column(name = "MODEL", nullable = false, length = 100)
    private String model;

    // Common display size field for both tablets and smartwatches.
    @Column(name = "DISPLAY_SIZE", length = 50)
    private String displaySize;

    // Common weight field for both product types.
    @Column(name = "WEIGHT", length = 50)
    private String weight;

    // Operating system of the product, for example Android, iOS, or Wear OS.
    @Column(name = "OPERATING_SYSTEM", length = 100)
    private String operatingSystem;

    // Connectivity information, for example Bluetooth, NFC, 4G, or 5G.
    @Column(name = "CONNECTIVITY", length = 100)
    private String connectivity;

    // This stores whether the product supports Wi-Fi or not.
    @Column(name = "WIFI_CAPABILITY")
    private Boolean wifiCapability;

    // Price is required because products must be sold with a price.
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or greater")
    @Column(name = "PRICE", nullable = false)
    private Double price;

    /*
     * Stock quantity is needed for order processing.
     * The business layer can use this value to check whether enough stock is
     * available before allowing an order to be created.
     * When an order is created, this quantity can be reduced.
     * When an order is deleted, this quantity can be increased again.
     */
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be zero or greater")
    @Column(name = "STOCK_QUANTITY", nullable = false)
    private Integer stockQuantity;

    // A short description of the product.
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    /**
     * Empty constructor is required by JPA.
     */
    public Product() {
    }

    /**
     * This constructor is used to create a product with common product details.
     *
     * @param brand product brand
     * @param model product model
     * @param displaySize product display size
     * @param weight product weight
     * @param operatingSystem product operating system
     * @param connectivity product connectivity type
     * @param wifiCapability whether the product has Wi-Fi
     * @param price product price
     * @param stockQuantity available stock quantity
     * @param description product description
     */
    public Product(String brand, String model, String displaySize, String weight,
            String operatingSystem, String connectivity, Boolean wifiCapability,
            Double price, Integer stockQuantity, String description) {

        this.brand = brand;
        this.model = model;
        this.displaySize = displaySize;
        this.weight = weight;
        this.operatingSystem = operatingSystem;
        this.connectivity = connectivity;
        this.wifiCapability = wifiCapability;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    /**
     * @return the product id
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * @param productId the product id to set
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * @return the product brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @param brand the product brand to set
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * @return the product model
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model the product model to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the display size
     */
    public String getDisplaySize() {
        return displaySize;
    }

    /**
     * @param displaySize the display size to set
     */
    public void setDisplaySize(String displaySize) {
        this.displaySize = displaySize;
    }

    /**
     * @return the product weight
     */
    public String getWeight() {
        return weight;
    }

    /**
     * @param weight the product weight to set
     */
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * @return the operating system
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * @param operatingSystem the operating system to set
     */
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * @return the connectivity details
     */
    public String getConnectivity() {
        return connectivity;
    }

    /**
     * @param connectivity the connectivity details to set
     */
    public void setConnectivity(String connectivity) {
        this.connectivity = connectivity;
    }

    /**
     * @return true if the product has Wi-Fi support
     */
    public Boolean getWifiCapability() {
        return wifiCapability;
    }

    /**
     * @param wifiCapability the Wi-Fi support value to set
     */
    public void setWifiCapability(Boolean wifiCapability) {
        this.wifiCapability = wifiCapability;
    }

    /**
     * @return the product price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * @param price the product price to set
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * @return the available stock quantity
     */
    public Integer getStockQuantity() {
        return stockQuantity;
    }

    /**
     * @param stockQuantity the stock quantity to set
     */
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * @return the product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the product description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks whether this product currently has stock available.
     *
     * @return true if stock quantity is greater than zero
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * Checks whether this product has enough stock for a requested quantity.
     *
     * @param requestedQuantity quantity requested by the customer
     * @return true if enough stock is available
     */
    public boolean hasEnoughStock(int requestedQuantity) {
        return stockQuantity != null
                && requestedQuantity > 0
                && stockQuantity >= requestedQuantity;
    }

    /**
     * Returns a readable product name using brand and model.
     *
     * @return product brand and model together
     */
    public String getDisplayName() {
        String productBrand = brand != null ? brand : "";
        String productModel = model != null ? model : "";
        return (productBrand + " " + productModel).trim();
    }

    /**
     * Checks whether this product supports Wi-Fi.
     *
     * @return true if Wi-Fi capability is enabled
     */
    public boolean hasWifiSupport() {
        return Boolean.TRUE.equals(wifiCapability);
    }

    /**
     * Checks whether this product has a valid selling price.
     *
     * @return true if price is zero or greater
     */
    public boolean hasValidPrice() {
        return price != null && price >= 0;
    }

    /**
     * Reduces the stock quantity when an order is placed.
     *
     * @param quantity quantity to reduce
     * @return true if stock was reduced successfully
     */
    public boolean reduceStock(int quantity) {
        if (hasEnoughStock(quantity)) {
            stockQuantity = stockQuantity - quantity;
            return true;
        }
        return false;
    }

    /**
     * Increases the stock quantity when an order is cancelled or removed.
     *
     * @param quantity quantity to add back
     * @return true if stock was increased successfully
     */
    public boolean increaseStock(int quantity) {
        if (quantity > 0) {
            if (stockQuantity == null) {
                stockQuantity = 0;
            }
            stockQuantity = stockQuantity + quantity;
            return true;
        }
        return false;
    }
}
