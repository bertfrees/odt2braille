package be.docarch.odt2braille.ant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.List;
import java.util.logging.Level;
import java.text.DecimalFormat;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import net.sf.saxon.dom.DocumentBuilderImpl;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.docarch.odt2braille.Volume;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.ODT2PEFConverter;
import be.docarch.odt2braille.PEFHandler;
import be.docarch.odt2braille.PEFFileFormat;
import be.docarch.odt2braille.StatusIndicator;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ConfigurationDecoder;
import be.docarch.odt2braille.setup.ExportConfiguration;

/**
 *
 * @author Bert Frees
 */
public class Odt2Braille extends Task {

    private File srcFile;
    private File targetFile;
    private File liblouisDir;
    private Bean configuration = new Bean(Configuration.class);
    private Bean exportConfiguration = new Bean(ExportConfiguration.class);
    
    public void setTargetfile(String file) {
        targetFile = new File(file);
        if (targetFile.exists()) {
            throw new BuildException("Target file or directory '" + file + "' already exists");
        }
    }
    
    public void setSrcfile(String file) {
        srcFile = new File(file);
        if (!srcFile.exists()) {
            throw new BuildException("Source file '" + file + "' does not exist");
        }
    }

    public void setLiblouisdir(String file) {
        liblouisDir = new File(file);
        if (!liblouisDir.exists() || !liblouisDir.isDirectory()) {
            throw new BuildException("Liblouis directory '" + file + "' does not exist");
        }
    }

    public Bean createConfiguration() {
        validate();
        return configuration;
    }
    
    public Bean createExportconfiguration() {
        validate();
        return exportConfiguration;
    }
    
    @Override
    public void execute() throws BuildException {
        validate();
        Constants.getLogger().setLevel(Level.SEVERE);
        Constants.setStatusIndicator(new StatusIndicator() {
            @Override
            public void setStatus(String value) {
                System.out.println(value);
            }
            @Override
            public Locale getPreferredLocale() {
                return Locale.getDefault();
            }
        });
        try {
            ODT odt = new ODT(srcFile);
            Constants.setLiblouisDirectory(liblouisDir);

            // load configuration stored in the ODT file
            // assume the path to the configuration is meta/odt2braille/configuration.rdf
            ExportConfiguration exportConfig = null;
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

            // overwrite configuration with properties from Ant script
            configuration.applyCommandsTo(odt.getConfiguration());
            if (exportConfig == null)
                exportConfig = new ExportConfiguration();
            exportConfiguration.applyCommandsTo(exportConfig);
            PEF pef = ODT2PEFConverter.convert(odt, exportConfig, null);

            File[] brailleFiles;
            if (exportConfig.getFileFormat() instanceof PEFFileFormat) {
                if (exportConfig.getMultipleFiles()) {
                    brailleFiles = pef.getPEFs();
                } else {
                    brailleFiles = new File[] { pef.getSinglePEF() };
                }
            } else {
                PEFHandler pefHandler = new PEFHandler(pef);
                if (exportConfig.getMultipleFiles()) {
                    brailleFiles = pefHandler.convertToFiles(exportConfig.getFileFormat());
                } else {
                    brailleFiles = new File[] { pefHandler.convertToSingleFile(exportConfig.getFileFormat()) };
                }
            }
            if (brailleFiles.length > 1) {
                File newFile;
                if (!targetFile.exists()) {
                    targetFile.mkdir();
                    String fileSeparator = System.getProperty("file.separator");
                    String folderName = targetFile.getName();
                    String brailleExt = exportConfig.getFileFormat().getFileExtension();
                    List<Volume> volumes = pef.getVolumes();
                    DecimalFormat format = new DecimalFormat();
                    format.setMaximumFractionDigits(0);
                    format.setMinimumIntegerDigits(1+(int)Math.floor(Math.log10(volumes.size())));
                    format.setGroupingUsed(false);
                    for (int i=0; i<brailleFiles.length; i++) {
                        newFile = new File(targetFile.getAbsolutePath() + fileSeparator + folderName + "." + format.format(i+1) + brailleExt);
                        if (newFile.exists()) { newFile.delete(); }
                        brailleFiles[i].renameTo(newFile);
                    }
                }
            } else {
                if (!targetFile.exists()) {
                    brailleFiles[0].renameTo(targetFile);
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void validate() throws BuildException {
        if (srcFile == null) { throw new BuildException("Source file not defined"); }
        if (targetFile == null) { throw new BuildException("Target file not defined"); }
        if (liblouisDir == null) {
            try {
                setLiblouisdir(new File(new File(
                    Odt2Braille.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "liblouis").getAbsolutePath());
            } catch (Exception e) {
                throw new BuildException("Liblouis directory not defined");
            }
        }
    }
}
