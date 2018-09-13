
//
// TATc_Cost_DSM.hpp
//
// C++ header per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Describes structures used internally by TAT-C Cost analyses.
// Intent is that structures are used for in-memory calculations.
// These structures cover costing overall proposed missions.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 04
//

#ifndef TATC_COST_DSM_HPP
#define TATC_COST_DSM_HPP

#include "TATc_CaR_inputs.hpp"
#include "TATc_Cost_Obs.hpp"

using std::vector;
using std::string;
using TATc::CaR::ins::MasterInput  ;
using TATc::CaR::ins::Constellation;
using TATc::CaR::ins::Spacecraft   ;
using TATc::CaR::ins::Payload      ;
using TATc::CaR::ins::Context      ;
using TATc::CaR::ins::Launch       ;
using TATc::CaR::ins::GroundStation;
using TATc::CaR::outs::ResultOutput  ;
using TATc::CaR::outs::CostEstimate  ;
using TATc::CaR::outs::SpacecraftRank;
using TATc::Cost::CBS::CostBreakdown;
using TATc::Cost::Obs::SpacecraftCost   ;
using TATc::Cost::Obs::SpacecraftDetails;
using TATc::Cost::Obs::SubsystemDetails ;

namespace TATc { // Tradespace Analysis Tool for Constellations
namespace Cost { // Cost & Risk (C&R) Module Costing 
namespace DSM  { // Mission cost structures


class ConstellationDetails { // Derived from constellation cost request
public:
   ConstellationDetails( const MasterInput & ); // from master input ref

   // Singleton accessors
   int getDesigns() const;
   int getPlanes () const;

   // SpacecraftCost accessor
   const vector<SpacecraftCost> &getSpacecraftCosts() const; // Ref

   // DesignLife/FY accessors
   double getDesignLife() const;
   int    getFiscalYear() const;

   // Factor accessors
   double getContextFactor  () const;
   double getInflationFactor() const;

   // Launch accessor
   const Launch &getLaunch() const; // Ref

   // GroundStation accessor
   const vector<GroundStation> &getGroundStations() const; // Ref

   // Utilities: find numbers in s/c details
   int nSpacecraftToBeBuilt  (       ) const;
   int nSpacecraftWithDesign ( int d ) const;
   int aSpacecraftWithDesign ( int d ) const;
   int nSpacecraftFlyingPlane( int p ) const;

   // Utility: estimate cost from optional launchVeh name
   double estimateLaunchCost( const string & = "" ) const;

private:
   void unpackSpacecraft( const MasterInput & ); // unpack utilities
   void unpackContext   ( const MasterInput & ); // for cost request

   // utility methods: for unpackSpacecraft
   void applyCube( const MasterInput &, int ); // Cube Sat. Decisions
   void applySSCM( const MasterInput &, int ); // Small Sat. Cost Model
   void applyUSCM( const MasterInput &, int ); // Unmanned Space Veh. CM
   void applyNICM( const MasterInput &, int ); // NASA Instrument CM

   // utility methods: for unpackContext
   double estimateInflationRatioForFY( int ) const;
   double   lookUpInflationRatioForFY( int ) const;

   int designs; // # Unique designs for s/c (M)
   int planes ; // # Orbital planes flown

   vector<SpacecraftCost> spacecraftCost; // size = N, indexed (input order)

   double designLife; // extracted from master input cost request [months]
   int    fiscalYear; // extracted [FY]

   double   contextFactor; // derived from Mission Director
   double inflationFactor; // derived from Fiscal Year req'd

   Launch                launch       ; // extracted from master
   vector<GroundStation> groundStation; // input cost req context
};


class ConstellationCost { // Results of costing proposed constellation
public:
   ConstellationCost( const MasterInput & ); // from master input ref

   // Mutators
   void setHardware ( const CostBreakdown & ); // Ref
   void setIAT      ( const CostBreakdown & ); // Ref
   void setProgram  ( const CostBreakdown & ); // Ref
   void setGround   ( const CostBreakdown & ); // Ref
   void setLaunchOps( const CostBreakdown & ); // Ref
   void setOperating(); // derive Ops from others

   // Accessors
   const ConstellationDetails &getDetails     () const; // Ref
   const ResultOutput         &getResultOutput()      ; // Ref
   // Note: total/ranking utilities modify result when accessed

private:
   void totalNonRecurring(); // total utilities
   void totalRecurring   (); // for result output
   void totalLifecycle   ();
   void finalRankings    (); // ranking utilitiy

   ConstellationDetails derived  ; // from unpacking constellation cost req
   CostBreakdown        hardware ; // hardware  cost breakdown
   CostBreakdown        iat      ; // iat       cost breakdown
   CostBreakdown        program  ; // program   cost breakdown
   CostBreakdown        ground   ; // ground    cost breakdown
   CostBreakdown        launchOps; // launchOps cost breakdown
   ResultOutput         resultOutput; // overall result output
};


} // namespace DSM
} // namespace Cost
} // namespace TATc

#endif

