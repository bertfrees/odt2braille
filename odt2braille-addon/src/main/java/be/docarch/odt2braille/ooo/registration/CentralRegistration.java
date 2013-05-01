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

package be.docarch.odt2braille.ooo.registration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;

public class CentralRegistration {
	
	public static XSingleComponentFactory __getComponentFactory( String implName ) {
		StringTokenizer t = new StringTokenizer(getRegistrationClasses(), " ");
		while (t.hasMoreTokens()) {
			String className = t.nextToken();
			if (className != null && className.length() != 0) {
				try {
					Class regClass = Class.forName(className);
					Object result = regClass.getDeclaredMethod("__getComponentFactory", new Class[]{String.class})
							.invoke(regClass, implName);
					if (result != null) {
						return (XSingleComponentFactory)result; }}
				catch (Exception e) {}}}
		return null;
	}
	
	public static boolean __writeRegistryServiceInfo(XRegistryKey key) {
		boolean result = true;
		StringTokenizer t = new StringTokenizer(getRegistrationClasses(), " ");
		while (t.hasMoreTokens()) {
			String className = t.nextToken();
			if (className != null && className.length() != 0) {
				try {
					Class regClass = Class.forName(className);
					result &= ((Boolean)regClass.getDeclaredMethod("__writeRegistryServiceInfo", new Class[]{XRegistryKey.class})
							.invoke(regClass, key)).booleanValue(); }
				catch (Exception e) {}}}
		return result;
	}
	
	private static String getRegistrationClasses() {
		try {
			Enumeration<URL> urlEnum = CentralRegistration.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (urlEnum.hasMoreElements()) {
				URL url = urlEnum.nextElement();
				url.getFile();
				JarURLConnection connection = (JarURLConnection)url.openConnection();
				Manifest mf = connection.getManifest();
				Attributes attrs = (Attributes)mf.getAttributes(
						CentralRegistration.class.getCanonicalName().replace('.', '/').concat(".class"));
				if (attrs != null)
					return attrs.getValue("RegistrationClasses"); }}
		catch (IOException e) {}
		return "";
	}
}
