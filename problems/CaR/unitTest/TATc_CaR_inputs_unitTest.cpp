
//
// TATc_CaR_inputs_unitTest.cpp
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Nov 28
//

#include <fstream>
#include <iostream>
#include "TATc_CaR_inputs.hpp"

using std::cout;
using std::endl;
using Json::Value;
using std::vector;
using std::string;
using std::ifstream;
using TATc::CaR::ins::MasterInput  ;
using TATc::CaR::ins::Constellation;
using TATc::CaR::ins::Spacecraft   ;
using TATc::CaR::ins::Payload      ;
using TATc::CaR::ins::Context      ;
using TATc::CaR::ins::Launch       ;
using TATc::CaR::ins::GroundStation;

void showMasterInput  ( const MasterInput   & );
void showConstellation( const Constellation & );
void showSpacecraft   ( const Spacecraft    & );
void showPayload      ( const Payload       & );
void showContext      ( const Context       & );
void showLaunch       ( const Launch        & );
void showGroundStation( const GroundStation & );


int main() { // Unit-testing
    
   Value v; // Build from read Value
   ifstream fin( "bin/CostRisk.json" );
   fin >> v; // Read entire file
   MasterInput masterInput( v );

   // Show all that was read from Value
   cout << "Unit-test of TATc_CaR_inputs" << endl;
   showMasterInput( masterInput );

   return 0;
}


void showMasterInput( const MasterInput &masterInput ) {

   // Extract MasterInput fields
   Constellation constellation = masterInput.getConstellation();
   Context       context       = masterInput.getContext      ();

   // Show MasterInput fields
   cout << "a MasterInput..." << endl;
   showConstellation( constellation );
   showContext      ( context       );
}


void showConstellation( const Constellation &constellation ) {

   // Extract Constellation fields
   double             designLife = constellation.getDesignLife();
   vector<Spacecraft> spacecraft = constellation.getSpacecraft();
   int sc = 0, size = spacecraft.size();

   // Show Constellation fields
   cout << "a Constellation..."
        <<  "\n  designLife:[" << designLife
        << "]\n";
   for ( ; sc < size; sc++ )
      showSpacecraft( spacecraft[sc] );
}


void showSpacecraft( const Spacecraft &spacecraft ) {

   // Extract Spacecraft singleton fields
   string stabilizationType  = spacecraft.getStabilizationType ();
   double totalDryMass       = spacecraft.getTotalDryMass      ();
   string propellantType     = spacecraft.getPropellantType    ();
   double totalDeltaV        = spacecraft.getTotalDeltaV       ();
   double propellantMass     = spacecraft.getPropellantMass    ();
   double totalMaxPower      = spacecraft.getTotalMaxPower     ();
   double peakMaxPower       = spacecraft.getPeakMaxPower      ();
   double beginLifePower     = spacecraft.getBeginLifePower    ();
   double endLifePower       = spacecraft.getEndLifePower      ();
   double alt                = spacecraft.getAlt               ();
   double incl               = spacecraft.getIncl              ();
   double RAAN               = spacecraft.getRAAN              ();
   bool   canMnvrViaGndCmd   = spacecraft.getCanMnvrViaGndCmd  ();
   double pointingAccuracy   = spacecraft.getPointingAccuracy  ();
   double radiationTolerance = spacecraft.getRadiationTolerance();
   string commBandTypes      = spacecraft.getCommBandTypes     ();
   string thermalControlType = spacecraft.getThermalControlType();
   int    launchNumber       = spacecraft.getLaunchNumber      ();
   int    launchPriority     = spacecraft.getLaunchPriority    ();
   bool   isSpare            = spacecraft.getIsSpare           ();
   int    techReadinessLevel = spacecraft.getTechReadinessLevel();

   // Extract Spacecraft MnvrFrequency fields
   double stationKeepingMnvrFreq = spacecraft.getStationKeepingMnvrFreq();
   double        atmDragMnvrFreq = spacecraft.getAtmDragMnvrFreq       ();

   // Extract Spacecraft Payload field
   vector<Payload> payload = spacecraft.getPayload();
   int pl = 0, size = payload.size();

   // Show Spacecraft fields
   cout << "a Spacecraft..."
        <<  "\n  stabilizationType     :[" << stabilizationType
        << "]\n  totalDryMass          :[" << totalDryMass
        << "]\n  propellantType        :[" << propellantType
        << "]\n  totalDeltaV           :[" << totalDeltaV
        << "]\n  propellantMass        :[" << propellantMass
        << "]\n  totalMaxPower         :[" << totalMaxPower
        << "]\n  peakMaxPower          :[" << peakMaxPower
        << "]\n  beginLifePower        :[" << beginLifePower
        << "]\n  endLifePower          :[" << endLifePower
        << "]\n  alt                   :[" << alt
        << "]\n  incl                  :[" << incl
        << "]\n  RAAN                  :[" << RAAN
        << "]\n  stationKeepingMnvrFreq:[" << stationKeepingMnvrFreq
        << "]\n         atmDragMnvrFreq:[" <<        atmDragMnvrFreq
        << "]\n  canMnvrViaGndCmd      :[" << canMnvrViaGndCmd
        << "]\n  pointingAccuracy      :[" << pointingAccuracy
        << "]\n  radiationTolerance    :[" << radiationTolerance
        << "]\n  commBandTypes         :[" << commBandTypes
        << "]\n  thermalControlType    :[" << thermalControlType
        << "]\n  launchNumber          :[" << launchNumber
        << "]\n  launchPriority        :[" << launchPriority
        << "]\n  isSpare               :[" << isSpare
        << "]\n  techReadinessLevel    :[" << techReadinessLevel
        << "]\n";
   for ( ; pl < size; pl++ )
      showPayload( payload[pl] );
}


