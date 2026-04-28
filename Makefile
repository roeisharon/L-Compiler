###############
# DIRECTORIES #
###############
BASEDIR           = $(shell pwd)
JFlex_DIR         = ${BASEDIR}/jflex
CUP_DIR           = ${BASEDIR}/cup
SRC_DIR           = ${BASEDIR}/src
BIN_DIR           = ${BASEDIR}/bin
INPUT_DIR         = ${BASEDIR}/input
OUTPUT_DIR        = ${BASEDIR}/output
EXTERNAL_JARS_DIR = ${BASEDIR}/external_jars
MANIFEST_DIR      = ${BASEDIR}/manifest

#########
# FILES #
#########
JFlex_GENERATED_FILE      = ${SRC_DIR}/Lexer.java
CUP_GENERATED_FILES       = ${SRC_DIR}/Parser.java ${SRC_DIR}/TokenNames.java
JFlex_CUP_GENERATED_FILES = ${JFlex_GENERATED_FILE} ${CUP_GENERATED_FILES}
SRC_FILES                 = ${SRC_DIR}/*.java ${SRC_DIR}/*/*.java
EXTERNAL_JAR_FILES        = ${EXTERNAL_JARS_DIR}/java-cup-11b-runtime.jar
MANIFEST_FILE             = ${MANIFEST_DIR}/MANIFEST.MF
SPIM_PROGRAM              = spim
SPIM_TIMEOUT_SECONDS      = 20

########################
# DEFINITIONS :: JFlex #
########################
JFlex_PROGRAM  = jflex
JFlex_FLAGS    = -q
JFlex_DEST_DIR = ${SRC_DIR}
JFlex_FILE     = ${JFlex_DIR}/LEX_FILE.lex

######################
# DEFINITIONS :: CUP #
######################
CUP_PROGRAM                    = java -jar ${EXTERNAL_JARS_DIR}/java-cup-11b.jar
CUP_FILE                       = ${CUP_DIR}/CUP_FILE.cup
CUP_GENERATED_PARSER_NAME      = Parser
CUP_GENERATED_SYMBOLS_FILENAME = TokenNames

######################
# DEFINITIONS :: CUP #
######################
CUP_FLAGS =                                \
-nowarn                                    \
-parser  ${CUP_GENERATED_PARSER_NAME}      \
-symbols ${CUP_GENERATED_SYMBOLS_FILENAME}

#########################
# DEFINITIONS :: PARSER #
#########################
INPUT    = ${INPUT_DIR}/input.txt
OUTPUT   = ${OUTPUT_DIR}/mips.txt

##########
# TARGET #
##########
all:
	clear
	@echo "*******************************"
	@echo "*                             *"
	@echo "*                             *"
	@echo "* [0] Remove COMPILER program *"
	@echo "*                             *"
	@echo "*                             *"
	@echo "*******************************"
	rm -rf COMPILER
	@echo "\n"
	@echo "************************************************************"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "* [1] Remove *.class files and JFlex-CUP generated files:  *"
	@echo "*                                                          *"
	@echo "*     Lexer.java                                           *"
	@echo "*     Parser.java                                          *"
	@echo "*     TokenNames.java                                      *"
	@echo "*                                                          *"
	@echo "************************************************************"
	rm -rf ${JFlex_CUP_GENERATED_FILES} ${BIN_DIR}/*.class ${BIN_DIR}/*/*.class
	rm -f ${SRC_DIR}/*\ [0-9].java
	@echo "\n"
	@echo "************************************************************"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "* [2] Use JFlex to synthesize Lexer.java from LEX_FILE.lex *"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "************************************************************"
	$(JFlex_PROGRAM) ${JFlex_FLAGS} -d ${JFlex_DEST_DIR} ${JFlex_FILE}
	@echo "\n"
	@echo "*******************************************************************************"
	@echo "*                                                                             *"
	@echo "*                                                                             *"
	@echo "* [3] Use CUP to synthesize Parser.java and TokenNames.java from CUP_FILE.cup *"
	@echo "*                                                                             *"
	@echo "*                                                                             *"
	@echo "*******************************************************************************"
	$(CUP_PROGRAM) ${CUP_FLAGS} -destdir ${SRC_DIR} ${CUP_FILE}
	@echo "\n"
	@echo "********************************************************"
	@echo "*                                                      *"
	@echo "*                                                      *"
	@echo "* [4] Create *.class files from *.java files + CUP JAR *"
	@echo "*                                                      *"
	@echo "*                                                      *"
	@echo "********************************************************"
	mkdir -p ${BIN_DIR}
	javac -cp ${EXTERNAL_JAR_FILES} -d ${BIN_DIR} ${SRC_FILES}
	@echo "\n"
	@echo "***********************************************************"
	@echo "*                                                         *"
	@echo "*                                                         *"
	@echo "* [5] Create a JAR file from from *.class files + CUP JAR *"
	@echo "*                                                         *"
	@echo "*                                                         *"
	@echo "***********************************************************"
	jar cfm COMPILER ${MANIFEST_FILE} -C ${BIN_DIR} .

everything: all
	@echo "\n"
	@echo "*****************************"
	@echo "*                           *"
	@echo "*                           *"
	@echo "* [6] Run resulting program *"
	@echo "*                           *"
	@echo "*                           *"
	@echo "*****************************"
	java -jar COMPILER ${INPUT} ${OUTPUT} > /dev/null
	@echo "\n"
	@echo "****************************************"
	@echo "*                                      *"
	@echo "*                                      *"
	@echo "* [7] Run spim and redirect its output *"
	@echo "*                                      *"
	@echo "*                                      *"
	@echo "****************************************"
	@if command -v ${SPIM_PROGRAM} >/dev/null 2>&1; then \
		if command -v gtimeout >/dev/null 2>&1; then \
			gtimeout ${SPIM_TIMEOUT_SECONDS}s ${SPIM_PROGRAM} -f ${OUTPUT_DIR}/mips.txt > ${OUTPUT_DIR}/MIPS_OUTPUT.txt; \
			status=$$?; \
		else \
			perl -e 'alarm shift; exec @ARGV' ${SPIM_TIMEOUT_SECONDS} ${SPIM_PROGRAM} -f ${OUTPUT_DIR}/mips.txt > ${OUTPUT_DIR}/MIPS_OUTPUT.txt; \
			status=$$?; \
		fi; \
		if [ $$status -ne 0 ]; then \
			echo "SPIM execution timed out/failed (possible infinite loop in generated MIPS)." > ${OUTPUT_DIR}/MIPS_OUTPUT.txt; \
			echo "WARNING: SPIM timed out/failed; check generated mips.txt."; \
		fi; \
	else \
		echo "SPIM not installed. Install spim (or run mips.txt in QtSPIM)" > ${OUTPUT_DIR}/MIPS_OUTPUT.txt; \
		echo "WARNING: SPIM not installed, skipped runtime execution."; \
	fi

clean:
	rm -rf COMPILER ${JFlex_CUP_GENERATED_FILES} ${BIN_DIR}/*.class ${BIN_DIR}/*/*.class

test_smoke: all
	bash ${BASEDIR}/tests/smoke_test.sh

.PHONY: all everything clean test_smoke