
//
// TATc_CaR_inputs.cpp
//
// C++ implementation of TAT-C SICD Section 7.1.2
//
// Unpacks TAT-C C&R input structures read from a JSON file.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 15
//

#include "TATc_CaR_inputs.hpp"

using Json::Value;
using std::vector;
using std::string;
using TATc::CaR::ins::MasterInput  ;
using TATc::CaR::ins::Constellation;
using TATc::CaR::ins::Spacecraft   ;
using TATc::CaR::ins::Payload      ;
using TATc::CaR::ins::Context      ;
using TATc::CaR::ins::Launch       ;
using TATc::CaR::ins::GroundStation;


// MasterInput constructor
MasterInput::MasterInput( const Value &in )
   :constellation( in["constellation"] ),
    context      ( in["context"      ] ) { // from JSON Value
}

// MasterInput accessors
const Constellation &MasterInput::getConstellation() const {
   return constellation;
}
const Context       &MasterInput::getContext      () const {
   return context      ;
}


// Constellation constructor
Constellation::Constellation( const Value &in ) { // from JSON Value
   designLife = in["designLife"].asDouble();

   const Value &in_sc = in["spacecraft"];
   int sc = 0, size = in_sc.size();
   for ( ; sc < size; sc++ ) {
      Spacecraft s( in_sc[sc] );
      spacecraft.push_back( s );
   }
}

// Constellation accessors
      double              Constellation::getDesignLife() const {
   return designLife;
}
const vector<Spacecraft> &Constellation::getSpacecraft() const {
   return spacecraft;
}


// Spacecraft constructor
Spacecraft::Spacecraft( const Value &in ) { // from JSON Value
   stabilizationType = in["stabilizationType"].asString();
   totalDryMass      = in["totalDryMass"     ].asDouble();
   propellantType    = in["propellantType"   ].asString();
   totalDeltaV       = in["totalDeltaV"      ].asDouble();
   propellantMass    = in["propellantMass"   ].asDouble();

    totalMaxPower = in[ "totalMaxPower"].asDouble();
     peakMaxPower = in[  "peakMaxPower"].asDouble();
   beginLifePower = in["beginLifePower"].asDouble();
     endLifePower = in[  "endLifePower"].asDouble();

   alt  = in["alt" ].asDouble();
   incl = in["incl"].asDouble();
   RAAN = in["RAAN"].asDouble();

   stationKeepingMnvrFreq = in["stationKeepingMnvrFreq"].asDouble();
          atmDragMnvrFreq = in[       "atmDragMnvrFreq"].asDouble();

   canMnvrViaGndCmd   = in["canMnvrViaGndCmd"  ].asBool  ();
   pointingAccuracy   = in["pointingAccuracy"  ].asDouble();
   radiationTolerance = in["radiationTolerance"].asDouble();

   commBandTypes = in["commBandTypes"].asString();

   thermalControlType = in["thermalControlType"].asString();

   launchNumber   = in["launchNumber"  ].asInt();
   launchPriority = in["launchPriority"].asInt();

   isSpare = in["isSpare"].asBool();

   techReadinessLevel = in["techReadinessLevel"].asInt();

   const Value &in_pl = in["payload"];
   int pl = 0, size = in_pl.size();
   for ( ; pl < size; pl++ ) {
      Payload p( in_pl[pl] );
      payload.push_back( p );
   }
}

// Spacecraft singleton accessors
const string &Spacecraft::getStabilizationType () const {
   return stabilizationType ;
}
      double  Spacecraft::getTotalDryMass      () const {
   return totalDryMass      ;
}
const string &Spacecraft::getPropellantType    () const {
   return propellantType    ;
}
      double  Spacecraft::getTotalDeltaV       () const {
   return totalDeltaV       ;
}
      double  Spacecraft::getPropellantMass    () const {
   return propellantMass    ;
}
      double  Spacecraft::getTotalMaxPower     () const {
   return totalMaxPower     ;
}
      double  Spacecraft::getPeakMaxPower      () const {
   return peakMaxPower      ;
}
      double  Spacecraft::getBeginLifePower    () const {
   return beginLifePower    ;
}
      double  Spacecraft::getEndLifePower      () const {
   return endLifePower      ;
}
      double  Spacecraft::getAlt               () const {
   return alt               ;
}
      double  Spacecraft::getIncl              () const {
   return incl              ;
}
      double  Spacecraft::getRAAN              () const {
   return RAAN              ;
}
      bool    Spacecraft::getCanMnvrViaGndCmd  () const {
   return canMnvrViaGndCmd  ;
}
      double  Spacecraft::getPointingAccuracy  () const {
   return pointingAccuracy  ;
}
      double  Spacecraft::getRadiationTolerance() const {
   return radiationTolerance;
}
const string &Spacecraft::getCommBandTypes     () const {
   return commBandTypes     ;
}
const string &Spacecraft::getThermalControlType() const {
   return thermalControlType;
}
      int     Spacecraft::getLaunchNumber      () const {
   return launchNumber      ;
}
      int     Spacecraft::getLaunchPriority    () const {
   return launchPriority    ;
}
      bool    Spacecraft::getIsSpare           () const {
   return isSpare           ;
}
      int     Spacecraft::getTechReadinessLevel() const {
   return techReadinessLevel;
}

