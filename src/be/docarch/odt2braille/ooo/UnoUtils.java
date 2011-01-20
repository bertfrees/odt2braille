/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

package be.docarch.odt2braille.ooo;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;


/**
 * This class was taken from com.versusoft.packages.ooo
 *
 * @author Vincent Spiewak
 */
public class UnoUtils {

    public static void storeToURL(String fileURL, String filter, XFrame xFrame) throws com.sun.star.io.IOException {

        PropertyValue[] conversionProperties = new PropertyValue[1];
        conversionProperties[0] = new PropertyValue();
        conversionProperties[0].Name = "FilterName";
        conversionProperties[0].Value = filter;

        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xFrame.getController().getModel());
        storable.storeToURL(fileURL, conversionProperties);
    }

    public static String UnoURLtoURL(String unoURL, XComponentContext xContext) {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                unoURL = unoURL.substring(6);
            } else {
                if (unoURL.startsWith("file://localhost")) {
                    unoURL = unoURL.replaceFirst("file://localhost", "");
                } else {
                    unoURL = unoURL.substring(7);
                }
            }
            return URLDecoder.decode(unoURL, "UTF-8");

        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }

    public static String createUnoFileURL(String filelocation, XComponentContext xContext) throws MalformedURLException {

        java.net.URL before;

        if (System.getProperty("os.name").contains("Windows")) {
            before = new URL("file:/" + filelocation);
        } else {
            before = new URL("file://" + filelocation);
        }


        // Create a URL, which can be used by UNO
        String myUNOFileURL = com.sun.star.uri.ExternalUriReferenceTranslator.create(xContext).translateToInternal(before.toExternalForm());

        if (myUNOFileURL.length() == 0 && filelocation.length() > 0) {
            throw new MalformedURLException();
        }

        return myUNOFileURL;
    }

    public static String createUnoTmpFile(String prefix, String suffix, XComponentContext xContext) throws java.io.IOException {
        String tmpUrl = File.createTempFile(
                prefix,
                suffix).getAbsolutePath();
        System.out.println("Tmp File=" + tmpUrl);
        tmpUrl = UnoUtils.createUnoFileURL(tmpUrl, xContext);
        System.out.println("Uno Tmp File=" + tmpUrl);
        return tmpUrl;
    }

    public static String getUILocale(XComponentContext xContext) throws com.sun.star.uno.Exception {

        XMultiComponentFactory serviceManager = xContext.getServiceManager();
       // create the provider

       String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
       XMultiServiceFactory xProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, serviceManager.createInstanceWithContext(sProviderService, xContext));
        String sReadOnlyView = "com.sun.star.configuration.ConfigurationUpdateAccess";
        PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
        aPathArgument.Name = "nodepath";
        aPathArgument.Value = "org.openoffice.Setup/L10N";

        Object[] aArguments = new Object[1];
        aArguments[0] = aPathArgument;

        XInterface xViewRoot = (XInterface) xProvider.createInstanceWithArguments(sReadOnlyView, aArguments);
        XNameAccess xProperties = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xViewRoot);

        return (String) xProperties.getByName("ooLocale");

    }
}