void showPayload( const Payload &payload ) {

   // Extract Payload fields
   string name               = payload.getName              ();
   double designLife         = payload.getDesignLife        ();
   string functionType       = payload.getFunctionType      ();
   double totalMass          = payload.getTotalMass         ();
   double instrumentMass     = payload.getInstrumentMass    ();
   double peakPower          = payload.getPeakPower         ();
   int    techReadinessLevel = payload.getTechReadinessLevel();
   string mounting           = payload.getMounting          ();
   double apertureDiameter   = payload.getApertureDiameter  ();
   double dataRate           = payload.getDataRate          ();

   // Show Payload fields
   cout << "a Payload..."
        <<  "\n  name              :[" << name
        << "]\n  designLife        :[" << designLife
        << "]\n  functionType      :[" << functionType
        << "]\n  totalMass         :[" << totalMass
        << "]\n  instrumentMass    :[" << instrumentMass
        << "]\n  peakPower         :[" << peakPower
        << "]\n  techReadinessLevel:[" << techReadinessLevel
        << "]\n  mounting          :[" << mounting
        << "]\n  apertureDiameter  :[" << apertureDiameter
        << "]\n  dataRate          :[" << dataRate
        << "]\n";
}


void showContext( const Context &context ) {

   // Extract Context singleton fields
   string missionDirector = context.getMissionDirector();
   int    fiscalYear      = context.getFiscalYear     ();
   Launch launch          = context.getLaunch         ();

   // Extract Context GroundStation field
   vector<GroundStation> groundStation = context.getGroundStations();
   int gs = 0, size = groundStation.size();

   // Show Context fields
   cout << "a Context..."
        <<  "\n  missionDirector:[" << missionDirector
        << "]\n  fiscalYear     :[" << fiscalYear
        << "]\n";
   showLaunch( launch );
   for ( ; gs < size; gs++ )
      showGroundStation( groundStation[gs] );
}


void showLaunch( const Launch &launch ) {

   // Extract Launch fields
   string site        = launch.getSite       ();
   int    year        = launch.getYear       ();
   string vehicle     = launch.getVehicle    ();
   int    totalNumber = launch.getTotalNumber();
   double frequency   = launch.getFrequency  ();
   double reliability = launch.getReliability();

   // Show Launch fields
   cout << "a Launch..."
        <<  "\n  site       :[" << site
        << "]\n  year       :[" << year
        << "]\n  vehicle    :[" << vehicle
        << "]\n  totalNumber:[" << totalNumber
        << "]\n  frequency  :[" << frequency
        << "]\n  reliability:[" << reliability
        << "]\n";
}


void showGroundStation( const GroundStation &groundStation ) {

   // Extract GroundStation fields
   double lat           = groundStation.getLat          ();
   double lon           = groundStation.getLon          ();
   bool   isDesignated  = groundStation.getIsDesignated ();
   string commBandTypes = groundStation.getCommBandTypes();

   // Show GroundStation fields
   cout << "a GroundStation..."
        <<  "\n  lat          :[" << lat
        << "]\n  lon          :[" << lon
        << "]\n  isDesignated :[" << isDesignated
        << "]\n  commBandTypes:[" << commBandTypes
        << "]\n";
}

