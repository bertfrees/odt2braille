package be.docarch.odt2braille.convert;

/**
 *
 * @author Bert Frees
 */
public interface Converter<I,O> {
    
    public O convert(I input) throws ConversionException;
    
   public void cleanUp();
    
}
