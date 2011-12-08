package be.docarch.odt2braille.tools.ant;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import be.docarch.odt2braille.setup.BeanInfo;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.NoteReferenceFormat;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.CharacterStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.ListStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;

public class AdditionalBeanInfo {

    private static final Map<Class,PropertyDescriptor[]> propertyDescriptorsMap
               = new HashMap<Class,PropertyDescriptor[]>();

    public static PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
        if (!propertyDescriptorsMap.containsKey(beanClass)) { return null; }
        return propertyDescriptorsMap.get(beanClass);
    }

    public static PropertyDescriptor getPropertyDescriptor(Class beanClass, String property) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
        while (propertyDescriptors != null) {
            for (int i=0; i<propertyDescriptors.length; i++) {
                if (propertyDescriptors[i].getName().equals(property)) {
                    return propertyDescriptors[i];
                }
            }
            beanClass = beanClass.getSuperclass();
            propertyDescriptors = getPropertyDescriptors(beanClass);
        }
        return null;
    }

    static {

        Class beanClass;

        try {

            beanClass = Configuration.class;

            PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[7];
            propertyDescriptors[0] = newMappedPropertyDescriptor(beanClass, "paragraphStyle",      ParagraphStyle.class,      String.class);
            propertyDescriptors[1] = newMappedPropertyDescriptor(beanClass, "characterStyle",      CharacterStyle.class,      String.class);
            propertyDescriptors[2] = newMappedPropertyDescriptor(beanClass, "headingStyle",        HeadingStyle.class,        int.class);
            propertyDescriptors[3] = newMappedPropertyDescriptor(beanClass, "tableStyle",          TableStyle.class,          String.class);
            propertyDescriptors[4] = newMappedPropertyDescriptor(beanClass, "listStyle",           ListStyle.class,           int.class);
            propertyDescriptors[5] = newMappedPropertyDescriptor(beanClass, "noteReferenceFormat", NoteReferenceFormat.class, String.class);

            Method[] m = new Method[2];
            m[0] = BeanInfo.getPropertyDescriptor(beanClass, "translationTables").getReadMethod();
            m[1] = SettingMap.class.getMethod("get", new Class[]{Object.class});
            java.beans.PropertyDescriptor p = BeanInfo.getPropertyDescriptor(TranslationTable.class, "id");

            propertyDescriptors[6] = new PropertyDescriptor("translationTable", String.class, Locale.class,
                    new Method[] {m[0],m[1], p.getReadMethod()},
                    new Method[] {m[0],m[1], p.getWriteMethod()});

            propertyDescriptorsMap.put(beanClass, propertyDescriptors);

            beanClass = TocStyle.class;

            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newMappedPropertyDescriptor(beanClass, "level", TocLevelStyle.class, int.class)
            });
            
        } catch (NoSuchMethodException e) {
        }
    }

    public static class PropertyDescriptor {

        private final String property;
        private final Class propertyClass;
        private final Class keyClass;
        private final Method[] readMethods;
        private final Method[] writeMethods;

        private PropertyDescriptor(String property,
                                   Class propertyClass,
                                   Class keyClass,
                                   Method[] readMethods,
                                   Method[] writeMethods) {
            
            this.property = property;
            this.propertyClass = propertyClass;
            this.keyClass = keyClass;
            this.readMethods = readMethods;
            this.writeMethods = writeMethods;
        }

        public String getName()           { return property; }
        public Class getPropertyType()    { return propertyClass; }
        public Class getKeyType()         { return keyClass; }
        public Method[] getReadMethods()  { return readMethods; }
        public Method[] getWriteMethods() { return writeMethods; }
    }

    private static PropertyDescriptor newMappedPropertyDescriptor(Class beanClass,
                                                                  String property,
                                                                  Class propertyClass,
                                                                  Class keyClass)
                                                           throws NoSuchMethodException {
        Method[] readMethods = new Method[] {
            BeanInfo.getPropertyDescriptor(beanClass, property + "s").getReadMethod(),
            SettingMap.class.getMethod("get", new Class[]{Object.class}) };
        return new PropertyDescriptor(property, propertyClass, keyClass, readMethods, null);
    }
}
