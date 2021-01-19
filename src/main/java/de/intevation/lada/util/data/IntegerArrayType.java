/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Implementation for a new data type in the postgresql/postgis jdbc driver.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class IntegerArrayType implements UserType {
    protected static final int  SQLTYPE = java.sql.Types.ARRAY;

    @Override
    public int[] sqlTypes() {
        return new int[] {SQLTYPE};
    }

    @Override
    public Class<Integer[]> returnedClass() {
        return Integer[].class;
    }

    @Override
    public boolean equals(Object x, Object y)
    throws HibernateException {
        return x == null ? y == null : x.equals(y);
    }

    @Override
    public int hashCode(Object x)
    throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object deepCopy(Object value)
    throws HibernateException {
        return value == null ? null : ((Integer[]) value).clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value)
    throws HibernateException {
        return (Integer[]) this.deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
    throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
    throws HibernateException {
        return original;
    }

    @Override
    public Object nullSafeGet(
        ResultSet rs,
        String[] names,
        SharedSessionContractImplementor session,
        Object owner)
    throws HibernateException, SQLException {
        Array array = rs.getArray(names[0]);
        Integer[] javaArray = (Integer[]) array.getArray();
        return javaArray;
    }

    @Override
    public void nullSafeSet(
        PreparedStatement st,
        Object value,
        int index,
        SharedSessionContractImplementor session)
    throws HibernateException, SQLException {
        Connection connection = st.getConnection();
        Integer[] castObject = (Integer[]) value;
        Array array = connection.createArrayOf("integer", castObject);
        st.setArray(index, array);
    }
}
