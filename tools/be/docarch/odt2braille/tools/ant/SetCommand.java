package be.docarch.odt2braille.tools.ant;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;
import org.apache.tools.ant.BuildException;
import be.docarch.odt2braille.setup.BeanInfo;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.SettingList;

public class SetCommand implements Command {

    private Class parentBeanClass;
    private String property;
    private Method[] writeMethods;
    private String value;
    private Class valueClass;
    private String key;
    private Class keyClass;

    public SetCommand(Class parentBeanClass) {
        this.parentBeanClass = parentBeanClass;
    };
        
    public void setProperty(String property) {
        this.property = property;

        AdditionalBeanInfo.PropertyDescriptor p1 = AdditionalBeanInfo.getPropertyDescriptor(parentBeanClass, property);
        if (p1 != null) {
            valueClass = p1.getPropertyType();
            keyClass = p1.getKeyType();
            writeMethods = p1.getWriteMethods();
            if (writeMethods == null) { throw new BuildException("Property '" + property + "' is read-only"); }
            return;
        }
        PropertyDescriptor p2 = BeanInfo.getPropertyDescriptor(parentBeanClass, property);
        if (p2 != null) {
            valueClass = p2.getPropertyType();
            if (!SettingMap.class.isAssignableFrom(valueClass) &&
                !SettingList.class.isAssignableFrom(valueClass)) {
                writeMethods = new Method[]{p2.getWriteMethod()};
                if (writeMethods == null) { throw new BuildException("Property '" + property + "' is read-only"); }
                return;
            }
        }

        throw new BuildException("Property '" + property + "' not supported");
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
        if (property != null) { validateKey(); }
    }

    private void validateValue() throws BuildException {
        if (value == null) { throw new BuildException("No 'value' parameter was defined"); }
    }

    private void validateKey() throws BuildException {
        if (keyClass != null && key == null) { throw new BuildException("A 'key' parameter must be defined for property '" + property + "'"); }
        if (keyClass == null && key != null) { throw new BuildException("Property '" + property + "' cannot have a 'key' parameter"); }
    }

    private static Object parse(String s, Class c) throws BuildException {
        if (c == String.class) {
            return s;
        } else if (c == boolean.class ||
                   c == Boolean.class) {
            return Boolean.parseBoolean(s);
        } else if (c == int.class ||
                   c == Integer.class) {
            return Integer.parseInt(s);
        } else if (Enum.class.isAssignableFrom(c)) {
            return Enum.valueOf(c, s);
        } else if (c == Locale.class) {
            if (!s.contains("-")) {
                return new Locale(s);
            } else {
                int i = s.indexOf("-");
                return new Locale(s.substring(0,i), s.substring(i+1));
            }
        }
        throw new BuildException("value " + s + " could not be parsed.");
    }

    public void applyTo(Object object) {
        if (property == null) { throw new BuildException("No 'property' parameter was defined"); }
        validateValue();
        validateKey();
        List allParameters = new ArrayList();
        if (key != null) { allParameters.add(parse(key, keyClass)); }
        allParameters.add(parse(value, valueClass));
        try {
            for (int i=0; i<writeMethods.length; i++) {
                Method method = writeMethods[i];
                Object[] parameters = new Object[method.getParameterTypes().length];
                for (int j=0; j<parameters.length; j++) {
                    parameters[j] = allParameters.remove(0);
                }
                object = method.invoke(object, parameters);
            }
        } catch(Exception e) {
            throw new BuildException(e);
        }
    }
}
