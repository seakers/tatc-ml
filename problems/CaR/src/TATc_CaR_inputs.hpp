
//
// TATc_CaR_inputs.hpp
//
// C++ header per TAT-C SICD Section 7.1.2
//
// Describes structures for requesting TAT-C Cost & Risk analysis
// for a proposed Earth Science Spacecraft Constellation mission.
// Intent is that overall structure is read from a JSON file.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 15
//

#ifndef TATC_CAR_INPUTS_HPP
#define TATC_CAR_INPUTS_HPP

#include <json/json.h>
#include <vector>
#include <string>

using Json::Value;
using std::vector;
using std::string;

namespace TATc { // Tradespace Analysis Tool for Constellations
namespace CaR  { // Cost & Risk (C&R) Module
namespace ins  { // Input-interface structures


class Payload { // Instrument payload description
public:
   Payload( const Value & ); // construct from JSON Value const-ref

   // Accessors
   const string &getName              () const; // Ref
         double  getDesignLife        () const;
   const string &getFunctionType      () const; // Ref
         double  getTotalMass         () const;
         double  getInstrumentMass    () const;
         double  getPeakPower         () const;
         int     getTechReadinessLevel() const;
   const string &getMounting          () const; // Ref
         double  getApertureDiameter  () const;
         double  getDataRate          () const;

   // Operators
   bool operator==( const Payload &    ) const;
   bool operator!=( const Payload &rhs ) const { return !(*this==rhs); }

private:
   string name              ; // instrument payload designation (NICM type)
   double designLife        ; // expected on-orbit observing time [months]
   string functionType      ; // required (e.g. 'active', 'passive', etc)
   double totalMass         ; // expected for all related components [kg]
   double instrumentMass    ; // expected for antennas, optics, etc [kg]
   double peakPower         ; // required for total instrument system [W]
   int    techReadinessLevel; // for instrument system (usual 1-9 levels)
   string mounting          ; // location of sensor (e.g. 'body', 'mast',
                              // 'probe'); 'body' used if not recognized

   // Optional parameters -- ignored if non-positive:
   double apertureDiameter; // of science instrument, as relevant [cm]
   double dataRate        ; // expected for science data capture [bps]
};


class Spacecraft { // Model of proposed spacecraft
public:
   Spacecraft( const Value & ); // construct from JSON Value const-ref

   // Singleton accessors
   const string &getStabilizationType () const; // Ref
         double  getTotalDryMass      () const;
   const string &getPropellantType    () const; // Ref
         double  getTotalDeltaV       () const;
         double  getPropellantMass    () const;
         double  getTotalMaxPower     () const;
         double  getPeakMaxPower      () const;
         double  getBeginLifePower    () const;
         double  getEndLifePower      () const;
         double  getAlt               () const;
         double  getIncl              () const;
         double  getRAAN              () const;
         bool    getCanMnvrViaGndCmd  () const;
         double  getPointingAccuracy  () const;
         double  getRadiationTolerance() const;
   const string &getCommBandTypes     () const; // Ref
   const string &getThermalControlType() const; // Ref
         int     getLaunchNumber      () const;
         int     getLaunchPriority    () const;
         bool    getIsSpare           () const;
         int     getTechReadinessLevel() const;

   // MnvrFrequency accessors
         double  getStationKeepingMnvrFreq() const;
         double  getAtmDragMnvrFreq       () const;

   // Payload accessor
   const vector<Payload> &getPayload() const; // Ref

   // Operators
   bool operator==( const Spacecraft &    ) const;
   bool operator!=( const Spacecraft &rhs ) const { return !(*this==rhs); }

private:
   string stabilizationType; // method (e.g. '3axis', 'spinning', etc)
   double totalDryMass     ; // spacecraft without propellant [kg]
   string propellantType   ; // spacecraft uses (e.g. 'monoprop', etc)
   double totalDeltaV      ; // total delta-V requirement [km/s]
   double propellantMass   ; // initial for spacecraft [kg]

   double  totalMaxPower; // expected spacecraft total [W]
   double   peakMaxPower; // extreme  spacecraft total [W]
   double beginLifePower; // required s/c BoL production [W]
   double   endLifePower; // expected s/c EoL production [W]