// Spacecraft MnvrFrequency accessors
      double  Spacecraft::getStationKeepingMnvrFreq() const {
   return stationKeepingMnvrFreq;
}
      double  Spacecraft::getAtmDragMnvrFreq       () const {
   return        atmDragMnvrFreq;
}

// Spacecraft Payload accessor
const vector<Payload> &Spacecraft::getPayload() const {
   return payload;
}

// Spacecraft operators
bool Spacecraft::operator==( const Spacecraft &rhs ) const {

   // Spacecraft are NOT equal if these fields differ
   if ( stabilizationType != rhs.stabilizationType ||
        totalDryMass      != rhs.totalDryMass      ||
        propellantType    != rhs.propellantType    ||
        totalDeltaV       != rhs.totalDeltaV       ||
        propellantMass    != rhs.propellantMass    )
      return false;

   // Spacecraft NOT equal if Pwr fields differ
   if (  totalMaxPower != rhs. totalMaxPower ||
          peakMaxPower != rhs.  peakMaxPower ||
        beginLifePower != rhs.beginLifePower ||
          endLifePower != rhs.  endLifePower )
      return false;

   // ** Spacecraft equality does not generally depend
   //  on orbital requirements, except for the case of:
   // Spacecraft are NOT equal if radiationTolerance is
   //  altitude-dependent and altitudes differ, or if
   //  specified radiationTolerance fields differ...

   // Both radiationTolerance fields indicate alt-dependence:
   if ( radiationTolerance < 0 && rhs.radiationTolerance < 0 ) {
      if ( alt != rhs.alt ) return false;
   } else // At least one tolerance is a specified rqmt:
      if (radiationTolerance != rhs.radiationTolerance )
         return false;

   // Spacecraft are NOT equal if MnvrFrequency fields differ
   if ( stationKeepingMnvrFreq != rhs.stationKeepingMnvrFreq ||
               atmDragMnvrFreq != rhs.       atmDragMnvrFreq )
      return false;

   // Spacecraft are NOT equal if these fields differ
   if ( canMnvrViaGndCmd   != rhs.canMnvrViaGndCmd   ||
        pointingAccuracy   != rhs.pointingAccuracy   ||
        commBandTypes      != rhs.commBandTypes      ||
        thermalControlType != rhs.thermalControlType ||
        techReadinessLevel != rhs.techReadinessLevel )
      return false;

   // ** Spacecraft equality does not depend on:
   //  launchNumber, launchPriority or if isSpare

   // ** Spacecraft are NOT equal if Payload fields differ
   int p = 0, size =     payload.size(), // Set up test:
          rhs_size = rhs.payload.size();
   if ( size != rhs_size ) return false; // Array-sizes differ
   for ( ; p < size; p++ ) // Any Payload-element is NOT equal
      if ( payload[p] != rhs.payload[p] ) return false;

   return true; // Otherwise, Spacecraft are equal
}


// Payload constructor
Payload::Payload( const Value &in ) { // from JSON Value
   name               = in["name"              ].asString();
   designLife         = in["designLife"        ].asDouble();
   functionType       = in["functionType"      ].asString();
   totalMass          = in["totalMass"         ].asDouble();
   instrumentMass     = in["instrumentMass"    ].asDouble();
   peakPower          = in["peakPower"         ].asDouble();
   techReadinessLevel = in["techReadinessLevel"].asInt   ();
   mounting           = in["mounting"          ].asString();
   apertureDiameter   = in["apertureDiameter"  ].asDouble();
   dataRate           = in["dataRate"          ].asDouble();
}

