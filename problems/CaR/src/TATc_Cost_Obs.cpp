
//
// TATc_Cost_Obs.cpp
//
// C++ implementation per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Manages Observatory Cost structures for internal calculation.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 02
//

#include "TATc_Cost_Obs.hpp"

using std::vector;
using std::string;
using TATc::Cost::CBS::CostBreakdown;
using TATc::Cost::Obs::SpacecraftCost   ;
using TATc::Cost::Obs::SpacecraftDetails;
using TATc::Cost::Obs::SubsystemDetails ;


// SpacecraftCost constructor
SpacecraftCost::SpacecraftCost( const SpacecraftDetails &s )
   :derived( s ) { // Default copy constructor explicit call
}

// SpacecraftCost SpacecraftDetails plane mutator
void SpacecraftCost::setSpacecraftPlane( int p ) {
   derived.setPlane( p );
}

// SpacecraftCost SpacecraftDetails totalCost mutator
void SpacecraftCost::setSpacecraftTotalCost( const CostBreakdown &c ) {
   derived.setTotalCost( c );
}

// SpacecraftCost SubsystemDetails accumulator
void SpacecraftCost::addSubsystemDetails( const SubsystemDetails &s ) {
   subsystemDetails.push_back( s );
}

// SpacecraftCost SpacecraftDetails accessor
const SpacecraftDetails &SpacecraftCost::getSpacecraftDetails() const {
   return derived;
}

// SpacecraftCost SubsystemDetails accessor
const vector<SubsystemDetails> &SpacecraftCost::getSubsystemDetails() const {
   return subsystemDetails;
}


// SpacecraftDetails constructor
SpacecraftDetails::SpacecraftDetails() { // Default
   setDesign   ( 0  );
   setPlane    ( 0  );
   setDryMass  ( 0. );
   setBusMass  ( 0. );
   setTotalMass( 0. );
   // TotalCost Default

   // Factors default to 1
   setHeritageFactor   ( 1. );
   setReliabilityFactor( 1. );
}

// SpacecraftDetails mutators
void SpacecraftDetails::setDesign   (       int            d ) {
   design    = d;
}
void SpacecraftDetails::setPlane    (       int            p ) {
   plane     = p;
}
void SpacecraftDetails::setDryMass  (       double         m ) {
     dryMass = m;
}
void SpacecraftDetails::setBusMass  (       double         m ) {
     busMass = m;
}
void SpacecraftDetails::setTotalMass(       double         m ) {
   totalMass = m;
}
void SpacecraftDetails::setTotalCost( const CostBreakdown &c ) {
   totalCost = c;
}

// SpacecraftDetails Factor mutators
void SpacecraftDetails::setHeritageFactor   ( double f ) {
      heritageFactor = f;
}
void SpacecraftDetails::setReliabilityFactor( double f ) {
   reliabilityFactor = f;
}

// SpacecraftDetails accessors
      int            SpacecraftDetails::getDesign   () const {
   return design   ;
}
      int            SpacecraftDetails::getPlane    () const {
   return plane    ;
}
      double         SpacecraftDetails::getDryMass  () const {
   return   dryMass;
}
      double         SpacecraftDetails::getBusMass  () const {
   return   busMass;
}
      double         SpacecraftDetails::getTotalMass() const {
   return totalMass;
}
const CostBreakdown &SpacecraftDetails::getTotalCost() const {
   return totalCost;
}

// SpacecraftDetails Factor accessors
double SpacecraftDetails::getHeritageFactor   () const {
   return    heritageFactor;
}
double SpacecraftDetails::getReliabilityFactor() const {
   return reliabilityFactor;
}

// SpacecraftDetails utilities: estimating s/c factors
double SpacecraftDetails::
       estimateHeritageFactorFromTRL    ( int    TRL  ) const {

   // Given TRL, return heritageFactor...
   // From "Space Mission Analysis & Design" Table 20-8 and
   // https://esto.nasa.gov/files/trl_definitions.pdf
   switch ( TRL ) { // Technology Readiness Level
      case 9: return 0.2;
      case 8: return 0.3;
      case 7: return 0.4;
      case 6: return 0.6;
      case 5: return 0.7;
      case 4: return 0.9;
      case 3: return 1.0;
      case 2: return 1.1;
      case 1: return 1.3;

      default: return 1.; // Default for meaningless TRL
   }
}
double SpacecraftDetails::
       estimateReliabilityFactorFromMass( double mass ) const {

   // Given totalMass [kg], return reliabilityFactor...
   // From "Cost Models for Large versus Small Spacecraft" Rasmussen
        if ( mass <=    0 ) // kg (but meaningless)
      return 1.00; // Default for meaningless mass
   else if ( mass <  1000 ) // kg
      return 0.11; // 1 - 0.89
   else if ( mass >= 1000 ) // kg
      return 0.06; // 1 - 0.94
   else
      return 1.; // Impossible (but included for good form)
}

// SpacecraftDetails utility: Subsystem obj Names & Sizing
SubsystemDetails SpacecraftDetails::
                 initializeSubsystem( const string &name ) const {
   SubsystemDetails subsystemDetails; // initialize...
   subsystemDetails.setName( name );

   // Apply factors per presence of propellant:
   bool zeroPropellant = ( busMass == dryMass );

   // Apply sizing to subsystems...
        if ( name == "structure"  )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.318718381100 :
                                 0.288990825700)*dryMass ); // Structure
   else if ( name == "thermal"    )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.035413153500 :
                                 0.032110091700)*dryMass ); // Thermal
   else if ( name == "eps"        )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.409780775700 :
                                 0.371559633000)*dryMass ); // EPS
   else if ( name == "ttc"        )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.057335581800 :
                                 0.051987767600)*dryMass ); // TTC
   else if ( name == "adcs"       )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.075885328800 :
                                 0.068807339400)*dryMass ); // ADCS
   else if ( name == "propulsion" )
      subsystemDetails.setMass( (zeroPropellant ?
                                 0.000000000000 :
                                 0.093272171300)*dryMass ); // Propulsion
   else
      subsystemDetails.setMass( 0. ); // Default

   return subsystemDetails;
}


// SubsystemDetails constructor
SubsystemDetails::SubsystemDetails() { // Default
   setName( ""  );
   setMass(  0. );
   // Cost Default
}

// SubsystemDetails mutators
void SubsystemDetails::setName( const string        &n ) {
   name = n;
}
void SubsystemDetails::setMass(       double         m ) {
   mass = m;
}
void SubsystemDetails::setCost( const CostBreakdown &c ) {
   cost = c;
}

// SubsystemDetails accessors
const string        &SubsystemDetails::getName() const {
   return name;
}
      double         SubsystemDetails::getMass() const {
   return mass;
}
const CostBreakdown &SubsystemDetails::getCost() const {
   return cost;
}

