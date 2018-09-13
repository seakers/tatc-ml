
# Project : TAT-C Cost & Risk (C&R=CaR) C++ Module
# Author  : Matt Holland (NASA/GSFC Code 587)
# Modified: 2016 Dec 07


# Define macros for locations and final products
BASE_ROOT   = src
TEST_ROOT   = unitTest
BINDIR      = bin
EXECUTABLE  = $(BINDIR)/TATc_CostRisk
INPUT_TEST  = $(BINDIR)/TATc_CaR_inputs_unitTest
OUTPUT_TEST = $(BINDIR)/TATc_CaR_outputs_unitTest
BRKDWN_TEST = $(BINDIR)/TATc_Cost_CBS_unitTest
OBSCST_TEST = $(BINDIR)/TATc_Cost_Obs_unitTest
DSMCST_TEST = $(BINDIR)/TATc_Cost_DSM_unitTest

# Define macros for compilation options
# TEST_FLAGS = -DUSE_64_BIT_LONGS -D__CONSOLE_APP__
TEST_FLAGS = -DUSE_64_BIT_LONGS -D__DEBUG__ -D__CONSOLE_APP__

OPTIMIZATIONS = -O3 -funroll-loops

# Define macros for the needed includes
HEADERS = -I$(BASE_ROOT) \
          -I$(BASE_ROOT)/json

JSON_HDRS = $(BASE_ROOT)/json/json.h \
            $(BASE_ROOT)/json/json-forwards.h

DRVR_HDRS = $(JSON_HDRS) \
            $(BASE_ROOT)/TATc_CaR_inputs.hpp \
            $(BASE_ROOT)/TATc_CaR_outputs.hpp \
            $(BASE_ROOT)/TATc_Cost_CBS.hpp \
            $(BASE_ROOT)/TATc_Cost_Obs.hpp \
            $(BASE_ROOT)/TATc_Cost_DSM.hpp

# Define macros for driver/object files
DRIVER = \
   $(BASE_ROOT)/TATc_CostRisk.o

IN_TST_DRVR = \
   $(TEST_ROOT)/TATc_CaR_inputs_unitTest.o

OUT_TST_DRVR = \
   $(TEST_ROOT)/TATc_CaR_outputs_unitTest.o

CBS_TST_DRVR = \
   $(TEST_ROOT)/TATc_Cost_CBS_unitTest.o

OBS_TST_DRVR = \
   $(TEST_ROOT)/TATc_Cost_Obs_unitTest.o

DSM_TST_DRVR = \
   $(TEST_ROOT)/TATc_Cost_DSM_unitTest.o

JSON_OBJS = \
   $(BASE_ROOT)/jsoncpp.o

OBJECTS = \
   $(BASE_ROOT)/TATc_CaR_inputs.o \
   $(BASE_ROOT)/TATc_CaR_outputs.o \
   $(BASE_ROOT)/TATc_Cost_CBS.o \
   $(BASE_ROOT)/TATc_Cost_Obs.o \
   $(BASE_ROOT)/TATc_Cost_DSM.o

ALL_OBJS = $(JSON_OBJS) $(OBJECTS)

# Specify compiler commands, w/ options
CPPFLAGS = $(OPTIMIZATIONS) $(TESTFLAGS) -Wall -g -std=gnu++11
CPP = g++

# Specify link-specific flags
ifeq ($(wildcard /usr/lib/libstdc++.6.dylib),)
LINK_FLAGS =   
else
LINK_FLAGS = /usr/lib/libstdc++.6.dylib \
             -headerpad_max_install_names \
             -lm -v 
endif

# Object modules for library
$(DRIVER): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(IN_TST_DRVR): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(OUT_TST_DRVR): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(CBS_TST_DRVR): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(OBS_TST_DRVR): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(DSM_TST_DRVR): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(JSON_OBJS): %.o: %.cpp $(JSON_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

$(OBJECTS): %.o: %.cpp $(DRVR_HDRS)
	$(CPP) $(CPPFLAGS) $(HEADERS) -c -o $@ $<

# Default suffix rule
.SUFFIXES: .o .cpp


# Target definitions
.DEFAULT_GOAL := all
.PHONY: all testCases ioTests inTests clean bare

all: $(EXECUTABLE) testCases

$(EXECUTABLE): $(DRIVER) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(DRIVER) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(EXECUTABLE)

testCases: ioTests inTests

ioTests: $(INPUT_TEST) $(OUTPUT_TEST)

inTests: $(BRKDWN_TEST) $(OBSCST_TEST) $(DSMCST_TEST)

$(INPUT_TEST): $(IN_TST_DRVR) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(IN_TST_DRVR) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(INPUT_TEST)

$(OUTPUT_TEST): $(OUT_TST_DRVR) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(OUT_TST_DRVR) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(OUTPUT_TEST)

$(BRKDWN_TEST): $(CBS_TST_DRVR) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(CBS_TST_DRVR) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(BRKDWN_TEST)

$(OBSCST_TEST): $(OBS_TST_DRVR) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(OBS_TST_DRVR) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(OBSCST_TEST)

$(DSMCST_TEST): $(DSM_TST_DRVR) $(ALL_OBJS)
	@if [ ! -x $(BINDIR) ]; then mkdir $(BINDIR); fi
	$(CPP) $(CPPFLAGS) $(LINK_FLAGS) $(DSM_TST_DRVR) $(ALL_OBJS) -o $@
	@chmod 755 $(BINDIR) $(DSMCST_TEST)

clean:
	rm -f $(ALL_OBJS) core $(BINDIR)/CostRisk_Default.json
	rm -f $(DRIVER) $(IN_TST_DRVR) $(OUT_TST_DRVR)
	rm -f $(CBS_TST_DRVR) $(OBS_TST_DRVR) $(DSM_TST_DRVR)

bare: clean
	rm -f $(EXECUTABLE) $(INPUT_TEST) $(OUTPUT_TEST)
	rm -f $(BRKDWN_TEST) $(OBSCST_TEST) $(DSMCST_TEST)

