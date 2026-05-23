package managedbeans;

import ejb.SmartwatchEJB;
import entities.Smartwatch;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.List;

/**
 * SmartwatchBean is the backing bean for all Smartwatch-related JSF pages.
 *
 * I created this backing bean to handle:
 * - newSmartwatch.xhtml   : creating a new smartwatch
 * - listSmartwatches.xhtml: displaying all smartwatches in stock
 * - searchSmartwatch.xhtml: searching smartwatches by model name
 *
 * The bean delegates all data access to SmartwatchEJB to keep
 * business logic separate from the presentation layer.
 */
@Named("smartwatchBean")
@RequestScoped
public class SmartwatchBean {

    @EJB
    private SmartwatchEJB smartwatchEJB;

    // Fields bound to the Create a New Smartwatch form inputs.
    private String brand;
    private String model;
    private String displaySize;
    private String weight;
    private String operatingSystem;
    private String connectivity;
    private Boolean wifiCapability = false;
    private Double price;
    private Integer stockQuantity;
    private String description;

    // Smartwatch-specific fields.
    private Boolean healthMonitoring = false;
    private Boolean fitnessTracking = false;
    private String wearableConnectivity;

    // Used on the search page.
    private String searchModel;

    // Holds results for list and search pages.
    private List<Smartwatch> smartwatches;
    private List<Smartwatch> foundSmartwatches;

    // Success/error message displayed after create.
    private String message;

    /**
     * Action method triggered when the user clicks "Create a Smartwatch".
     * Constructs a Smartwatch entity from form fields and delegates to EJB.
     *
     * @return navigation to listSmartwatches on success, null to stay
     */
    public String createSmartwatch() {
        Smartwatch smartwatch = new Smartwatch(brand, model, displaySize, weight,
                operatingSystem, connectivity, wifiCapability,
                price, stockQuantity, description,
                healthMonitoring, fitnessTracking, wearableConnectivity);

        String error = smartwatchEJB.createSmartwatch(smartwatch);

        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        message = "Successfully created the smartwatch: " + brand + " " + model;
        return "listSmartwatches?faces-redirect=true";
    }

    /**
     * Loads all smartwatches from the database for the stock list page.
     *
     * @return list of all Smartwatch entities
     */
    public List<Smartwatch> getSmartwatches() {
        if (smartwatches == null) {
            smartwatches = smartwatchEJB.findAllSmartwatches();
        }
        return smartwatches;
    }

    /**
     * Searches smartwatches by model name for the search page.
     *
     * @return null to stay on the search page and display results
     */
    public String searchSmartwatches() {
        if (searchModel == null || searchModel.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter a model name to search.", null));
            return null;
        }
        foundSmartwatches = smartwatchEJB.searchByModel(searchModel.trim());
        return null;
    }

    // Getters and setters.
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getDisplaySize() { return displaySize; }
    public void setDisplaySize(String displaySize) { this.displaySize = displaySize; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getConnectivity() { return connectivity; }
    public void setConnectivity(String connectivity) { this.connectivity = connectivity; }

    public Boolean getWifiCapability() { return wifiCapability; }
    public void setWifiCapability(Boolean wifiCapability) { this.wifiCapability = wifiCapability; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getHealthMonitoring() { return healthMonitoring; }
    public void setHealthMonitoring(Boolean healthMonitoring) { this.healthMonitoring = healthMonitoring; }

    public Boolean getFitnessTracking() { return fitnessTracking; }
    public void setFitnessTracking(Boolean fitnessTracking) { this.fitnessTracking = fitnessTracking; }

    public String getWearableConnectivity() { return wearableConnectivity; }
    public void setWearableConnectivity(String wearableConnectivity) { this.wearableConnectivity = wearableConnectivity; }

    public String getSearchModel() { return searchModel; }
    public void setSearchModel(String searchModel) { this.searchModel = searchModel; }

    public List<Smartwatch> getFoundSmartwatches() { return foundSmartwatches; }
    public void setFoundSmartwatches(List<Smartwatch> foundSmartwatches) { this.foundSmartwatches = foundSmartwatches; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
