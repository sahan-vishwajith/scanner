JC = javac

SRC_DIR = .
AST_DIR = ast
CSEM_DIR = csem

JAVA_FILES := $(wildcard $(SRC_DIR)/*.java) \
              $(wildcard $(CSEM_DIR)/*.java) \
              $(wildcard $(SCANNER_DIR)/*.java)

OUTPUT_DIR = .

OBJ_FILES := $(patsubst %.java, $(OUTPUT_DIR)/%.class, $(JAVA_FILES))

all: $(OBJ_FILES)

$(OUTPUT_DIR)/%.class: %.java
	@mkdir -p $(@D)
	$(JC) -d $(OUTPUT_DIR) $<

clean:
	find . -name "*.class" -type f -delete

.PHONY: all clean