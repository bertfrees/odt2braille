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

package be.docarch.odt2braille.ooo;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchDescriptor;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

import be.docarch.odt2braille.Constants;

public final class ProtocolHandler extends WeakBase
		implements XServiceInfo, XDispatchProvider, XInitialization, XDispatch {
	
	private static final String IMPLEMENTATION_NAME = "be.docarch.odt2braille.ooo.ProtocolHandler";
	private static final String[] SERVICE_NAMES = { "com.sun.star.frame.ProtocolHandler" };
	
	private static final boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac os");
	
	public static XSingleComponentFactory __getComponentFactory(String name) {
		if (IMPLEMENTATION_NAME.equals(name))
			return Factory.createComponentFactory(ProtocolHandler.class, SERVICE_NAMES);
		return null;
	}

	public static boolean __writeRegistryServiceInfo(XRegistryKey key) {
		return Factory.writeRegistryServiceInfo(IMPLEMENTATION_NAME, SERVICE_NAMES, key);
	}
	
	private final XComponentContext context;
	private XFrame frame;
	private UnoGUI gui;
	
	public ProtocolHandler(XComponentContext context) {
		this.context = context;
	}
	
	public void init() {
		if (gui == null)
			gui = new UnoGUI(context, frame);
	}
	
	/* XServiceInfo */
	public String getImplementationName() {
		 return IMPLEMENTATION_NAME;
	}

	public boolean supportsService(String service) {
		for (int i = 0; i < SERVICE_NAMES.length; i++)
			if (service.equals(SERVICE_NAMES[i]))
				return true;
		return false;
	}

	public String[] getSupportedServiceNames() {
		return SERVICE_NAMES;
	}
	
	/* XInitialization */
	public void initialize(Object[] object) throws com.sun.star.uno.Exception {
		if (object.length > 0)
			this.frame = (XFrame)UnoRuntime.queryInterface(XFrame.class, object[0]);
	}
	
	/* XDispatchProvider */
	public XDispatch queryDispatch(URL url, String targetFrameName, int searchFlags) {
		if (url.Protocol.startsWith("be.docarch.odt2braille"))
			if (url.Path.compareTo("FormatCommand") == 0 ||
			    url.Path.compareTo("ExportCommand") == 0 ||
			   (url.Path.compareTo("EmbossCommand") == 0 && !IS_MAC_OS) ||
			    url.Path.compareTo("InsertDotPatternCommand") == 0 ||
			    url.Path.compareTo("InsertSixKeysCommand") == 0)
				return this;
		return null;
	}
	
	public XDispatch[] queryDispatches(DispatchDescriptor[] descriptors) {
		XDispatch[] dispatcher = new XDispatch[descriptors.length];
		for (int i = 0; i < descriptors.length; ++i)
			dispatcher[i] = queryDispatch(descriptors[i].FeatureURL, descriptors[i].FrameName, descriptors[i].SearchFlags);
		return dispatcher;
	}
	
	/* XDispatch */
	public void dispatch(URL url, PropertyValue[] args) {
		try {
			if (url.Protocol.startsWith("be.docarch.odt2braille")) {
				init();
				if (url.Path.compareTo("FormatCommand") == 0)
					gui.changeSettings();
				else if (url.Path.compareTo("ExportCommand") == 0)
					gui.exportBraille();
				else if (url.Path.compareTo("EmbossCommand") == 0)
					gui.embossBraille();
				else if (url.Path.compareTo("InsertDotPatternCommand") == 0)
					gui.insertBraille();
				else if (url.Path.compareTo("InsertSixKeysCommand") == 0)
					gui.sixKeyEntryMode(); }
		} finally {
			Constants.flushLogger();
		}
	}
	
	public void addStatusListener(XStatusListener listener, URL url) {}
	
	public void removeStatusListener(XStatusListener listener, URL url) {}
	
}
