@echo off
echo Javadoc
javadoc -private -author -version -breakiterator -d javadoc -subpackages be.docarch:com.versusoft.packages.jodl:org_pef_text:org.daisy:com_braillo:com_indexbraille:com_yourdolphin:de_brailletec:org_daisy:se_tpb -sourcepath ..\src;..\..\DaisyPipeline\src;..\..\JODL\src;..\..\..\brailleutils\src;..\..\..\brailleutils\catalog
@pause
exit