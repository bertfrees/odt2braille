/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.setup;

import java.util.Locale;
import java.util.List;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Expression;
import java.beans.Encoder;
import java.beans.PropertyDescriptor;
import java.beans.Statement;
import java.lang.reflect.Method;
import org.daisy.braille.tools.Length;

public class PersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {

        if (oldInstance instanceof Configuration) {

            return new Expression(oldInstance, ConfigurationFactory.class, "getInstance", new Object[] {});

        } else if (oldInstance instanceof Locale) {

            Locale oldLocale = (Locale)oldInstance;
            if (oldLocale.getCountry().length() > 0) {
                return new Expression(oldInstance, Locale.class, "new", new Object[]{oldLocale.getLanguage(), oldLocale.getCountry()});
            } else {
                return new Expression(oldInstance, Locale.class, "new", new Object[]{oldLocale.getLanguage()});
            }

        } else if (oldInstance instanceof Length) {

            Length oldLength = (Length)oldInstance;
            switch (oldLength.getUnitsOfLength()) {
                case MILLIMETER:
                    return new Expression(oldInstance, Length.class, "newMillimeterValue", new Object[]{oldLength.getLength()});
                case INCH:
                    return new Expression(oldInstance, Length.class, "newInchValue", new Object[]{oldLength.getLength()});
                case CENTIMETER:
                    return new Expression(oldInstance, Length.class, "newCentimeterValue", new Object[]{oldLength.getLength()});
                case ROW:
                    return new Expression(oldInstance, Length.class, "newRowsValue", new Object[]{oldLength.getLength()});
                case COLUMN:
                    return new Expression(oldInstance, Length.class, "newColumnsValue", new Object[]{oldLength.getLength()});
            }
        }

        return super.instantiate(oldInstance, out);
    }

    @Override
    protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {

        try {

            PropertyDescriptor[] descriptors = BeanInfo.getPropertyDescriptors(type);

            if (descriptors != null) {

                initialize(type.getSuperclass(), oldInstance, newInstance, out);

                for (PropertyDescriptor d : descriptors) {
                    Method getter = d.getReadMethod();
                    Method setter = d.getWriteMethod();
                    if (getter != null) {
                        Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{});
                        Object oldValue = oldGetExp.getValue();
                        out.writeExpression(oldGetExp);
                        if (setter != null) {
                            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{});
                            Object newValue = newGetExp.getValue();
                            if (!equals(newValue, out.get(oldValue))) {
                                out.writeStatement(new Statement(oldInstance, setter.getName(), new Object[]{oldValue}));
                            }
                        }
                    }
                }

            } else if (type == SettingMap.class) {

                SettingMap oldMap = (SettingMap)oldInstance;
                SettingMap newMap = (SettingMap)newInstance;

                Object oldValue, newValue;

                for (Object key : oldMap.keys()) {

                    oldValue = oldMap.get(key);
                    newValue = newMap.get(key);

                    if (!oldValue.equals(newValue)) {
                        if (!(key instanceof String || key instanceof Integer)) {
                            out.writeExpression(instantiate(key, out));
                        }
                        out.writeExpression(new Expression(oldValue, oldInstance, "get", new Object[]{key}));
                    }
                }

            } else if (type == SettingList.class) {

                SettingList oldSettingList = (SettingList)oldInstance;
                SettingList newSettingList = (SettingList)newInstance;
                
                List oldList = oldSettingList.values();
                List newList = newSettingList.values();

                if (!oldList.equals(newList)) {
                    if (newList.size() > 0) {
                        out.writeStatement(new Statement(oldInstance, "clear", new Object[]{}));
                        newSettingList.clear();
                    }
                    int i = 0;
                    Object newObject;
                    for (Object oldObject : oldList) {
                        newObject = newSettingList.add();
                        newSettingList.remove(i++);
                        Expression expression = new Expression(oldObject, oldInstance, "add", new Object[]{});
                        if (oldObject.equals(newObject)) {
                            out.writeStatement(expression);
                        } else {
                            out.writeExpression(expression);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {

        if (oldInstance.getClass() == Length.class) {
            return equals(oldInstance, newInstance);
        }

        return (newInstance != null && oldInstance != null &&
                oldInstance.getClass() == newInstance.getClass());
    }
    
    private static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }
}
