package be.docarch.odt2braille.setup;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Expression;
import java.beans.Encoder;
import java.beans.PropertyDescriptor;
import java.beans.Statement;
import java.lang.reflect.Method;


public class PersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {

        if (oldInstance instanceof Configuration) {

            return new Expression(oldInstance, Configuration.class, "newInstance", new Object[] {});

        } else if (oldInstance instanceof Locale) {

            Locale oldLocale = (Locale)oldInstance;
            if (oldLocale.getCountry().length() > 0) {
                return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{oldLocale.getLanguage(), oldLocale.getCountry()});
            } else {
                return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{oldLocale.getLanguage()});
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
                    
                    Object newObject;

                    for (Object oldObject : oldList) {
                        newObject = newSettingList.add();
                        if (!oldObject.equals(newObject)) {
                            out.writeExpression(new Expression(oldObject, oldInstance, "add", new Object[]{}));
                        } else {
                            out.writeStatement(new Statement(oldInstance, "add", new Object[]{}));
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
        return (newInstance != null && oldInstance != null &&
                oldInstance.getClass() == newInstance.getClass());
    }
    
    private static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }
}