// Payload accessors
const string &Payload::getName              () const {
   return name              ;
}
      double  Payload::getDesignLife        () const {
   return designLife        ;
}
const string &Payload::getFunctionType      () const {
   return functionType      ;
}
      double  Payload::getTotalMass         () const {
   return totalMass         ;
}
      double  Payload::getInstrumentMass    () const {
   return instrumentMass    ;
}
      double  Payload::getPeakPower         () const {
   return peakPower         ;
}
      int     Payload::getTechReadinessLevel() const {
   return techReadinessLevel;
}
const string &Payload::getMounting          () const {
   return mounting          ;
}
      double  Payload::getApertureDiameter  () const {
   return apertureDiameter  ;
}
      double  Payload::getDataRate          () const {
   return dataRate          ;
}

// Payload operators
bool Payload::operator==( const Payload &rhs ) const {

   // Payloads are NOT equal if required fields differ
   if ( name               != rhs.name               ||
        designLife         != rhs.designLife         ||
        totalMass          != rhs.totalMass          ||
        instrumentMass     != rhs.instrumentMass     ||
        peakPower          != rhs.peakPower          ||
        techReadinessLevel != rhs.techReadinessLevel ||
        mounting           != rhs.mounting           )
      return false;

   // Payloads are NOT equal if relevant optional fields differ
   if ( (apertureDiameter > 0 || rhs.apertureDiameter > 0) &&
         apertureDiameter     != rhs.apertureDiameter      )
      return false;
   if ( (dataRate         > 0 || rhs.dataRate         > 0) &&
         dataRate             != rhs.dataRate              )
      return false;

   return true; // Otherwise, Payloads are equal
}


// Context constructor
Context::Context( const Value &in )
   :launch( in["launch"] ) { // from JSON Value
   missionDirector = in["missionDirector"].asString();
   fiscalYear      = in["fiscalYear"     ].asInt   ();

   const Value &in_gs = in["groundStation"];
   int gs = 0, size = in_gs.size();
   for ( ; gs < size; gs++ ) {
      GroundStation g( in_gs[gs] );
      groundStation.push_back( g );
   }
}

// Context singleton accessors
const string &Context::getMissionDirector() const {
   return missionDirector;
}
      int     Context::getFiscalYear     () const {
   return fiscalYear     ;
}
const Launch &Context::getLaunch         () const {
   return launch         ;
}

// Context GroundStation accessor
const vector<GroundStation> &Context::getGroundStations() const {
   return groundStation;
}


// Launch constructors
Launch::Launch() { // Default: used only to initialize copies
   site        = "";
   year        = 0 ;
   vehicle     = "";
   totalNumber = 0 ;
   frequency   = 0.;
   reliability = 0.;
}
Launch::Launch( const Value &in ) { // from JSON Value
   site        = in["site"       ].asString();
   year        = in["year"       ].asInt   ();
   vehicle     = in["vehicle"    ].asString();
   totalNumber = in["totalNumber"].asInt   ();
   frequency   = in["frequency"  ].asDouble();
   reliability = in["reliability"].asDouble();
}

// Launch accessors
const string &Launch::getSite       () const {
   return site       ;
}
      int     Launch::getYear       () const {
   return year       ;
}
const string &Launch::getVehicle    () const {
   return vehicle    ;
}
      int     Launch::getTotalNumber() const {
   return totalNumber;
}
      double  Launch::getFrequency  () const {
   return frequency  ;
}
      double  Launch::getReliability() const {
   return reliability;
}


// GroundStation constructor
GroundStation::GroundStation( const Value &in ) { // from JSON Value
   lat           = in["lat"          ].asDouble();
   lon           = in["lon"          ].asDouble();
   isDesignated  = in["isDesignated" ].asBool  ();
   commBandTypes = in["commBandTypes"].asString();
}

// GroundStation accessors
      double  GroundStation::getLat          () const {
   return lat          ;
}
      double  GroundStation::getLon          () const {
   return lon          ;
}
      bool    GroundStation::getIsDesignated () const {
   return isDesignated ;
}
const string &GroundStation::getCommBandTypes() const {
   return commBandTypes;
}

