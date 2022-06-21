M2_HOME := $(HOME)/.m2/repository

LOUISUTDML_VERSION := 2.11.0-p1-SNAPSHOT
LOUIS_VERSION := 3.21.0-p1

rwildcard = $(shell [ -d $1 ] && find $1 -type f -name '$2' | sed 's/ /\\ /g')

install : \
		$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-MacOSX-gpp-shared.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-x86_64-MacOSX-gpp-shared.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-x86_64-MacOSX-gpp-executable.nar
	mvn clean install -Pmacosx_x86_64

# Maven can not automatically resolve these dependencies
$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar : \
		utils/oxt-maven-plugin/pom.xml \
		$(call rwildcard,utils/oxt-maven-plugin/src/main/,*)
	cd $(dir $<) && mvn clean install

$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar : \
		utils/l10n-maven-plugin/pom.xml \
		$(call rwildcard,utils/l10n-maven-plugin/src/main/,*)
	cd $(dir $<) && mvn clean install

$(M2_HOME)/com/github/maven-nar/nar-maven-plugin/3.5.3-SNAPSHOT/nar-maven-plugin-3.5.3-SNAPSHOT.jar : \
		utils/nar-maven-plugin/pom.xml \
		$(call rwildcard,utils/nar-maven-plugin/src/main/,*)
	cd $(dir $<) && mvn clean install

ifneq (,$(findstring -SNAPSHOT,$(LOUIS_VERSION)))
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-MacOSX-gpp-shared.nar : \
		libs/liblouis/pom.xml \
		$(call rwildcard,libs/liblouis/src/,*)
	make -C $(dir $<) clean compile-macosx install
else
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-MacOSX-gpp-shared.nar :
	mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get \
		-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:x86_64-MacOSX-gpp-shared
endif

$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-x86_64-MacOSX-gpp-shared.nar \
$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-x86_64-MacOSX-gpp-executable.nar : \
		libs/liblouisutdml/pom.xml \
		$(call rwildcard,libs/liblouisutdml/src/,*) \
		$(M2_HOME)/com/github/maven-nar/nar-maven-plugin/3.5.3-SNAPSHOT/nar-maven-plugin-3.5.3-SNAPSHOT.jar
	make -C $(dir $<) clean compile-macosx install

install-windows : \
		$(M2_HOME)/be/docarch/oxt-maven-plugin/1.0-SNAPSHOT/oxt-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/be/docarch/l10n-maven-plugin/1.0-SNAPSHOT/l10n-maven-plugin-1.0-SNAPSHOT.jar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar \
		$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-i686-w64-mingw32-gpp-executable.nar
	mvn clean install -Pwindows_x86

ifneq (,$(findstring -SNAPSHOT,$(LOUIS_VERSION)))
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar \
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar : \
		libs/liblouis/pom.xml \
		$(call rwildcard,libs/liblouis/src/,*) \
		$(M2_HOME)/com/github/maven-nar/nar-maven-plugin/3.5.3-SNAPSHOT/nar-maven-plugin-3.5.3-SNAPSHOT.jar
	make -C $(dir $<) clean compile-windows install
else
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar :
	mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get \
		-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:i686-w64-mingw32-gpp-executable
$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-x86_64-w64-mingw32-gpp-executable.nar :
	mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.0:get \
		-Dartifact=org.liblouis:louis:$(LOUIS_VERSION):nar:x86_64-w64-mingw32-gpp-executable
endif

$(M2_HOME)/org/liblouis/louisutdml/$(LOUISUTDML_VERSION)/louisutdml-$(LOUISUTDML_VERSION)-i686-w64-mingw32-gpp-executable.nar : \
		libs/liblouisutdml/pom.xml \
		$(call rwildcard,libs/liblouisutdml/src/,*) \
		$(M2_HOME)/org/liblouis/louis/$(LOUIS_VERSION)/louis-$(LOUIS_VERSION)-i686-w64-mingw32-gpp-executable.nar
	make -C $(dir $<) clean compile-windows install
