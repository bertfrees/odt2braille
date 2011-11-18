package be.docarch.odt2braille.tools.ant;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import be.docarch.odt2braille.*;
import be.docarch.odt2braille.setup.*;

/**
 *
 * @author Bert Frees
 */
public class Odt2Braille extends Task {
    
    private File inFile;
    private File outFile;
    private PropertyContainer configuration = new PropertyContainer(Configuration.class);
    private PropertyContainer exportConfiguration = new PropertyContainer(ExportConfiguration.class);
    
    public void setOut(String file) {
        outFile = new File(file);
        if (outFile.exists()) {
            throw new BuildException("Output file or directory '" + file + "' already exists");
        }
    }
    
    public void setIn(String file) {
        inFile = new File(file);
        if (!inFile.exists()) {
            throw new BuildException("Input file '" + file + "' does not exist");
        }
    }
    
    public PropertyContainer createConfiguration() {
        return configuration;
    }
    
    public PropertyContainer createExportConfiguration() {
        return exportConfiguration;
    }
    
    @Override
    public void execute() {
        try {
            if (inFile == null) { throw new BuildException("Input file not defined"); }
            if (inFile == null) { throw new BuildException("Output file not defined"); }
            ODT odt = new ODT(inFile);
            Configuration config = odt.getConfiguration();
            ExportConfiguration exportConfig = new ExportConfiguration();
            configuration.applyCommandsTo(config);
            exportConfiguration.applyCommandsTo(exportConfig);
          //ODT2PEFConverter.setLiblouisLocation();
            PEF pef = ODT2PEFConverter.convert(odt, exportConfig, null, null);
            pef.getSinglePEF().renameTo(outFile);
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class PropertyContainer {
    
        private List<Command> commands = new ArrayList<Command>();
        protected Class type;
        
        public PropertyContainer() {}
        
        public PropertyContainer(Class type) {
            this.type = type;
        }
        
        public SetProperty createSet() {
            if (type == null) { throw new BuildException("Property not defined"); }
            SetProperty property = new SetProperty(type);
            commands.add(property);
            return property;
        }

        public GetProperty createGet() {
            if (type == null) { throw new BuildException("Property not defined"); }
            GetProperty property = new GetProperty(type);
            commands.add(property);
            return property;
        }
        
        public void applyCommandsTo(Object object) {
            for (Command command : commands) {
                command.applyTo(object);
            }        
        }
    }
    
    public class GetProperty extends PropertyContainer
                          implements Command {
    
        private final Class objectType;
        private Class returnType;
        private Method method;
        
        public GetProperty(Class objectType) {
            this.objectType = objectType;
        }
        
        public void setProperty(String property) {
            PropertyDescriptor desc = BeanInfo.getPropertyDescriptor(objectType, property);
            if (desc == null) { throw new BuildException("Property '" + property + "' not supported by " + objectType.getCanonicalName()); }
            method = desc.getReadMethod();
            type = method.getReturnType().getClass();
        }
       

        public void applyTo(Object object) {
            if (method == null) { throw new BuildException("Property not defined"); }
            try {
                Object returnObject = method.invoke(object, new Object[]{});
                applyCommandsTo(returnObject);
            } catch (BuildException e) {
                throw e;
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
    }
    
    public class SetProperty implements Command {
    
        private final Class objectType;
        private Method method;
        private Object value;
        
        public SetProperty(Class objectType) {
            this.objectType = objectType;
        };
        
        public void setProperty(String property) {
            PropertyDescriptor desc = BeanInfo.getPropertyDescriptor(objectType, property);
            if (desc == null) { throw new BuildException("Property '" + property + "' not supported by " + objectType.getCanonicalName()); }
            method = desc.getWriteMethod();
            if (method == null) { throw new BuildException("Property '" + property + "' is read-only"); }
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void applyTo(Object object) {
            if (method == null) { throw new BuildException("Property not defined"); }
            if (value == null) { throw new BuildException("No value defined for property '" + method.getName() + "'"); }
            try {
                method.invoke(object, new Object[]{value});
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
    }
    
    public interface Command {
        public void applyTo(Object object);
    }
}
