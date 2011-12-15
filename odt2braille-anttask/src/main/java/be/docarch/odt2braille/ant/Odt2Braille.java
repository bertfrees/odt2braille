package be.docarch.odt2braille.ant;

import java.io.File;
import java.util.Locale;
import java.util.List;
import java.util.logging.Level;
import java.text.DecimalFormat;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import be.docarch.odt2braille.Volume;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.ODT2PEFConverter;
import be.docarch.odt2braille.PEFHandler;
import be.docarch.odt2braille.PEFFileFormat;
import be.docarch.odt2braille.StatusIndicator;
import be.docarch.odt2braille.setup.Configuration;
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
            Configuration config = odt.getConfiguration();
            ExportConfiguration exportConfig = new ExportConfiguration();
            configuration.applyCommandsTo(config);
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
        if (liblouisDir == null) { throw new BuildException("Liblouis directory not defined"); }
    }
}
