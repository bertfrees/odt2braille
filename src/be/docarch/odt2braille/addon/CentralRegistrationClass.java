/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.addon;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * This class was generated by the NetBeans OOo API plugin. Do not change.
 *
 * @author  Bert Frees
 */
public class CentralRegistrationClass {
    
    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        String regClassesList = getRegistrationClasses();
        StringTokenizer t = new StringTokenizer(regClassesList, " ");
        while (t.hasMoreTokens()) {
            String className = t.nextToken();
            if (className != null && className.length() != 0) {
                try {
                    Class regClass = Class.forName(className);
                    Method writeRegInfo = regClass.getDeclaredMethod("__getComponentFactory", new Class[]{String.class});
                    Object result = writeRegInfo.invoke(regClass, sImplementationName);
                    if (result != null) {
                       return (XSingleComponentFactory)result;
                    }
                }
                catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        boolean bResult = true;
        String regClassesList = getRegistrationClasses();
        StringTokenizer t = new StringTokenizer(regClassesList, " ");
        while (t.hasMoreTokens()) {
            String className = t.nextToken();
            if (className != null && className.length() != 0) {
                try {
                    Class regClass = Class.forName(className);
                    Method writeRegInfo = regClass.getDeclaredMethod("__writeRegistryServiceInfo", new Class[]{XRegistryKey.class});
                    Object result = writeRegInfo.invoke(regClass, xRegistryKey);
                    bResult &= ((Boolean)result).booleanValue();
                }
                catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return bResult;
    }

    private static String getRegistrationClasses() {
        CentralRegistrationClass c = new CentralRegistrationClass();
        String name = c.getClass().getCanonicalName().replace('.', '/').concat(".class");
        try {
            Enumeration<URL> urlEnum = c.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (urlEnum.hasMoreElements()) {
                URL url = urlEnum.nextElement();
                String file = url.getFile();
                JarURLConnection jarConnection =
                    (JarURLConnection) url.openConnection();
                Manifest mf = jarConnection.getManifest();

                Attributes attrs = (Attributes) mf.getAttributes(name);
                if ( attrs != null ) {
                    String classes = attrs.getValue( "RegistrationClasses" );
                    return classes;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
            
        return "";
    }
    
    /** Creates a new instance of CentralRegistrationClass */
    private CentralRegistrationClass() {
    }
}
