package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * This Tablet class is a child entity class of Product.
 *
 * I created this class to store tablet-specific details only. The common
 * product details such as brand, model, display size, weight, operating system,
 * connectivity, Wi-Fi capability, price, and stock quantity are already stored
 * in the Product superclass.
 *
 * Since Product uses Joined-Subclass inheritance, this Tablet entity will have
 * its own TABLET table, and it will be joined with the PRODUCT table using the
 * same product id.
 */
@Entity
@Table(name = "TABLET")
@PrimaryKeyJoinColumn(name = "PRODUCT_ID")
@NamedQueries({
    // This query is used to get all tablets from the database.
    @NamedQuery(name = "Tablet.findAll", query = "SELECT t FROM Tablet t"),

    // This query is used to find a tablet using the inherited product id.
    @NamedQuery(name = "Tablet.findById", query = "SELECT t FROM Tablet t WHERE t.productId = :productId"),

    // This query is used to search tablets by brand.
    @NamedQuery(name = "Tablet.findByBrand", query = "SELECT t FROM Tablet t WHERE t.brand = :brand"),

    // This query is used to search tablets by model.
    @NamedQuery(name = "Tablet.findByModel", query = "SELECT t FROM Tablet t WHERE t.model = :model")
})
public class Tablet extends Product implements Serializable {

    private static final long serialVersionUID = 1L;

    // Storage capacity is specific to tablets, for example 64GB or 128GB.
    @Column(name = "STORAGE_CAPACITY", length = 50)
    private String storageCapacity;

    // This stores whether the tablet supports a stylus or not.
    @Column(name = "STYLUS_SUPPORT")
    private Boolean stylusSupport;

    // Battery capacity is specific to tablets, for example 8000mAh.
    @Column(name = "BATTERY_CAPACITY", length = 50)
    private String batteryCapacity;

    /**
     * Empty constructor is required by JPA.
     */
    public Tablet() {
    }

    /**
     * This constructor is used to create a tablet with both common product
     * details and tablet-specific details.
     *
     * @param brand product brand
     * @param model product model
     * @param displaySize product display size
     * @param weight product weight
     * @param operatingSystem product operating system
     * @param connectivity product connectivity type
     * @param wifiCapability whether the tablet has Wi-Fi
     * @param price tablet price
     * @param stockQuantity available stock quantity
     * @param description product description
     * @param storageCapacity tablet storage capacity
     * @param stylusSupport whether the tablet supports a stylus
     * @param batteryCapacity tablet battery capacity
     */
    public Tablet(String brand, String model, String displaySize, String weight,
            String operatingSystem, String connectivity, Boolean wifiCapability,
            Double price, Integer stockQuantity, String description,
            String storageCapacity, Boolean stylusSupport, String batteryCapacity) {

        // This sends the common product details to the Product superclass.
        super(brand, model, displaySize, weight, operatingSystem, connectivity,
                wifiCapability, price, stockQuantity, description);

        this.storageCapacity = storageCapacity;
        this.stylusSupport = stylusSupport;
        this.batteryCapacity = batteryCapacity;
    }

    /**
     * @return the tablet storage capacity
     */
    public String getStorageCapacity() {
        return storageCapacity;
    }

    /**
     * @param storageCapacity the tablet storage capacity to set
     */
    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    /**
     * @return true if the tablet supports a stylus
     */
    public Boolean getStylusSupport() {
        return stylusSupport;
    }

    /**
     * @param stylusSupport the stylus support value to set
     */
    public void setStylusSupport(Boolean stylusSupport) {
        this.stylusSupport = stylusSupport;
    }

    /**
     * @return the tablet battery capacity
     */
    public String getBatteryCapacity() {
        return batteryCapacity;
    }

    /**
     * @param batteryCapacity the tablet battery capacity to set
     */
    public void setBatteryCapacity(String batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }
}
