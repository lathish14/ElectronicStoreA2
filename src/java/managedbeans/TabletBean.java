package managedbeans;

import ejb.TabletEJB;
import entities.Tablet;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.List;

/**
 * TabletBean is the backing bean for all Tablet-related JSF pages.
 *
 * I created this backing bean to handle:
 * - newTablet.xhtml   : creating a new tablet (Create a New Tablet form)
 * - listTablets.xhtml : displaying all tablets in stock
 * - searchTablet.xhtml: searching tablets by model name
 *
 * The bean delegates all data access to TabletEJB which keeps business
 * logic in the EJB layer as required by the assignment.
 */
@Named("tabletBean")
@RequestScoped
public class TabletBean {

    @EJB
    private TabletEJB tabletEJB;

    // Fields bound to the Create a New Tablet form inputs.
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

    // Tablet-specific fields.
    private String storageCapacity;
    private Boolean stylusSupport = false;
    private String batteryCapacity;

    // Used on the search page.
    private String searchModel;

    // Holds results for list and search pages.
    private List<Tablet> tablets;
    private List<Tablet> foundTablets;

    // Success/error message to display after create.
    private String message;

    /**
     * Action method triggered when the user clicks "Create a Tablet".
     * Constructs a Tablet entity from form fields and delegates to TabletEJB.
     *
     * @return navigation to listTablets on success, null to stay on form
     */
    public String createTablet() {
        Tablet tablet = new Tablet(brand, model, displaySize, weight,
                operatingSystem, connectivity, wifiCapability,
                price, stockQuantity, description,
                storageCapacity, stylusSupport, batteryCapacity);

        String error = tabletEJB.createTablet(tablet);

        if (error != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return null;
        }

        message = "Successfully created the tablet: " + brand + " " + model;
        return "listTablets?faces-redirect=true";
    }

    /**
     * Loads all tablets from the database for the stock list page.
     * Called from listTablets.xhtml via #{tabletBean.tablets}.
     *
     * @return list of all Tablet entities
     */
    public List<Tablet> getTablets() {
        if (tablets == null) {
            tablets = tabletEJB.findAllTablets();
        }
        return tablets;
    }

    /**
     * Searches tablets by model name when the user submits the search form.
     *
     * @return navigation to found tablets result on the same page
     */
    public String searchTablets() {
        if (searchModel == null || searchModel.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter a model name to search.", null));
            return null;
        }
        foundTablets = tabletEJB.searchByModel(searchModel.trim());
        return null; // Stay on search page and display results.
    }

    // Getters and setters for all form fields.
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

    public String getStorageCapacity() { return storageCapacity; }
    public void setStorageCapacity(String storageCapacity) { this.storageCapacity = storageCapacity; }

    public Boolean getStylusSupport() { return stylusSupport; }
    public void setStylusSupport(Boolean stylusSupport) { this.stylusSupport = stylusSupport; }

    public String getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public String getSearchModel() { return searchModel; }
    public void setSearchModel(String searchModel) { this.searchModel = searchModel; }

    public List<Tablet> getFoundTablets() { return foundTablets; }
    public void setFoundTablets(List<Tablet> foundTablets) { this.foundTablets = foundTablets; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