   double alt ; // required orbital altitude (assuming circular) [km]
   double incl; // required orbital plane inclination [deg]
   double RAAN; // required orbital RAAN element value [deg]

   // Requirements to maintain s/c constellation config:
   double stationKeepingMnvrFreq; // [maneuvers/month]
   double        atmDragMnvrFreq; // [maneuvers/month]

   bool   canMnvrViaGndCmd  ; // can=T, cannot=F avoid s/c collision
   double pointingAccuracy  ; // required for spacecraft attitude [deg]
   double radiationTolerance; // s/c rqmt if non-neg [mRad], else:
                              // indicates orbital alt drives rqmt

   string commBandTypes; // rqmt list of space-delim designators:
                         // ('K', 'S', etc, listed as in: "S X Ka")

   string thermalControlType; // rqmt for s/c ('active', 'passive', etc)

   int launchNumber  ; // expected order in schedule (1=1st, etc)
   int launchPriority; // indicator of any s/c launch priority
                       // aboard its designated launch vehicle:
                       // (0=none, 1=primary, 2=secondary, etc)

   bool isSpare; // is=T, is not=F s/c spare (needed to address failure)

   int techReadinessLevel; // for overall s/c (usual 1-9 levels)

   vector<Payload> payload; // array of all instruments on board s/c
};


class Constellation { // Proposed s/c constellation
public:
   Constellation( const Value & ); // construct from JSON Value const-ref

   // Accessors
         double              getDesignLife() const;
   const vector<Spacecraft> &getSpacecraft() const; // Ref

private:
   double             designLife; // expected on-orbit time [months]
   vector<Spacecraft> spacecraft; // array of observatory specs
};


class Launch { // Vehicle and launch parameters
public:
   Launch(); // Default constructor--all zero/empty fields
             // Note: only used to initialize copies of input data
   Launch( const Value & ); // construct from JSON Value const-ref

   // Accessors
   const string &getSite       () const; // Ref
         int     getYear       () const;
   const string &getVehicle    () const; // Ref
         int     getTotalNumber() const;
         double  getFrequency  () const;
         double  getReliability() const;

private:
   string site; // name (std designation) of intended site
   int    year; // planned year of first (or only) launch:
                // uses (OS value for) current year if given earlier

   string vehicle    ; // name (std designation) of intended vehicle
   int    totalNumber; // # of launches expected for entire constellation
   double frequency  ; // uniform rate for intended vehicle [launch/yr]
   double reliability; // expected success rate [0 <= p <= 1]
};


class GroundStation { // Locations and ops params
public:
   GroundStation( const Value & ); // construct from JSON Value const-ref

   // Accessors
         double  getLat          () const;
         double  getLon          () const;
         bool    getIsDesignated () const;
   const string &getCommBandTypes() const; // Ref

private:
   double lat          ; // Earth latitude [deg (N)]
   double lon          ; // Earth longitude [deg (W)]
   bool   isDesignated ; // is=T, is not=F a designated gnd stn
   string commBandTypes; // rqmt list of space-delim designators:
                         // ('K', 'S', etc, listed as in: "S X Ka")
};


class Context { // Mission context description
public:
   Context( const Value & ); // construct from JSON Value const-ref

   // Singleton accessors
   const string &getMissionDirector() const; // Ref
         int     getFiscalYear     () const;
   const Launch &getLaunch         () const; // Ref

   // GroundStation accessor
   const vector<GroundStation> &getGroundStations() const; // Ref

private:
   string missionDirector; // nature of mission-directing org:
                           // Use: 'Government', 'Military',
                           //      'Commercial', or 'Academic'.
                           // Default: 'Goverment' if not recognized.

   int fiscalYear; // desired year for requested cost estimates:
                   // uses (OS value for) current year if given earlier

   Launch                launch       ; // vehicle info and params
   vector<GroundStation> groundStation; // array of all available
};


class MasterInput { // C&R executable input
public:
   MasterInput( const Value & ); // construct from JSON Value const-ref

   // Accessors
   const Constellation &getConstellation() const; // Ref
   const Context       &getContext      () const; // Ref

private:
   Constellation constellation; // specification down to instrument level
   Context       context      ; // specification of mission context
};


} // namespace ins
} // namespace CaR
} // namespace TATc

#endif

