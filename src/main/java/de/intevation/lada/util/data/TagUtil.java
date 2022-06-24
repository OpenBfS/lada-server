/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.TagZuordnung;
import de.intevation.lada.model.stammdaten.Tag;
import de.intevation.lada.util.rest.Response;

/**
 * Utility class containing methods for creating and setting tags.
 */
public class TagUtil {

    private final Repository repository;

    @Inject
    private TagUtil(Repository repository) {
        this.repository = repository;
    }

    /**
     * Creates an auto generated tag using the current date and a given prefix.
     * Format is: {prefix}_yyyyMMdd_{serialNumber}
     * @param prefix Prefix to set
     * @param netzbetreiberId netzbetreiberId to set in the tag
     * @return Response of tag creation
     */
    public synchronized Response generateTag(
        String prefix,
        String netzbetreiberId
    ) {
        //Get current date
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String today = date.format(formatter);

        //Get latest generated tag
        CriteriaBuilder builder =
            repository.entityManager().getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> tagRoot = criteriaQuery.from(Tag.class);
        Predicate nameFilter =
            builder.like(tagRoot.get("tag"), prefix + "\\_" + today + "\\_%");
        Order nameOrder = builder.asc(tagRoot.get("tag"));
        criteriaQuery.where(nameFilter);
        criteriaQuery.orderBy(nameOrder);
        List<Tag> tags = repository.filterPlain(criteriaQuery);

        Integer serNumber = 1;
        //If tags were found, find next serial number
        if (tags.size() > 0) {
            AtomicInteger lastSerNumber = new AtomicInteger(0);
            tags.forEach(item -> {
                try {
                    Integer currentserial =
                        Integer.parseInt(item.getTag().split("_")[2]);
                    if (lastSerNumber.get() < currentserial) {
                        lastSerNumber.set(currentserial);
                    }
                } catch (NumberFormatException nfe) {
                    //There might be a user generated tag also matching
                    // the generated tag pattern: Skip
                }
            });
            serNumber = lastSerNumber.get() + 1;
        }

        //Create next tag
        Tag currentTag = new Tag();
        currentTag.setAutoTag(true);
        currentTag.setNetzbetreiberId(netzbetreiberId);
        currentTag.setTypId(Tag.TAG_TYPE_NETZBETREIBER);
        currentTag.setTag(prefix + "_" + today + "_" + serNumber);

        // Generated tags expire after 548 days
        Instant then = Instant.now()
            .plus(Tag.GENERATED_EXPIRATION_TIME, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS);
        currentTag.setGueltigBis(Timestamp.from(then));

        return repository.create(currentTag);
    }

    /**
     * Sets tags for the given probe records an connected messung records.
     * @param probeIds Probe ids to set tags for
     * @param tagId Tag id to set
     * @return List of created tag references
     */
    public List<TagZuordnung> setTagsByProbeIds(
            List<Integer> probeIds, Integer tagId
    ) {
        // TODO: Instead of using IDs as parameters, pass the objects directly
        // instead of fetching them from the database again, whenever possible.

        //Get given probe and messung records
        List<Probe> probes = repository.filterPlain(
            repository.queryBuilder(Probe.class).andIn("id", probeIds)
            .getQuery());
        List<Messung> messungs = repository.filterPlain(
            repository.queryBuilder(Messung.class).andIn("probeId", probeIds)
            .getQuery());

        //Set tags
        List<TagZuordnung> zuordnungs = new ArrayList<TagZuordnung>();
        probes.forEach(probe -> {
            TagZuordnung zuordnung = new TagZuordnung();
            zuordnung.setTagId(tagId);
            zuordnung.setProbeId(probe.getId());
            repository.create(zuordnung);
            zuordnungs.add(zuordnung);
        });

        messungs.forEach(messung -> {
            TagZuordnung zuordnung = new TagZuordnung();
            zuordnung.setTagId(tagId);
            zuordnung.setMessungId(messung.getId());
            repository.create(zuordnung);
            zuordnungs.add(zuordnung);
        });
        return zuordnungs;
    }

    /**
     * @return Timestamp Tag.MST_TAG_EXPIRATION_TIME days after now.
     */
    public static Timestamp getMstTagDefaultExpiration() {
        Instant then = Instant.now()
            .plus(Tag.MST_TAG_EXPIRATION_TIME, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS);
        return Timestamp.from(then);
    }
}
