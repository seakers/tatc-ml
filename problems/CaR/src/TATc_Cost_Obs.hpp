
//
// TATc_Cost_Obs.hpp
//
// C++ header per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Describes structures used internally by TAT-C Cost analyses.
// Intent is that structures are used for in-memory calculations.
// These structures focus on costing individual observatories.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 15
//

#ifndef TATC_COST_OBS_HPP
#define TATC_COST_OBS_HPP

#include <vector>
#include <string>
#include "TATc_Cost_CBS.hpp"

using std::vector;
using std::string;
using TATc::Cost::CBS::CostBreakdown;

namespace TATc { // Tradespace Analysis Tool for Constellations
namespace Cost { // Cost & Risk (C&R) Module Costing 
namespace Obs  { // Observatory cost structures


class SubsystemDetails { // Derived from costing s/c subsystems
public:
   SubsystemDetails(); // Default constructor

   // Mutators
   void setName( const string        & ); // Ref
   void setMass(       double          );
   void setCost( const CostBreakdown & ); // Ref

   // Accessors
   const string        &getName() const; // Ref
         double         getMass() const;
   const CostBreakdown &getCost() const; // Ref

private:
   string        name; // usually one from list of examples below
   double        mass; // mass of this subsystem [kg]
   CostBreakdown cost; // CBS for this subsystem
};


class SpacecraftDetails { // Derived from cost request for each s/c
public:
   SpacecraftDetails(); // Default constructor

   // Mutators
   void setDesign   (       int             );
   void setPlane    (       int             );
   void setDryMass  (       double          );
   void setBusMass  (       double          );
   void setTotalMass(       double          );
   void setTotalCost( const CostBreakdown & ); // Ref

   // Factor mutators
   void setHeritageFactor   ( double );
   void setReliabilityFactor( double );

   // Accessors
         int            getDesign   () const;
         int            getPlane    () const;
         double         getDryMass  () const;
         double         getBusMass  () const;
         double         getTotalMass() const;
   const CostBreakdown &getTotalCost() const; // Ref

   // Factor accessors
   double getHeritageFactor   () const;
   double getReliabilityFactor() const;

   // Utilities: estimating s/c factors
   double estimateHeritageFactorFromTRL    ( int    ) const;
   double estimateReliabilityFactorFromMass( double ) const;

   // Utility: SubsystemDetails object names & sizing
   SubsystemDetails initializeSubsystem( const string & ) const;

private:
   int           design   ; // index of unique s/c design
   int           plane    ; // derived orbital plane index
   double          dryMass; // from master cost request [kg]
   double          busMass; // s/c bus with propellant [kg]
   double        totalMass; // spacecraft with payload [kg]
   CostBreakdown totalCost; // overall CBS for spacecraft

   double    heritageFactor; // derived from s/c TRL
   double reliabilityFactor; // derived from s/c mass
};


class SpacecraftCost { // Results of costing each proposed spacecraft
public:
   SpacecraftCost( const SpacecraftDetails & ); // derived details ref

   // SpacecraftDetails plane mutator
   void setSpacecraftPlane( int );

   // SpacecraftDetails totalCost mutator
   void setSpacecraftTotalCost( const CostBreakdown & ); // Ref

   // SubsystemDetails accumulator
   void addSubsystemDetails( const SubsystemDetails & ); // Ref

   // SpacecraftDetails accessor
   const SpacecraftDetails &getSpacecraftDetails() const; // Ref

   // SubsystemDetails accessor
   const vector<SubsystemDetails> &getSubsystemDetails() const; // Ref

private:
   SpacecraftDetails derived; // from unpacking spacecraft cost request

   vector<SubsystemDetails> subsystemDetails; // e.g.: structure, thermal,
                                              //       eps, ttc, adcs,
                                              //       propulsion and
                                              //       payload(s)
                                              // plus: iat, ground &
                                              //       launchOps
};


} // namespace Obs
} // namespace Cost
} // namespace TATc

#endif

