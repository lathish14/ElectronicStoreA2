package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * This Smartwatch class is a child entity class of Product.
 *
 * I created this class to store smartwatch-specific details only. The common
 * product details such as brand, model, display size, weight, operating system,
 * connectivity, Wi-Fi capability, price, and stock quantity are already stored
 * in the Product superclass.
 *
 * Since Product uses Joined-Subclass inheritance, this Smartwatch entity will
 * have its own SMARTWATCH table, and it will be joined with the PRODUCT table
 * using the same product id.
 */
@Entity
@Table(name = "SMARTWATCH")
@PrimaryKeyJoinColumn(name = "PRODUCT_ID")
@NamedQueries({
    // This query is used to get all smartwatches from the database.
    @NamedQuery(name = "Smartwatch.findAll", query = "SELECT s FROM Smartwatch s"),

    // This query is used to find a smartwatch using the inherited product id.
    @NamedQuery(name = "Smartwatch.findById", query = "SELECT s FROM Smartwatch s WHERE s.productId = :productId"),

    // This query is used to search smartwatches by brand.
    @NamedQuery(name = "Smartwatch.findByBrand", query = "SELECT s FROM Smartwatch s WHERE s.brand = :brand"),

    // This query is used to search smartwatches by model.
    @NamedQuery(name = "Smartwatch.findByModel", query = "SELECT s FROM Smartwatch s WHERE s.model = :model")
})
public class Smartwatch extends Product implements Serializable {

    private static final long serialVersionUID = 1L;

    // This stores health monitoring features, for example heart rate tracking.
    @Column(name = "HEALTH_MONITORING")
    private Boolean healthMonitoring;

    // This stores fitness tracking features, for example step counter.
    @Column(name = "FITNESS_TRACKING")
    private Boolean fitnessTracking;

    // This stores wearable connectivity, for example Bluetooth or NFC.
    @Column(name = "WEARABLE_CONNECTIVITY", length = 100)
    private String wearableConnectivity;

    /**
     * Empty constructor is required by JPA.
     */
    public Smartwatch() {
    }

    /**
     * This constructor is used to create a smartwatch with both common product
     * details and smartwatch-specific details.
     *
     * @param brand product brand
     * @param model product model
     * @param displaySize product display size
     * @param weight product weight
     * @param operatingSystem product operating system
     * @param connectivity product connectivity type
     * @param wifiCapability whether the smartwatch has Wi-Fi
     * @param price smartwatch price
     * @param stockQuantity available stock quantity
     * @param description product description
     * @param healthMonitoring whether the smartwatch has health monitoring
     * @param fitnessTracking whether the smartwatch has fitness tracking
     * @param wearableConnectivity smartwatch wearable connectivity
     */
    public Smartwatch(String brand, String model, String displaySize, String weight,
            String operatingSystem, String connectivity, Boolean wifiCapability,
            Double price, Integer stockQuantity, String description,
            Boolean healthMonitoring, Boolean fitnessTracking, String wearableConnectivity) {

        // This sends the common product details to the Product superclass.
        super(brand, model, displaySize, weight, operatingSystem, connectivity,
                wifiCapability, price, stockQuantity, description);

        this.healthMonitoring = healthMonitoring;
        this.fitnessTracking = fitnessTracking;
        this.wearableConnectivity = wearableConnectivity;
    }

    /**
     * @return true if the smartwatch has health monitoring
     */
    public Boolean getHealthMonitoring() {
        return healthMonitoring;
    }

    /**
     * @param healthMonitoring the health monitoring value to set
     */
    public void setHealthMonitoring(Boolean healthMonitoring) {
        this.healthMonitoring = healthMonitoring;
    }

    /**
     * @return true if the smartwatch has fitness tracking
     */
    public Boolean getFitnessTracking() {
        return fitnessTracking;
    }

    /**
     * @param fitnessTracking the fitness tracking value to set
     */
    public void setFitnessTracking(Boolean fitnessTracking) {
        this.fitnessTracking = fitnessTracking;
    }

    /**
     * @return the smartwatch wearable connectivity
     */
    public String getWearableConnectivity() {
        return wearableConnectivity;
    }

    /**
     * @param wearableConnectivity the wearable connectivity to set
     */
    public void setWearableConnectivity(String wearableConnectivity) {
        this.wearableConnectivity = wearableConnectivity;
    }
}
