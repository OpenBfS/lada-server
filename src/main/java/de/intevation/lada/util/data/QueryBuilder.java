/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * A builder for criteria queries to query objects of a specified class.
 *
 * Use Repository.queryBuilder(Class<T> c) to create new builders, e.g.
 * in service implementations.
 *
 * @param <T> Class for which queries will be build
 *
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
     * @param <T> The class for which a QueryBuilder is requested.
     * @param m EntityManager used to get a CriteriaBuilder.
     * @param c The class for which a QueryBuilder is requested.
     *
     * @return QueryBuilder for the given class.
    */
    QueryBuilder(EntityManager m, Class<T> c) {
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
     * Logical AND operation for null values.
     * @param <X> Column datatype
     *
     * @param id    The database column name.
     * @return The builder itself.
     */
    public <X> QueryBuilder<T> andIsNull(
        SingularAttribute<? super T, X> id) {
        return and(id, null);
    }

    /**
     * Logical AND operation.
     * @param <X> Column datatype
     *
     * @param id    The database column name.
     * @param value The filter value
     * @return The builder itself.
     */
    public <X> QueryBuilder<T> and(
            SingularAttribute<? super T, X> id, X value) {
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
     * @return The builder itself.
     */
    public QueryBuilder<T> not() {
        if (this.filter == null) {
            return this;
        }
        this.filter = this.filter.not();
        return this;
    }

    /**
     * Logical AND with case insensitive LIKE operation.
     * @param id    The database column name.
     * @param value The filter value
     * @return The builder itself.
     */
    public QueryBuilder<T> andLike(
            SingularAttribute<T, String> id, String value) {
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
     * @param <X> Column datatype
     *
     * @param id    The database column name
     * @param value The filter value.
     * @return The builder itself.
     */
    public <X> QueryBuilder<T> or(SingularAttribute<? super T, X> id, X value) {
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
     * Logical OR with case insensitive LIKE operation.
     *
     * @param id    The database column name.
     * @param value The filter value.
     * @return The builder itself.
     */
    public QueryBuilder<T> orLike(
            SingularAttribute<T, String> id, String value) {
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
     * @param values    Iterable of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> and(
            SingularAttribute<T, String> id, Iterable<String> values) {
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
     * @param values    Collection of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> or(
            SingularAttribute<T, String> id, Collection<String> values) {
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
     * @param values    Iterable of values.
     * @return The builder itself.
     */
    public QueryBuilder<T> orIntList(
            SingularAttribute<T, Integer> id, Iterable<Integer> values) {
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
     * @param <M>   The type of the values.
     * @param key   The database column.
     * @param values    The collection of values.
     *
     * @return The current Querybuilder.
     */
    public <M> QueryBuilder<T> orIn(
            SingularAttribute<T, M> key, Collection<M> values) {
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
     * @param <M>   The type of the values.
     * @param key   The database column.
     * @param values    The collection of values.
     *
     * @return The current Querybuilder.
     */
    public <M> QueryBuilder<T> andIn(
            SingularAttribute<T, M> key, Collection<M> values) {
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
     * Order result by the specified column name.
     * @param <X> Column datatype
     *
     * @param id    The column name.
     * @param asc   Ascending(true), Descending(false).
     * @return The current Querybuilder.
     */
    public <X> QueryBuilder<T> orderBy(
            SingularAttribute<T, X> id, boolean asc) {
        Order order;
        if (asc) {
            order = this.builder.asc(this.root.get(id));
        } else {
            order = this.builder.desc(this.root.get(id));
        }
        this.query.orderBy(order);
        return this;
    }

    /**
     * Order result by the specified column name.
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
