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

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;


/**
 * This class was generated by the NetBeans OOo API plugin.
 * <code>queryDispatch</code> and <code>dispatch</code> were changed.
 * In <code>dispatch</code>, calls are made to <code>UnoGUI.changeSettings</code>, <code>UnoGUI.exportBraille</code> or <code>UnoGUI.embossBraille</code>,
 * depending on which menu item was selected.
 *
 * @author  Bert Frees
 */
public final class Odt2BrailleAddOn extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XInitialization,
              com.sun.star.frame.XDispatch
{
    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = Odt2BrailleAddOn.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };

    public Odt2BrailleAddOn( XComponentContext context )
    {
        m_xContext = context;
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(Odt2BrailleAddOn.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
                                                       String sTargetFrameName,
                                                       int iSearchFlags )
    {
        if ( aURL.Protocol.startsWith("be.docarch.odt2braille.addon.odt2brailleaddon"))
        {
            if ( aURL.Path.compareTo("SettingsCommand") == 0 ) {
                return this;
            } else if (aURL.Path.compareTo("ExportCommand") == 0) {
                return this;
            } else if (aURL.Path.compareTo("EmbossCommand") == 0) {
                return this;
            } else if (aURL.Path.compareTo("InsertDotPatternCommand") == 0) {
                return this;
            } else if (aURL.Path.compareTo("InsertSixKeysCommand") == 0) {
                return this;
            }
        }
        return null;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
         com.sun.star.frame.DispatchDescriptor[] seqDescriptors )
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
            new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i )
        {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                                             seqDescriptors[i].FrameName,
                                             seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }

    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception
    {
        if ( object.length > 0 )
        {
            m_xFrame = (com.sun.star.frame.XFrame)UnoRuntime.queryInterface(
                com.sun.star.frame.XFrame.class, object[0]);
        }
    }

    // com.sun.star.frame.XDispatch:
     public void dispatch( com.sun.star.util.URL aURL,
                           com.sun.star.beans.PropertyValue[] aArguments )
    {
         if ( aURL.Protocol.startsWith("be.docarch.odt2braille.addon.odt2brailleaddon"))
        {
             if ( aURL.Path.compareTo("SettingsCommand") == 0 ) {

                 UnoGUI unoGui = null;

                 try {
                     unoGui = new UnoGUI(m_xContext, m_xFrame);
                     unoGui.changeSettings(SettingsDialog.SAVE_SETTINGS);
                 } finally {
                     unoGui.clean();
                 }

                 return;
             }
             
             else if ( aURL.Path.compareTo("ExportCommand") == 0 ) {

                 UnoGUI unoGui = null;

                 try {
                     unoGui = new UnoGUI(m_xContext, m_xFrame);
                     unoGui.exportBraille();
                 } finally {
                     unoGui.clean();
                 }

                 return;
             }

             else if ( aURL.Path.compareTo("EmbossCommand") == 0 ) {

                 UnoGUI unoGui = null;

                 try {
                     unoGui = new UnoGUI(m_xContext, m_xFrame);
                     unoGui.embossBraille();
                 } finally {
                     unoGui.clean();
                 }

                 return;
             }

             else if ( aURL.Path.compareTo("InsertDotPatternCommand") == 0 ) {

                 UnoGUI unoGui = null;

                 try {
                     unoGui = new UnoGUI(m_xContext, m_xFrame);
                     unoGui.insertBraille();
                 } finally {
                     unoGui.clean();
                 }

                 return;
             }

             else if ( aURL.Path.compareTo("InsertSixKeysCommand") == 0 ) {

                 UnoGUI unoGui = null;

                 try {
                     unoGui = new UnoGUI(m_xContext, m_xFrame);
                     unoGui.sixKeyEntryMode();
                 } finally {
                     unoGui.clean();
                 }

                 return;
             }
        }
    }

    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
                                    com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
                                       com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

}
