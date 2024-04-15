package be.docarch.odt2braille.xproc;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Locale;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.PEFHandler;
import be.docarch.odt2braille.ODT2PEFConverter;
import be.docarch.odt2braille.StatusIndicator;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ConfigurationDecoder;
import be.docarch.odt2braille.setup.ExportConfiguration;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.dom.DocumentBuilderImpl;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Odt2Braille extends DefaultStep  {
	
	private static final QName _source = new QName("source");
	private static final QName _liblouis_dir = new QName("liblouis-dir");
	private static final QName _result_brf = new QName("result-brf");
	private ReadablePipe config;
	private WritablePipe result;
	
	public Odt2Braille(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		config = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}
	
	@Override
	public void reset() {
		result.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			ODT odt = new ODT(new File(URI.create(getOption(_source).getString())));
			Constants.setLiblouisDirectory(new File(URI.create(getOption(_liblouis_dir).getString())));
			Constants.setStatusIndicator(
				new StatusIndicator() {
					public void setStatus(String value) {
						System.out.println(value); }
					public Locale getPreferredLocale() {
						return Locale.getDefault(); }});
			ExportConfiguration exportConfig = null;
			if (config != null && config.moreDocuments()) {
				// use the provided configuration
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write("<java version=\"1.6.0\" class=\"java.beans.XMLDecoder\">".getBytes("UTF-8"));
				Serializer serializer = runtime.getProcessor().newSerializer();
				serializer.setOutputStream(os);
				serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
				serializer.serializeNode(config.read());
				serializer.close();
				os.write("</java>".getBytes("UTF-8"));
				os.close();
				odt.loadConfiguration(new ByteArrayInputStream(os.toByteArray()));
			} else {
				// use the configuration stored in the ODT file itself
				// assume the path to the configuration is meta/odt2braille/configuration.rdf
				InputStream configRDF = odt.getFileAsStream("meta/odt2braille/configuration.rdf");
				if (configRDF != null) {
					Document doc = new DocumentBuilderImpl().parse(configRDF);
					// general configuration
					NodeList nodes = doc.getElementsByTagNameNS(
						"http://www.docarch.be/odt2braille/", "general-configuration");
					outer: for (int i = 0; i < nodes.getLength(); i++) {
						Element e = (Element)nodes.item(i);
						NodeList nnodes = e.getChildNodes();
						for (int ii = 0; ii < nnodes.getLength(); ii++) {
							Node nn = nnodes.item(ii);
							if (nn instanceof Element) {
								// serialize again
								Transformer transformer = TransformerFactory.newInstance().newTransformer();
								transformer.setOutputProperty(OutputKeys.METHOD, "xml");
								transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
								transformer.setOutputProperty(OutputKeys.INDENT, "no");
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								StreamResult result = new StreamResult(os);
								transformer.transform(new DOMSource(nn), result);
								odt.loadConfiguration(new ByteArrayInputStream(os.toByteArray()));
								break outer;
							}
						}
					}
					// export configuration
					nodes = doc.getElementsByTagNameNS(
						"http://www.docarch.be/odt2braille/", "export-configuration");
					outer: for (int i = 0; i < nodes.getLength(); i++) {
						Element e = (Element)nodes.item(i);
						NodeList nnodes = e.getChildNodes();
						for (int ii = 0; ii < nnodes.getLength(); ii++) {
							Node nn = nnodes.item(ii);
							if (nn instanceof Element) {
								// serialize again
								Transformer transformer = TransformerFactory.newInstance().newTransformer();
								transformer.setOutputProperty(OutputKeys.METHOD, "xml");
								transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
								transformer.setOutputProperty(OutputKeys.INDENT, "no");
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								StreamResult result = new StreamResult(os);
								transformer.transform(new DOMSource(nn), result);
								exportConfig = (ExportConfiguration)ConfigurationDecoder.readObject(
									new ByteArrayInputStream(os.toByteArray()));
								break outer;
							}
						}
					}
				}
			}
			if (exportConfig == null)
				exportConfig = new ExportConfiguration();
			PEF pef = ODT2PEFConverter.convert(
				odt,
				exportConfig,
				null);
			result.write(runtime.getProcessor().newDocumentBuilder().build(pef.getSinglePEF()));
			RuntimeValue brf = getOption(_result_brf);
			if (brf != null && !brf.getString().equals("")) {
				PEFHandler pefHandler = new PEFHandler(pef);
				exportConfig.setFileFormatType(ExportConfiguration.BRF);
				exportConfig.setCharSetType("org_daisy.EmbosserTableProvider.TableType.DE_DE");
				File tmpFile = pefHandler.convertToSingleFile(exportConfig.getFileFormat());
				File targetFile = new File(URI.create(brf.getString()));
				if (!targetFile.exists()) tmpFile.renameTo(targetFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XProcException(step.getNode(), e);
		}
	}
}
