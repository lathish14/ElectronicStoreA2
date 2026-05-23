package ejb;

import entities.Tablet;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TabletEJB is a stateless session bean that handles all business logic for
 * Tablet product management.
 *
 * I implemented this EJB to keep all tablet-related database operations in the
 * business layer, separate from the JSF presentation layer. The backing beans
 * call this EJB for creating tablets, listing stock, and searching by model.
 *
 * All JPQL operations use the named queries defined on the Tablet entity.
 */
@Stateless
public class TabletEJB {

    private static final Logger LOGGER = Logger.getLogger(TabletEJB.class.getName());

    @PersistenceContext(unitName = "ElectronicsStorePU")
    private EntityManager em;

    /**
     * Persists a new Tablet entity to the database.
     * Called when the user submits the Create a New Tablet form.
     *
     * @param tablet the Tablet entity populated from the JSF form
     * @return null on success, or an error message on failure
     */
    public String createTablet(Tablet tablet) {
        try {
            em.persist(tablet);
            LOGGER.log(Level.INFO, "Created tablet: {0} {1}", new Object[]{tablet.getBrand(), tablet.getModel()});
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to create tablet", ex);
            return "Failed to create tablet. Please check your input and try again.";
        }
    }

    /**
     * Retrieves all tablets from the database for the stock list page.
     * Uses the Tablet.findAll named query defined on the Tablet entity.
     *
     * @return list of all Tablet entities, ordered by brand and model
     */
    @SuppressWarnings("unchecked")
    public List<Tablet> findAllTablets() {
        return em.createNamedQuery("Tablet.findAll").getResultList();
    }

    /**
     * Finds a tablet by its product ID.
     * Used when loading tablet details for order creation.
     *
     * @param productId the primary key of the tablet
     * @return the Tablet entity, or null if not found
     */
    public Tablet findTabletById(Long productId) {
        try {
            return (Tablet) em.createNamedQuery("Tablet.findById")
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Searches for tablets whose model name matches the given keyword.
     * Used on the Search for a Tablet page. Returns all matching tablets.
     *
     * @param model the model name (or partial name) to search for
     * @return list of matching Tablet entities
     */
    @SuppressWarnings("unchecked")
    public List<Tablet> searchByModel(String model) {
        return em.createQuery(
                "SELECT t FROM Tablet t WHERE LOWER(t.model) LIKE LOWER(CONCAT('%', :model, '%'))",
                Tablet.class)
                .setParameter("model", model)
                .getResultList();
    }

    /**
     * Updates an existing tablet in the database.
     * Currently used by the OrderEJB to update stock quantity after an order.
     *
     * @param tablet the modified Tablet entity to merge
     * @return the updated managed Tablet entity
     */
    public Tablet updateTablet(Tablet tablet) {
        return em.merge(tablet);
    }
}
