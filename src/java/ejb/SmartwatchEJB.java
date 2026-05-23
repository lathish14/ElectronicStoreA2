package ejb;

import entities.Smartwatch;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SmartwatchEJB is a stateless session bean that handles all business logic
 * for Smartwatch product management.
 *
 * I implemented this EJB in parallel with TabletEJB to handle smartwatch
 * operations separately, since the two product types have different fields
 * and different JSF pages. All JPQL operations use the named queries defined
 * on the Smartwatch entity.
 */
@Stateless
public class SmartwatchEJB {

    private static final Logger LOGGER = Logger.getLogger(SmartwatchEJB.class.getName());

    @PersistenceContext(unitName = "ElectronicsStorePU")
    private EntityManager em;

    /**
     * Persists a new Smartwatch entity to the database.
     * Called when the user submits the Create a New Smartwatch form.
     *
     * @param smartwatch the Smartwatch entity populated from the JSF form
     * @return null on success, or an error message on failure
     */
    public String createSmartwatch(Smartwatch smartwatch) {
        try {
            em.persist(smartwatch);
            LOGGER.log(Level.INFO, "Created smartwatch: {0} {1}",
                    new Object[]{smartwatch.getBrand(), smartwatch.getModel()});
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to create smartwatch", ex);
            return "Failed to create smartwatch. Please check your input and try again.";
        }
    }

    /**
     * Retrieves all smartwatches from the database for the stock list page.
     * Uses the Smartwatch.findAll named query defined on the Smartwatch entity.
     *
     * @return list of all Smartwatch entities
     */
    @SuppressWarnings("unchecked")
    public List<Smartwatch> findAllSmartwatches() {
        return em.createNamedQuery("Smartwatch.findAll").getResultList();
    }

    /**
     * Finds a smartwatch by its product ID.
     * Used when loading smartwatch details for the order creation page.
     *
     * @param productId the primary key of the smartwatch
     * @return the Smartwatch entity, or null if not found
     */
    public Smartwatch findSmartwatchById(Long productId) {
        try {
            return (Smartwatch) em.createNamedQuery("Smartwatch.findById")
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Searches for smartwatches whose model name matches the given keyword.
     * Used on the Search for a Smartwatch page.
     *
     * @param model the model name (or partial name) to search for
     * @return list of matching Smartwatch entities
     */
    @SuppressWarnings("unchecked")
    public List<Smartwatch> searchByModel(String model) {
        return em.createQuery(
                "SELECT s FROM Smartwatch s WHERE LOWER(s.model) LIKE LOWER(CONCAT('%', :model, '%'))",
                Smartwatch.class)
                .setParameter("model", model)
                .getResultList();
    }

    /**
     * Updates an existing smartwatch in the database.
     * Used by OrderEJB to update stock quantity after an order is placed or deleted.
     *
     * @param smartwatch the modified Smartwatch entity to merge
     * @return the updated managed Smartwatch entity
     */
    public Smartwatch updateSmartwatch(Smartwatch smartwatch) {
        return em.merge(smartwatch);
    }
}
