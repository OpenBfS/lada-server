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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Tag;
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
            builder.like(tagRoot.get("name"), prefix + "\\_" + today + "\\_%");
        Order nameOrder = builder.asc(tagRoot.get("name"));
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
                        Integer.parseInt(item.getName().split("_")[2]);
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
        currentTag.setIsAutoTag(true);
        currentTag.setNetworkId(netzbetreiberId);
        currentTag.setTagType(Tag.TAG_TYPE_NETZBETREIBER);
        currentTag.setName(prefix + "_" + today + "_" + serNumber);

        // Generated tags expire after 548 days
        Instant then = Instant.now()
            .plus(Tag.GENERATED_EXPIRATION_TIME, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS);
        currentTag.setValUntil(Timestamp.from(then));

        return repository.create(currentTag);
    }

    /**
     * Sets tags for the given probe records an connected messung records.
     * @param probeIds Sample ids to set tags for
     * @param tagId Tag id to set
     */
    public void setTagsByProbeIds(
        List<Integer> probeIds, Integer tagId
    ) {
        // TODO: Instead of using IDs as parameters, pass the objects directly
        // instead of fetching them from the database again, whenever possible.

        //Get given probe and messung records
        List<Sample> probes = repository.filterPlain(
            repository.queryBuilder(Sample.class).andIn("id", probeIds)
            .getQuery());
        List<Measm> messungs = repository.filterPlain(
            repository.queryBuilder(Measm.class).andIn("sampleId", probeIds)
            .getQuery());

        //Set tags
        probes.forEach(probe -> {
            TagLink zuordnung = new TagLink();
            zuordnung.setTagId(tagId);
            zuordnung.setSampleId(probe.getId());
            repository.create(zuordnung);
        });

        messungs.forEach(messung -> {
            TagLink zuordnung = new TagLink();
            zuordnung.setTagId(tagId);
            zuordnung.setMeasmId(messung.getId());
            repository.create(zuordnung);
        });
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
