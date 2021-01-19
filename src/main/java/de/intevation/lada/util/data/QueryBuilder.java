/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryBuilder<T> {

    private EntityManager manager;
    private CriteriaBuilder builder;
    private CriteriaQuery<T> query;
    private Root<T> root;
    private Class<T> clazz;
    private Predicate filter;

    /**
     * Create a new QueryBuilder for the specified class.
     *
     * @param m
     * @param c
     */
    public QueryBuilder(EntityManager m, Class<T> c) {
        this.manager = m;
        this.clazz = c;
        this.builder = this.manager.getCriteriaBuilder();
        this.query = this.builder.createQuery(this.clazz);
        this.root = this.query.from(this.clazz);
    }

    /**
     * Get the criteria query build with this class.
     *
     * @return The query.
     */
    public CriteriaQuery<T> getQuery() {
        if (this.filter == null) {
            this.query.where();
            return this.query;
        }
        this.query.where(this.filter);
        return this.query;
    }

    /**
     * Logical AND operation.
     *
     * @param id    The database column name.
     * @param value The filter value
     * @return The builder itself.
     */
    public QueryBuilder<T> and(String id, Object value) {
        Predicate p;
        if (value == null) {
            p = this.builder.isNull(this.root.get(id));
        } else {
            p = this.builder.equal(this.root.get(id), value);
        }
        if (this.filter != null) {
            this.filter = this.builder.and(this.filter, p);
        } else {
            this.filter = this.builder.and(p);
        }
        return this;
    }

    /**
     * Negate filter.
     *
     */
    public QueryBuilder<T> not() {
        if (this.filter == null) {
            return this;
        }
        this.filter = this.filter.not();
        return this;
    }

    /**
     * Logical AND with like operation.
     *
     * @param id    The database column name.
     * @param value The filter value
     * @return The builder itself.
     */
    public QueryBuilder<T> andLike(String id, String value) {
        Path<String> path = this.root.get(id);
        Predicate p =
            this.builder.like(this.builder.lower(path), value.toLowerCase());
        if (this.filter != null) {
            this.filter = this.builder.and(this.filter, p);
        } else {
            this.filter = this.builder.and(p);
        }
        return this;
    }

    /**
     * Logical OR operation.
     *
     * @param id    The database column name
     * @param value The filter value.
     * @return The builder itself.
     */
    public QueryBuilder<T> or(String id, Object value) {
        Predicate p;
        if (value == null) {
            p = this.builder.isNull(this.root.get(id));
        } else {
            p = this.builder.equal(this.root.get(id), value);
        }
        if (this.filter != null) {
            this.filter = this.builder.or(this.filter, p);
        } else {
            this.filter = this.builder.or(p);
        }
        return this;
    }

    /**
     * Logical OR with like operation.
     *
     * @param column    The database column name.
     * @param value     The filter value
     * @return The builder itself.
     */
    public QueryBuilder<T> orLike(String id, String value) {
        Path<String> path = this.root.get(id);
        Predicate p =
            this.builder.like(this.builder.lower(path), value.toLowerCase());
        if (this.filter != null) {
            this.filter = this.builder.or(this.filter, p);
        } else {
            this.filter = this.builder.or(p);
        }
        return this;
    }

    /**
     * Logical AND operation.
     * All elements in <i>values</i> will be concatenated with AND operator.
     *
     * @param id        The database column name.
     * @param values    List of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> and(String id, List<String> values) {
        if (values == null) {
            Predicate p = this.builder.isNull(this.root.get(id));
            if (this.filter != null) {
                this.filter = this.builder.and(this.filter, p);
            }
            return this;
        }
        for (String v : values) {
            this.and(id, v);
        }
        return this;
    }

    /**
     * Logical OR operation.
     * All elements in <i>values</i> will be concatenated with OR operator.
     *
     * @param id        The database column name.
     * @param values    List of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> or(String id, List<String> values) {
        if (values == null) {
            Predicate p = this.builder.isNull(this.root.get(id));
            if (this.filter != null) {
                this.filter = this.builder.or(this.filter, p);
            }
            return this;
        }
        this.orIn(id, values);
        return this;
    }

    /**
     * Logical OR operation.
     * All elements in <i>values</i> will be concatenated with OR operator.
     *
     * @param id        The database column name.
     * @param values    List of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> orIntList(String id, List<Integer> values) {
        for (Integer v: values) {
            this.or(id, v);
        }
        return this;
    }

    /**
     * Logical AND operation.
     * The actually defined query will be concatenated with the query defined
     * in the builder <i>b</i>.
     *
     * @param b     A builder.
     * @return The builder itself.
     */
    public QueryBuilder<T> and(QueryBuilder<T> b) {
        if (b == null || b.filter == null) {
            return this;
        }
        if (this.filter != null) {
            this.filter = this.builder.and(this.filter, b.filter);
        } else {
            this.filter = this.builder.and(b.filter);
        }
        return this;
    }

    /**
     * Logical OR operation.
     * The actually defined query will be concatenated with the query defined
     * in the builder <i>b</i>.
     *
     * @param b     A builder.
     * @return The builder itself.
     */
    public QueryBuilder<T> or(QueryBuilder<T> b) {
        if (b == null || b.filter == null) {
            return this;
        }
        if (this.filter != null) {
            this.filter = this.builder.or(this.filter, b.filter);
        } else {
            this.filter = this.builder.or(b.filter);
        }
        return this;
    }

    /**
     * IN operation combined as logical OR.
     * Test whether result of 'key' is in a list of values.
     *
     * @param key   The database column.
     * @param values    The list of values.
     *
     * @return The current Querybuilder.
     */
    public <M> QueryBuilder<T> orIn(String key, List<M> values) {
        Expression<M> exp = this.root.get(key);
        Predicate p = exp.in(values);
        if (this.filter == null) {
            this.filter = this.builder.or(p);
        } else {
            this.filter = this.builder.or(this.filter, p);
        }
        return this;
    }

    /**
     * IN operation combined as logical AND.
     * Test whether result of 'key' is in a list of values.
     *
     * @param key   The database column.
     * @param values    The list of values.
     *
     * @return The current Querybuilder.
     */
    public <M> QueryBuilder<T> andIn(String key, List<M> values) {
        Expression<M> exp = this.root.get(key);
        Predicate p = exp.in(values);
        if (this.filter == null) {
            this.filter = this.builder.and(p);
        } else {
            this.filter = this.builder.and(this.filter, p);
        }
        return this;
    }

    /**
     * Use 'distinct' in the query.
     */
    public void distinct() {
        this.query.distinct(true);
    }

    /**
     * Order result by the specified column name
     *
     * @param id    The column name.
     * @param asc   Ascending(true), Descending(false).
     */
    public void orderBy(String id, boolean asc) {
        if (asc) {
            this.query.orderBy(this.builder.asc(this.root.get(id)));
        } else {
            this.query.orderBy(this.builder.desc(this.root.get(id)));
        }
    }

    /**
     * Order result by the specified column name
     *
     * @param ids   Map of column names and boolean for asc/desc.
     */
    public void orderBy(Map<String, Boolean> ids) {
        List<Order> order = new ArrayList<Order>();
        for (String id : ids.keySet()) {
            if (ids.get(id)) {
                order.add(this.builder.asc(this.root.get(id)));
            } else {
                order.add(this.builder.desc(this.root.get(id)));
            }
        }
        this.query.orderBy(order);
    }

    /**
     * Get an empty instance of this builder to create subfilters.
     *
     * @return An empty instance of this builder.
     */
    public QueryBuilder<T> getEmptyBuilder() {
        QueryBuilder<T> copy = new QueryBuilder<T>(manager, clazz);
        copy.builder = this.builder;
        copy.root = this.root;
        return copy;
    }
}
