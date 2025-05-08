include utils/java-shell-for-make/enable-java-shell.mk

ifeq ($(OS), WINDOWS)
MVN := mvn.cmd
else
MVN := mvn
endif

mvn := try { \
           System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "true"); \
           List<String> cmd = new ArrayList<>(); \
           for (String x : "$(MVN)".split("\\s+")) cmd.add(x); \
           for (String x : commandLineArgs) cmd.add(x); \
           exec(cmd); } \
       finally { \
           System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "false"); }

M2_HOME := $(shell \
	exitOnError( \
		/* cd into random directory without pom.xml in order to force Maven "stub" project */ \
		captureOutput(err::println, \
		              Files.createTempDirectory("mvn-stub-").toFile(), \
		              "$(CURDIR)/$(SHELL)", $(call quote-for-java,$(mvn)), "--", \
		              "org.apache.maven.plugins:maven-help-plugin:3.4.0:effective-settings", \
		              "-Doutput=$(CURDIR)/effective-settings.xml")); \
	println( \
		xpath(new File("effective-settings.xml"), \
		      "/*/*[local-name()='localRepository']/text()") \
		     .replace('\\', '/')); \
)

LOUISUTDML_VERSION := 2.11.0-p1
LOUIS_VERSION := 3.21.0-p2

rwildcard = $(shell if (new File("$1").isDirectory()) glob("$1**/$2").forEach(x -> println(x.getPath().replace('\\', '/').replace(" ", "\\ ")));)

install : \
		$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-aarch64-MacOSX-gpp-shared.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-aarch64-MacOSX-gpp-shared.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-aarch64-MacOSX-gpp-executable.nar
	exec("$(SHELL)", $(call quote-for-java,$(mvn)), "--", "clean", "install", "-Pmacosx_aarch64");

# Maven can not automatically resolve these dependencies
$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar : \
		utils/oxt-maven-plugin/pom.xml \
		$(call rwildcard,utils/oxt-maven-plugin/src/main/,*)
	exec(new File("$(dir $<)"), "$(CURDIR)/$(SHELL)", $(call quote-for-java,$(mvn)), "--", "clean", "install");

$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar : \
		utils/l10n-maven-plugin/pom.xml \
		$(call rwildcard,utils/l10n-maven-plugin/src/main/,*)
	exec(new File("$(dir $<)"), "$(CURDIR)/$(SHELL)", $(call quote-for-java,$(mvn)), "--", "clean", "install");

$(M2_HOME)/com/github/maven-nar/nar-maven-plugin/3.5.3-SNAPSHOT/nar-maven-plugin-3.5.3-SNAPSHOT.jar : \
		utils/nar-maven-plugin/pom.xml \
		$(call rwildcard,utils/nar-maven-plugin/src/main/,*)
	exec(new File("$(dir $<)"), "$(CURDIR)/$(SHELL)", $(call quote-for-java,$(mvn)), "--", "clean", "install");

ifneq (,$(findstring -SNAPSHOT,$(LOUIS_VERSION)))
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-aarch64-MacOSX-gpp-shared.nar : \
		libs/liblouis/pom.xml \
		$(call rwildcard,libs/liblouis/src/,*)
	exec("$(MAKE)", "-C", "$(dir $<)", "clean", "compile-macosx", "install");
else
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-aarch64-MacOSX-gpp-shared.nar :
	exec("$(SHELL)", $(call quote-for-java,$(mvn)), "--", \
	     "org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get", \
	     "-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:aarch64-MacOSX-gpp-shared");
endif

ifneq (,$(findstring -SNAPSHOT,$(LOUISUTDML_VERSION)))
$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-aarch64-MacOSX-gpp-shared.nar \
$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-aarch64-MacOSX-gpp-executable.nar : \
		libs/liblouisutdml/pom.xml \
		$(call rwildcard,libs/liblouisutdml/src/,*) \
		$(M2_HOME)/com/github/maven-nar/nar-maven-plugin/3.5.3-SNAPSHOT/nar-maven-plugin-3.5.3-SNAPSHOT.jar
	exec("$(MAKE)", "-C", "$(dir $<)", "clean", "compile-macosx", "install");
endif

install-windows : \
		$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-i686-w64-mingw32-gpp-executable.nar
	exec("$(SHELL)", $(call quote-for-java,$(mvn)), "--", "clean", "install", "-Pwindows_x86");

ifneq (,$(findstring -SNAPSHOT,$(LOUIS_VERSION)))
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar \
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar : \
		libs/liblouis/pom.xml \
		$(call rwildcard,libs/liblouis/src/,*)
	exec("$(MAKE)", "-C", "$(dir $<)", "clean", "compile-windows", "install");
else
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar :
	exec("$(SHELL)", $(call quote-for-java,$(mvn)), "--", \
	     "org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get", \
	     "-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:i686-w64-mingw32-gpp-executable");
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar :
	exec("$(SHELL)", $(call quote-for-java,$(mvn)), "--", \
	     "org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get", \
	     "-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:x86_64-w64-mingw32-gpp-executable");
endif

ifneq (,$(findstring -SNAPSHOT,$(LOUISUTDML_VERSION)))
$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-i686-w64-mingw32-gpp-executable.nar : \
		libs/liblouisutdml/pom.xml \
		$(call rwildcard,libs/liblouisutdml/src/,*) \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar
	exec("$(MAKE)", "-C", "$(dir $<)", "clean", "compile-windows", "install");
endif
