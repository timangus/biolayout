JAVAH = javah

TARGET_DIR = ../../../target
CLASSES_DIR = $(TARGET_DIR)/classes
NATIVE_DIR = $(TARGET_DIR)/native/$(PLATFORM)
LIB_DIR = $(TARGET_DIR)/classes/Resources/Libraries/$(PLATFORM)

CFLAGS_LOCAL = $(CFLAGS) -O3 -Wall -I$(NATIVE_DIR) -I$(JAVA_HOME)/include/
LDFLAGS_LOCAL = $(LDFLAGS) -shared

CLASSES = \
	org.BioLayoutExpress3D.Expression.ExpressionData \
	org.BioLayoutExpress3D.Network.FRLayout

SLASHED_CLASSES = $(subst .,/,$(CLASSES))
CLASS_FILES = $(SLASHED_CLASSES:%=$(CLASSES_DIR)/%.class)
OBJECTS = $(SLASHED_CLASSES:%=$(NATIVE_DIR)/%.o)
HEADERS = $(SLASHED_CLASSES:%=$(NATIVE_DIR)/%.h)

LIBRARY = $(LIB_DIR)/$(LIB_PREFIX)biolayout.$(LIB_EXT)

all: all-arches
clean: clean-arches

clean-products:
	rm -f $(HEADERS)
	rm -f $(OBJECTS)
	rm -f $(LIBRARY)

$(NATIVE_DIR)/%.o: %.c $(CLASS_FILES)
	$(JAVAH) -classpath $(CLASSES_DIR) -o $(@:%.o=%.h) \
		$(subst /,.,$(@:$(NATIVE_DIR)/%.o=%))
	$(CC) $(CFLAGS_LOCAL) -c -o $@ $<

$(LIBRARY): $(OBJECTS)
	$(CC) $(CFLAGS_LOCAL) $(LDFLAGS_LOCAL) -o $@ $^

library: $(LIBRARY)

linux32:
	@$(MAKE) CC=gcc CFLAGS="-m32 -fPIC" PLATFORM=Linux32 LIB_EXT=so LIB_PREFIX=lib library

linux64:
	@$(MAKE) CC=gcc CFLAGS="-m64 -fPIC" PLATFORM=Linux64 LIB_EXT=so LIB_PREFIX=lib library

mingw32:
	@$(MAKE) CC=i686-w64-mingw32-gcc PLATFORM=Win32 LIB_EXT=dll library

mingw64:
	@$(MAKE) CC=x86_64-w64-mingw32-gcc PLATFORM=Win64 LIB_EXT=dll library

all-arches: linux32 linux64 mingw32 mingw64

clean-linux32:
	@$(MAKE) PLATFORM=linux32 LIB_EXT=so clean-products

clean-linux64:
	@$(MAKE) PLATFORM=linux64 LIB_EXT=so clean-products

clean-mingw32:
	@$(MAKE) PLATFORM=mingw32 LIB_EXT=dll clean-products

clean-mingw64:
	@$(MAKE) PLATFORM=mingw64 LIB_EXT=dll clean-products

clean-arches: clean-linux32 clean-linux64 clean-mingw32 clean-mingw64
