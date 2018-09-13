
//
// TATc_CostRisk.cpp
//
// C++ implementation per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// }>}> TAT-C Cost & Risk Module CLI Driver <{<{
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
// Call: <path/to/>TATc_CostRisk [option]
// TBD option selected by option; writes to file
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 02
//

#include <fstream>
#include "TATc_Cost_DSM.hpp"

using Json::Value;
using std::vector;
using std::string;
using std::ifstream;
using std::ofstream;
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
using TATc::Cost::DSM::ConstellationCost   ;
using TATc::Cost::DSM::ConstellationDetails;

void          processRequest   ( const MasterInput          & );
void          processingDetails(       ConstellationCost    & );
CostBreakdown   learningCurve  ( const ConstellationDetails & );
CostBreakdown    subsystemTotal( const ConstellationDetails &,
                                 const string               & );


int main() { // Cost & Risk Module

   Value v; // Build from read Value
   ifstream fin( "bin/CostRisk.json" );
   fin >> v; // Read entire file
   MasterInput masterInput( v );

   // Process MasterInput req.
   processRequest( masterInput );

   return 0;
}


void processRequest( const MasterInput &masterInput ) {

   // Make ConstellationCost object from MasterInput:
   ConstellationCost  constellationCost( masterInput );
   processingDetails( constellationCost ); // Process...
   const // Ref to results from processing
   ResultOutput &resultOutput = constellationCost.getResultOutput();

   // Writing Default Result to JSON file
   ofstream fout( "bin/CostRisk_Default.json" );
   Value v; //  result Value
   resultOutput.buildJSON( v );
   fout << v << "\n\n"; // Write result
}


void processingDetails( ConstellationCost &constellationCost ) {

   const // Ref: extract details, then apply various summations...
   ConstellationDetails &derived = constellationCost.getDetails();
   constellationCost.setHardware (  learningCurve( derived              ) );
   constellationCost.setIAT      ( subsystemTotal( derived, "iat"       ) );
   constellationCost.setProgram  ( subsystemTotal( derived, "program"   ) );
   constellationCost.setGround   ( subsystemTotal( derived, "ground"    ) );
   constellationCost.setLaunchOps( subsystemTotal( derived, "launchOps" ) );
   constellationCost.setOperating(); // derive Ops Costs from the others
}


CostBreakdown learningCurve( const ConstellationDetails &derived ) {

   const // Ref to vector of all s/c costing objects (including copies)
   vector<SpacecraftCost> &spacecraftCost = derived.getSpacecraftCosts();
   CostBreakdown cost( derived.getFiscalYear() ); // output CBS, init FY
   CostEstimate  rdte, rdte_summation, // RDTE costs
                 tfu ,  tfu_summation; // TFU
   double hf = 0, lf = 0; // learning factors, # copies, s/c:
   int d = 0,  designs = derived.getDesigns(), nc = 0, sc = 0;
   for ( ; d < designs; d++ ) { // each design:
      nc = derived.nSpacecraftWithDesign( d );
      sc = derived.aSpacecraftWithDesign( d );
      hf = spacecraftCost[sc].getSpacecraftDetails()
                             .getHeritageFactor   ();

      // learning factor by # copies:
           if ( nc <  10 ) lf = 0.95;
      else if ( nc <= 50 ) lf = 0.90;
      else                 lf = 0.85;
     
      hf *= derived.getInflationFactor();
      lf *= derived.getInflationFactor(); // apply inflation factor &
      const CostBreakdown &spacecraftCost_sc = // accumulate design...
                           spacecraftCost[sc].getSpacecraftDetails()
                                             .getTotalCost        ();
      rdte =               spacecraftCost_sc .getRDTE             ();
      rdte.setEstimate     ( rdte.getEstimate     ()*nc*hf         );
      rdte.setStandardError( rdte.getStandardError()*nc*hf         );
      cost.applySummation  ( rdte,  rdte_summation, rdte_summation );
      tfu  =               spacecraftCost_sc .getTFU              ();
      tfu .setEstimate     ( tfu .getEstimate     ()*nc*lf         );
      tfu .setStandardError( tfu .getStandardError()*nc*lf         );
      cost.applySummation  ( tfu ,   tfu_summation,  tfu_summation );
   }

   // Return summation over s/c designs...
   cost.setRDTE( rdte_summation.getEstimate     (),
                 rdte_summation.getStandardError() );
   cost.setTFU (  tfu_summation.getEstimate     (),
                  tfu_summation.getStandardError() );
   return cost;
}


CostBreakdown subsystemTotal( const ConstellationDetails &derived  ,
                              const string               &subsystem ) {

   const // Ref to vector of all s/c costing objects (including copies)
   vector<SpacecraftCost> &spacecraftCost = derived.getSpacecraftCosts();
   CostBreakdown cost( derived.getFiscalYear() ); // output CBS, init FY
   CostEstimate  rdte, rdte_summation, // RDTE costs
                 tfu ,  tfu_summation; // TFU
   double cf = 0; // cost-inflation factor,  # copies, s/c:
   int d = 0,  designs = derived.getDesigns(), nc = 0, sc = 0;
   for ( ; d < designs; d++ ) { // each design:
      nc = derived.nSpacecraftWithDesign( d );
      sc = derived.aSpacecraftWithDesign( d );

      // Note: logic here requires named subsystem be
      //       found on every spacecraft (cost model)...
      const vector<SubsystemDetails> &subsystemDetails =
                            spacecraftCost[sc].getSubsystemDetails();
      int sdi = 0,  size = subsystemDetails.size(),  found = 0, sd = 0;
      for ( ; sdi < size && found == 0; sdi++ ) //  !found --> zeroth!
         if ( subsystemDetails[sdi].getName() ==
              subsystem                          ) { found = 1  ;
                                                        sd = sdi; }

      cf = derived.getInflationFactor(); // extract inflation factor &
      const CostBreakdown &subsystemCost_sd = //  accumulate design...
                           subsystemDetails[sd].getCost           ();
      rdte =               subsystemCost_sd    .getRDTE           ();
      rdte.setEstimate     ( rdte.getEstimate     ()*nc*cf         );
      rdte.setStandardError( rdte.getStandardError()*nc*cf         );
      cost.applySummation  ( rdte,  rdte_summation, rdte_summation );
      tfu  =               subsystemCost_sd    .getTFU            ();
      tfu .setEstimate     ( tfu .getEstimate     ()*nc*cf         );
      tfu .setStandardError( tfu .getStandardError()*nc*cf         );
      cost.applySummation  ( tfu ,   tfu_summation,  tfu_summation );
   }
   if ( subsystem == "launchOps" ) { // Add launch cost...
      cf = derived.getInflationFactor(); // extract inflation factor
      tfu .setEstimate     ( derived.estimateLaunchCost()*cf       );
      tfu .setStandardError(                            0*cf       );
      cost.applySummation  ( tfu ,   tfu_summation,  tfu_summation );
   }

   // Return summation over s/c designs...
   cost.setRDTE( rdte_summation.getEstimate     (),
                 rdte_summation.getStandardError() );
   cost.setTFU (  tfu_summation.getEstimate     (),
                  tfu_summation.getStandardError() );
   return cost;
}

