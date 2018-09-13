
//
// TATc_Cost_DSM.cpp
//
// C++ implementation per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Manages Mission Cost structures for internal calculation.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 04
//

#include <cmath>
#include <algorithm>
#include "TATc_Cost_DSM.hpp"

using std::log;
using std::pow;
using std::find;
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
using TATc::Cost::DSM::ConstellationCost   ;
using TATc::Cost::DSM::ConstellationDetails;


// ConstellationCost constructor
ConstellationCost::ConstellationCost( const MasterInput &in )
   :derived( in ) { // from master input ref
   // Default Costs/Outputs
}

// ConstellationCost mutators
void ConstellationCost::setHardware ( const CostBreakdown &cost ) {
   hardware  = cost;
   resultOutput.setHardwareCost  ( cost.getTotal() );
}
void ConstellationCost::setIAT      ( const CostBreakdown &cost ) {
   iat       = cost;
   resultOutput.setIatCost       ( cost.getTotal() );
}
void ConstellationCost::setProgram  ( const CostBreakdown &cost ) {
   program   = cost;
   resultOutput.setProgramCost   ( cost.getTotal() );
}
void ConstellationCost::setGround   ( const CostBreakdown &cost ) {
   ground    = cost;
   resultOutput.setGroundCost    ( cost.getTotal() );
}
void ConstellationCost::setLaunchOps( const CostBreakdown &cost ) {
   launchOps = cost;
   resultOutput.setLaunchCost    ( cost.getTotal() );
}
void ConstellationCost::setOperating() {
   CostBreakdown  cost( derived.getFiscalYear() );
   int N =     derived.nSpacecraftToBeBuilt();
   double cf = derived.getInflationFactor  (),
          cf_FY2013 = 1.308; // cost-inflation factors
   double      missionClass = 0, primeOperationsEstimate = 0,
             scNonRecurringCost = hardware.getRDTE().getEstimate()
                                                   *(cf_FY2013/cf);
        if ( scNonRecurringCost <   60.0e3 ) missionClass = 0.25;
   else if ( scNonRecurringCost <= 120.0e3 ) missionClass = 1.00;
   else if ( scNonRecurringCost <= 400.0e3 ) missionClass = 2.00;
   else if ( scNonRecurringCost <= 800.0e3 ) missionClass = 3.00;
   else if ( scNonRecurringCost >  800.0e3 ) missionClass = 6.00;
   primeOperationsEstimate = 533.8*pow( missionClass, 0.8868 ) // FY
                                  *pow(            N, 0.1814 ) // k$
                                                    *(cf/cf_FY2013);
   cost.setRDTE(       1.95*primeOperationsEstimate, // Checkouts
                 0.297*1.95*primeOperationsEstimate );
   cost.setTFU (            primeOperationsEstimate, // Prime
                 0.242     *primeOperationsEstimate );
   resultOutput.setOperationsCost( cost.getTotal() );
}

// ConstellationCost accessors
const ConstellationDetails &ConstellationCost::getDetails     () const {
   return derived     ;
}
const ResultOutput         &ConstellationCost::getResultOutput()       {
   totalNonRecurring();
   totalRecurring   ();
   totalLifecycle   ();
   finalRankings    ();
   return resultOutput;
}

// ConstellationCost private utilities
void ConstellationCost::totalNonRecurring() {
   CostBreakdown cost( derived.getFiscalYear() ); // init FY
   CostEstimate  summation;
   cost.applySummation( hardware .getRDTE (),
                             iat .getRDTE (),
                        summation            );
   cost.applySummation( program  .getRDTE (),
                        summation           ,
                        summation            );
   cost.applySummation( ground   .getRDTE (),
                        summation           ,
                        summation            );
   cost.applySummation( launchOps.getRDTE (),
                        summation           ,
                        summation            );
   cost.setRDTE       ( summation.getEstimate     (), // TFU = 0
                        summation.getStandardError() );
   resultOutput.setNonRecurringCost( cost.getTotal() );
}
void ConstellationCost::totalRecurring   () {
   CostBreakdown cost( derived.getFiscalYear() ); // init FY
   CostEstimate  summation;
   cost.applySummation( hardware .getTFU  (),
                             iat .getTFU  (),
                        summation            );
   cost.applySummation( program  .getTFU  (),
                        summation           ,
                        summation            );
   cost.applySummation( ground   .getTFU  (),
                        summation           ,
                        summation            );
   cost.applySummation( launchOps.getTFU  (),
                        summation           ,
                        summation            );
   cost.setTFU        ( summation.getEstimate     (), // RDTE = 0
                        summation.getStandardError() );
   resultOutput.setRecurringCost   ( cost.getTotal() );
}
void ConstellationCost::totalLifecycle   () {
   CostBreakdown cost( derived.getFiscalYear() ); // init FY
   CostEstimate  summation;
   cost.applySummation( hardware .getTotal(),
                             iat .getTotal(),
                        summation            );
   cost.applySummation( program  .getTotal(),
                        summation           ,
                        summation            );
   cost.applySummation( ground   .getTotal(),
                        summation           ,
                        summation            );
   cost.applySummation( launchOps.getTotal(),
                        summation           ,
                        summation            );
   cost.setRDTE       ( summation.getEstimate     (), // TFU = 0
                        summation.getStandardError() ); // (sets Total)
   resultOutput.setLifecycleCost   ( cost.getTotal() );
}
void ConstellationCost::finalRankings    () {
   SpacecraftRank  spacecraftRank; // Default
   const // Ref to spacecraft costing
   vector<SpacecraftCost> &spacecraftCost =
                derived.getSpacecraftCosts();
   int sc = 0,  size = spacecraftCost.size();
   for ( ; sc < size; sc++ ) {
      const // Ref to s/c CostEstimate
      CostEstimate &derived_sc_cost =
      spacecraftCost[sc].getSpacecraftDetails().
                         getTotalCost().getTotal();
      spacecraftRank .setSpacecraftIndex( sc );
      spacecraftRank .setTotalCost      ( // Cost
      derived_sc_cost.getEstimate  ()   );
      spacecraftRank .setFiscalYear     ( // FY
      derived        .getFiscalYear()   );
      spacecraftRank .setInflationFactor( // Factor
      derived        .getInflationFactor() );
      resultOutput.addSpacecraftRank( // Add Rank
      spacecraftRank                );
   }
   resultOutput.setSpacecraftRankings(); // Final
}


// ConstellationDetails constructor
ConstellationDetails::ConstellationDetails( const MasterInput &in ) {
   unpackSpacecraft( in ); // from master input cost request ref
   unpackContext   ( in );
}

// ConstellationDetails private utility
void ConstellationDetails::unpackSpacecraft( const MasterInput &in ) {
   designs = 0; // initialize...
   planes  = 0;

   const // Ref to the spacecraft models from input cost request...
   vector<Spacecraft> &spacecraft = in.getConstellation().getSpacecraft();
   vector<Spacecraft>::const_iterator spacecraftSeen, // with Iterators
                                      spacecraftIter; // (RandomAccess)

          double  dryMass, propellantMass, busMass, payloadMass, totalMass;
          double  incl_i; // an orbital inclination from input cost request
   vector<double> incl  ; // Container for unique incl values
   vector<double>::iterator inclIter; // with Iterator

   bool designSeen; // Has this s/c design been seen?
   SpacecraftDetails  spacecraftDetails_i; // Default
   int i = 0,  size = spacecraft.size()  ; // N s/c:
   for ( ; i < size; i++ ) { // each Spacecraft...
      const // Ref to ith model
      Spacecraft &spacecraft_i = spacecraft[i];
      spacecraftSeen =       spacecraft.begin() + i;
      spacecraftIter = find( spacecraft.begin(), // Search...
                             spacecraftSeen, spacecraft_i );
      if ( spacecraftIter != spacecraftSeen ) { // Seen:
         designSeen = true; // use existing details
         spacecraftCost.push_back( spacecraftCost[
                                   spacecraftIter -
                                   spacecraft.begin()] );
      } else { // Not seen yet: Add new design...
         designSeen = false;
         spacecraftDetails_i.setDesign( designs );
         designs++; // increment to count of seen
      }

      int plane; // orbital plane s/c design to fly in:
      incl_i = spacecraft_i.getIncl(); // Search incls...
      inclIter = find( incl.begin(), incl.end(), incl_i );
      if ( inclIter != incl.end() ) { // Already seen:
         plane = inclIter - incl.begin(); // save ind.
      } else { // Not seen yet:
         incl.push_back( incl_i ); // Add incl:
         plane = planes; // save latest ind.
         planes++; // increment to count of seen
      }

      if ( designSeen ) { // Existing design: set plane
         spacecraftCost[i].setSpacecraftPlane( plane );

      } else { // Design not seen before: continue unpack...
         propellantMass = spacecraft_i.getPropellantMass();
                dryMass = spacecraft_i.getTotalDryMass  ();
                busMass = dryMass + propellantMass;

         payloadMass = 0; // Determine total:
         const vector<Payload> &payload = // Ref to
         spacecraft_i.getPayload(); // model of ith Spacecraft's
         int p = 0,  pSize = payload.size(); // full Payload
         for ( ; p < pSize;  p++ ) // each Payload...
            payloadMass += payload[p].getTotalMass();

         // Add new s/c costing object...
         totalMass = busMass + payloadMass;
         spacecraftDetails_i.setPlane    ( plane     );
         spacecraftDetails_i.setDryMass  (   dryMass );
         spacecraftDetails_i.setBusMass  (   busMass );
         spacecraftDetails_i.setTotalMass( totalMass );
         // Note: total s/c cost set after subsystem costs
         spacecraftDetails_i.setHeritageFactor( // Heritage:
         spacecraftDetails_i.estimateHeritageFactorFromTRL(
         spacecraft_i.getTechReadinessLevel() ));
         spacecraftDetails_i.setReliabilityFactor( // Reliability...
         spacecraftDetails_i.estimateReliabilityFactorFromMass( totalMass ));
         SpacecraftCost spacecraftCost_i( spacecraftDetails_i );
         spacecraftCost.push_back( spacecraftCost_i );
              if ( totalMass <=   20 ) // Then apply one of...
            applyCube( in, i ); //     Cube Satellite Decision Tree
         else if ( totalMass >    20 &&
                   totalMass <  1000 )
            applySSCM( in, i ); //        Small Satellite Cost Model
         else if ( totalMass >= 1000 )
            applyUSCM( in, i ); // Unmanned Space Vehicle Cost Model
      }
   }
}

// Apply Cube Satellite Decision Tree to ith spacecraft in constellation
void ConstellationDetails::applyCube( const MasterInput &in, int index ) {
   SpacecraftCost &spacecraftCost_i = spacecraftCost[index]; // ith s/c
   const // Ref to ith s/c details
   SpacecraftDetails &derived_i = spacecraftCost_i.getSpacecraftDetails();
   CostBreakdown totalCost, cost; // total, shared Default CBSs
   double totalMass = derived_i.getTotalMass(), // s/c mass
          total = 0,  error = 0; // target Cost Total & Error

   // Cost as part of a constellation of Cube Sats...
   if ( in.getConstellation().getSpacecraft().size() > 1 ) {
           if (                      totalMass <=  1    ) total = 100000;
      else if ( totalMass >  1    && totalMass <=  1.25 ) total = 200000;
      else if ( totalMass >  1.25 && totalMass <=  4    ) total = 350000;
      else if ( totalMass >  4    && totalMass <=  5    ) total = 100000;
      else if ( totalMass >  5    && totalMass <=  8    ) total = 105000;
      else if ( totalMass >  8    && totalMass <= 10    ) total = 100000;
      else if ( totalMass > 10    && totalMass <= 12    ) total = 120000;
      else if ( totalMass > 12    && totalMass <= 15    ) total = 200000;
      else if ( totalMass > 15                          ) total = 800000;

   } else { // Otherwise, spacecraft NOT part of a constellation...
           if (                      totalMass <=  1    ) total = 100000;
      else if ( totalMass >  1    && totalMass <=  1.25 ) total = 200000;
      else if ( totalMass >  1.25 && totalMass <=  4    ) total = 350000;
      else if ( totalMass >  4    && totalMass <= 10    ) total = 125000;
      else if ( totalMass > 10    && totalMass <= 12    ) total = 200000;
      else if ( totalMass > 12                          ) total = 800000;
   }

   // Apply totalCost to RDTE, TFU, etc...
   totalCost.setRDTE( total, error, 0.5 );
   totalCost.setTFU ( total, error, 0.5 );
   spacecraftCost_i.setSpacecraftTotalCost( totalCost );

   SubsystemDetails iat = // IAT
            derived_i.initializeSubsystem( "iat" );
   total = 0.139*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.139*totalCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 1.0 );
   iat.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( iat );

   SubsystemDetails program = // Program
            derived_i.initializeSubsystem( "program" );
   total = 0.229*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.229*totalCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.5 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 0.5 );
   program.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( program );

   SubsystemDetails ground = // Ground
            derived_i.initializeSubsystem( "ground" );
   total = 0.066*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.066*totalCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 1.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 0.0 );
   ground.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( ground );

   SubsystemDetails launchOps = // LaunchOps
            derived_i.initializeSubsystem( "launchOps" );
   total = 0.061*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.061*totalCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 1.0 );
   launchOps.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( launchOps );
}

// Apply Small Satellite Cost Model to ith spacecraft in constellation
void ConstellationDetails::applySSCM( const MasterInput &in, int index ) {
   SpacecraftCost &spacecraftCost_i = spacecraftCost[index]; // ith s/c
   const // Ref to ith s/c details
   SpacecraftDetails &derived_i = spacecraftCost_i.getSpacecraftDetails();
   CostBreakdown cost; // shared Default CBS
   double X = 0, total = 0, // Parameter 1, target
                 error = 0; // Cost Total & Error

   SubsystemDetails structure = // Structure
            derived_i.initializeSubsystem( "structure" );
   X = structure.getMass(); // kg, range=[5-100] (ignored)
   total =  299 + 14.2*X*log( X ); // target cost [FY 2000 k$]
   error = 1097;                   //     & error
   cost.setRDTE( total, error, 0.7 );
   cost.setTFU ( total, error, 0.3 );
   structure.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( structure );

   SubsystemDetails thermal = // Thermal
            derived_i.initializeSubsystem( "thermal" );
   X = thermal.getMass(); // kg, range=[5-12] (ignored)
   total = 246 + 4.2*pow( X, 2 ); // target cost [FY 2000 k$]
   error = 119;                   //     & error
   cost.setRDTE( total, error, 0.5 );
   cost.setTFU ( total, error, 0.5 );
   thermal.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( thermal );

   SubsystemDetails eps = // EPS
            derived_i.initializeSubsystem( "eps" );
   double X0 = 0, total0 = 0, // Alternate 0, target
                  error0 = 0, // Cost Total & Error
          X1 = 0, total1 = 0, // Alternate 1
                  error1 = 0,
          X2 = 0, total2 = 0, // Alternate 2
                  error2 = 0;
   X0 = eps.getMass          (); // kg, range=[7- 70]
   total0 =  -926 +  396*pow( X0, 0.720 ); // target cost [FY 2000 k$]
   error0 =   910;                         //     & error (case 0)
   X1 =  in.getConstellation ().
            getSpacecraft    ()[index].
            getBeginLifePower(); // W, range=[20-480]
   total1 = -5850 + 4692*pow( X1, 0.150 ); // target cost [FY 2000 k$]
   error1 =  1585;                         //     & error (case 1)
   X2 =  in.getConstellation ().
            getSpacecraft    ()[index].
            getEndLifePower  (); // W, range=[ 5-440]
   total2 =   131 +  401*pow( X2, 0.452 ); // target cost [FY 2000 k$]
   error2 =  1603;                         //     & error (case 2)
   cost.chooseCER_threeCase(  X0, total0,   7, // choose best of
                                  error0,  70, // 3 CERs:
                              X1, total1,  20,
                                  error1, 480,
                              X2, total2,   5,
                                  error2, 440,
                                  total ,
                                  error       );
   cost.setRDTE( total, error, 0.62 );
   cost.setTFU ( total, error, 0.38 );
   eps.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( eps );

   SubsystemDetails ttc = // TTC
            derived_i.initializeSubsystem( "ttc" );
   X = ttc.getMass(); // kg, range=[3-30] (ignored)
   total =  841 + 95.6*pow( X, 1.35 ); // target cost [FY 2000 k$]
   error = 1060.6399011917;            //     & error
   cost.setRDTE( total, error, 0.71 );
   cost.setTFU ( total, error, 0.29 );
   ttc.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( ttc );

   SubsystemDetails adcs = // ADCS
            derived_i.initializeSubsystem( "adcs" );
   X0 = adcs.getMass            (); //  kg, range=[1-   25]
   total0 =  1358 + 8.58*pow( X0,  2   ); // target cost [FY 2000 k$]
   error0 =  1113;                        //     & error (case 0)
   X1 =   in.getConstellation   ().
             getSpacecraft      ()[index].
             getPointingAccuracy(); // deg, range=[0.25-12]
   total1 =   341 + 2651*pow( X1, -0.5 ); // target cost [FY 2000 k$]
   error1 =  1505;                        //     & error (case 1)
   cost.chooseCER_threeCase(  X0, total0,   1   , // choose best of
                                  error0,  25   , // 3 CERs:
                              X1, total1,   0.25,
                                  error1,  12   ,
                              -1, total0-1, 0   , // Cannot be
                                  error2,   0   , // selected.
                                  total ,
                                  error          );
   cost.setRDTE( total, error, 0.37 );
   cost.setTFU ( total, error, 0.63 );
   adcs.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( adcs );

   SubsystemDetails propulsion = // Propulsion
            derived_i.initializeSubsystem( "propulsion" );
   X = derived_i.getDryMass(); // kg, range=[20-400] (ignored)
   total =  65.6 + 2.19*pow( X, 1.261 ); // target cost [FY 2000 k$]
   error = 310;                          //     & error
   cost.setRDTE( total, error, 0.5 );
   cost.setTFU ( total, error, 0.5 );
   propulsion.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( propulsion );

   CostBreakdown busCost; // Spacecraft Bus
   CostEstimate  summation; // Default, accumulate...
   busCost.applySummation( structure .getCost().getRDTE(),
                           thermal   .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           eps       .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           ttc       .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           adcs      .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           propulsion.getCost().getRDTE(), summation );
   busCost.setRDTE       ( summation .getEstimate      (),
                           summation .getStandardError () );
   busCost.applySummation( structure .getCost().getTFU (),
                           thermal   .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           eps       .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           ttc       .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           adcs      .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           propulsion.getCost().getTFU (), summation );
   busCost.setTFU        ( summation .getEstimate      (),
                           summation .getStandardError () );

   SubsystemDetails payload = // Payload
            derived_i.initializeSubsystem( "payload" );
   total = 0.4*busCost.getTotal().getEstimate     (); // target cost
   error = 0.4*busCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.6 );                 // [FY 2000 k$]
   cost.setTFU ( total, error, 0.4 );
   payload.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( payload );

   CostBreakdown totalCost; // Spacecraft Total CostBS
   totalCost.applySummation(           busCost  .getRDTE(),
                             payload  .getCost().getRDTE(), summation );
   totalCost.setRDTE       ( summation.getEstimate      (),
                             summation.getStandardError () );
   totalCost.applySummation(           busCost  .getTFU (),
                             payload  .getCost().getTFU (), summation );
   totalCost.setTFU        ( summation.getEstimate      (),
                             summation.getStandardError () );
   spacecraftCost_i.setSpacecraftTotalCost( totalCost );

   SubsystemDetails iat = // IAT
            derived_i.initializeSubsystem( "iat" );
   total = 0.139*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.139*  busCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 1.0 );
   iat.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( iat );

   SubsystemDetails program = // Program
            derived_i.initializeSubsystem( "program" );
   total = 0.229*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.229*  busCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.5 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 0.5 );
   program.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( program );

   SubsystemDetails ground = // Ground
            derived_i.initializeSubsystem( "ground" );
   total = 0.066*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.066*  busCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 1.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 0.0 );
   ground.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( ground );

   SubsystemDetails launchOps = // LaunchOps
            derived_i.initializeSubsystem( "launchOps" );
   total = 0.061*totalCost.getTotal().getEstimate     (); // target cost
   error = 0.061*  busCost.getTotal().getStandardError(); //     & error
   cost.setRDTE( total, error, 0.0 );                     // [FY 2000 k$]
   cost.setTFU ( total, error, 1.0 );
   launchOps.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( launchOps );
}

// Apply Unmanned Space Vehicle Cost Model to ith s/c in constellation
void ConstellationDetails::applyUSCM( const MasterInput &in, int index ) {
   SpacecraftCost &spacecraftCost_i = spacecraftCost[index]; // ith s/c
   const // Ref to ith s/c details
   SpacecraftDetails &derived_i = spacecraftCost_i.getSpacecraftDetails();
   CostBreakdown cost; // shared Default CBS
   double X = 0, rdte = 0, // Parameter 1, RDTE Cost
                 tfu  = 0; //              TFU

   SubsystemDetails structure = // Structure
            derived_i.initializeSubsystem( "structure" );
   X = structure.getMass(); // kg, range=[54-560] (ignored)
   rdte = 157*pow( X, 0.83 ); // RDTE cost [FY 2000 k$]
   tfu  =  13.1*X;            // TFU
   cost.setRDTE( rdte, 0.38*rdte );
   cost.setTFU ( tfu , 0.39*tfu  );
   structure.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( structure );   

   SubsystemDetails thermal = // Thermal
            derived_i.initializeSubsystem( "thermal" );
   double X0 = 0, rdte0   = 0, // Parameter 1, RDTE Cost [Alternate0]
                  rdteErr = 0, //                & Error
          X1 = 0, rdte1   = 0, // Parameter 1, RDTE Cost [Alternate1]
          Y1 = 0;              //           2
   X0 = thermal  .getMass     (); // kg, range=[3- 87] (ignored)
   rdte0 = 394.0*pow( X0, 0.635 ); // RDTE cost [FY 2000 k$]
   tfu   =  50.6*pow( X0, 0.707 ); // TFU
   X1 = thermal  .getMass     (); // kg, range=[3- 87] (ignored)
   Y1 = derived_i.getTotalMass(); //          210-404
   rdte1 =   1.1*pow( X1, 0.610 )*pow( Y1, 0.943 ); // RDTE cost [FY 2000 k$]
   cost.chooseCER_twoParam( rdte0, 0.45*rdte0, // choose best of
                            rdte1, 0.32*rdte1, // 2 CERs:
                            rdte ,      rdteErr );
   cost.setRDTE( rdte,  rdteErr );
   cost.setTFU ( tfu , 0.61*tfu );
   thermal.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( thermal );

   SubsystemDetails eps = // EPS
            derived_i.initializeSubsystem( "eps" );
   X0 = eps.getMass          (); // kg, range=[ 31- 573] (ignored)
   rdte0 =  62.70*X0;                  // RDTE cost [FY 2000 k$]
   tfu   = 112.00*pow( X0   , 0.763 ); // TFU
   X1 = eps.getMass          (); // kg, range=[ 31- 573] (ignored)
   Y1 =  in.getConstellation ().
            getSpacecraft    ()[index].
            getBeginLifePower(); //  W, range=[100-2400] (ignored)
   rdte1 =   2.63*pow( X1*Y1, 0.712 ); // RDTE cost [FY 2000 k$]
   cost.chooseCER_twoParam( rdte0, 0.57*rdte0, // choose best of
                            rdte1, 0.36*rdte1, // 2 CERs:
                            rdte ,      rdteErr );
   cost.setRDTE( rdte,  rdteErr );
   cost.setTFU ( tfu , 0.44*tfu );
   eps.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( eps );

   SubsystemDetails ttc = // TTC
            derived_i.initializeSubsystem( "ttc" );
   X = ttc.getMass(); // kg, range=[12-79] (ignored)
   rdte = 545*pow( X, 0.761 ); // RDTE cost [FY 2000 k$]
   tfu  = 635*pow( X, 0.568 ); // TFU
   cost.setRDTE( rdte, 0.57*rdte );
   cost.setTFU ( tfu , 0.41*tfu  );
   ttc.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( ttc );

   SubsystemDetails adcs = // ADCS
            derived_i.initializeSubsystem( "adcs" );
   X = adcs.getMass(); // kg, range=[20-192] (ignored)
   rdte = 464*pow( X, 0.867 ); // RDTE cost [FY 2000 k$]
   tfu  = 293*pow( X, 0.777 ); // TFU
   cost.setRDTE( rdte, 0.48*rdte );
   cost.setTFU ( tfu , 0.34*tfu  );
   adcs.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( adcs );

   SubsystemDetails propulsion = // Propulsion
            derived_i.initializeSubsystem( "propulsion" );
   X = in.getConstellation     ().
          getSpacecraft        ()[index].
          getTechReadinessLevel(); // TRL, range=[1-9]
        if ( X <= 6 )
      rdte = // RDTE cost [FY 2000 k$ (from FY 2012 k$)]
             15165.18/estimateInflationRatioForFY( 2012 );
   else if ( X <= 8 )
      rdte = // RDTE cost [FY 2000 k$ (from FY 2012 k$)]
              3893.99/estimateInflationRatioForFY( 2012 );
   else if ( X <= 9 )
      rdte = // RDTE cost [FY 2000 k$ (from FY 2012 k$)]
               421.38/estimateInflationRatioForFY( 2012 );
   tfu = 0;  //  N/A cost
   cost.setRDTE( rdte, 0 );
   cost.setTFU ( tfu , 0 );
   propulsion.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( propulsion );

   CostBreakdown busCost; // Spacecraft Bus
   CostEstimate  summation; // Default, accumulate...
   busCost.applySummation( structure .getCost().getRDTE(),
                           thermal   .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           eps       .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           ttc       .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           adcs      .getCost().getRDTE(), summation );
   busCost.applySummation( summation                     ,
                           propulsion.getCost().getRDTE(), summation );
   busCost.setRDTE       ( summation .getEstimate      (),
                           summation .getStandardError () );
   busCost.applySummation( structure .getCost().getTFU (),
                           thermal   .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           eps       .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           ttc       .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           adcs      .getCost().getTFU (), summation );
   busCost.applySummation( summation                     ,
                           propulsion.getCost().getTFU (), summation );
   busCost.setTFU        ( summation .getEstimate      (),
                           summation .getStandardError () );

   applyNICM( in, index );    // extract
   SubsystemDetails payload = // Payload:
   spacecraftCost_i.getSubsystemDetails()[
   spacecraftCost_i.getSubsystemDetails().size() - 1];

   CostBreakdown totalCost; // Spacecraft Total CostBS
   totalCost.applySummation(           busCost  .getRDTE(),
                             payload  .getCost().getRDTE(), summation );
   totalCost.setRDTE       ( summation.getEstimate      (),
                             summation.getStandardError () );
   totalCost.applySummation(           busCost  .getTFU (),
                             payload  .getCost().getTFU (), summation );
   totalCost.setTFU        ( summation.getEstimate      (),
                             summation.getStandardError () );
   spacecraftCost_i.setSpacecraftTotalCost( totalCost );

   SubsystemDetails iat = // IAT
            derived_i.initializeSubsystem( "iat" );
   X0 = totalCost.getRDTE   ().getEstimate(); // [FY 2000 k$]
   X1 = derived_i.getDryMass(); // kg, range=[155-1537] (ignored)
   rdte = 989 + 0.215*X0; // RDTE cost [FY 2000 k$]
   tfu  =      10.400*X1; // TFU
   cost.setRDTE( rdte, 0.46*rdte );
   cost.setTFU ( tfu , 0.44*tfu  );
   iat.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( iat );

   SubsystemDetails program = // Program
            derived_i.initializeSubsystem( "program" );
   X0 = totalCost.getRDTE().getEstimate(); //  [FY 2000 k$]
   X1 = totalCost.getTFU ().getEstimate();
   rdte = 1.963*pow( X0, 0.841 ); // RDTE cost [FY 2000 k$]
   tfu  = 0.341*X1;               // TFU
   cost.setRDTE( rdte, 0.36*rdte );
   cost.setTFU ( tfu , 0.39*tfu  );
   program.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( program );

   SubsystemDetails ground = // Ground
            derived_i.initializeSubsystem( "ground" );
   X0 = totalCost.getRDTE().getEstimate(); //  [FY 2000 k$]
   rdte = 9.262*pow( X0, 0.642 ); // RDTE cost [FY 2000 k$]
   tfu  = 0;                      // N/A
   cost.setRDTE( rdte, 0.34*rdte );
   cost.setTFU ( tfu , 0.00*tfu  );
   ground.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( ground );

   SubsystemDetails launchOps = // LaunchOps
            derived_i.initializeSubsystem( "launchOps" );
   X1 = derived_i.getDryMass(); // kg, range=[155-1537] (ignored)
   rdte = 0;      // N/A
   tfu  = 4.9*X1; // TFU cost [FY 2000 k$]
   cost.setRDTE( rdte, 0.00*rdte );
   cost.setTFU ( tfu , 0.42*tfu  );
   launchOps.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( launchOps );
}

// Apply NASA Instrument Cost Model to ith s/c in constellation
void ConstellationDetails::applyNICM( const MasterInput &in, int index ) {
   SpacecraftCost &spacecraftCost_i = spacecraftCost[index]; // ith s/c
   const // Ref to ith s/c details
   SpacecraftDetails &derived_i = spacecraftCost_i.getSpacecraftDetails();
   CostBreakdown hardwareCost, softwareCost, cost; // payload Default CBSs
   CostEstimate  rdte, tfu; // payload RDTE & TFU estimates
   double X = in.getConstellation(). // Parameters 1-3...
                 getSpacecraft   ()[index].
                 getPayload      ()[0].
                 getTotalMass    (), // kg
          Y = in.getConstellation().
                 getSpacecraft   ()[index].
                 getPeakMaxPower (), // W
          Z = in.getConstellation().
                 getDesignLife   (), // months
          total = 0, // Payload Hardware target Cost
          error = 0, // Total & Error [FY 2015 k$]
          swTot = 0; // Total for Payload Software
   string name = in.getConstellation().
                    getSpacecraft   ()[index].
                    getPayload      ()[0].
                    getName         ();
          if ( name == "Optical/Imager"                ) {
      total =  673.0*pow( X, 0.48 )*pow( Y, 0.51 );
   } else if ( name == "Active Microwave Instruments"  ) {
      total = 1244.0*pow( X, 0.36 )*pow( Y, 0.50 );
   } else if ( name == "Passive Microwave Instruments" ) {
      total = 1664.0*pow( X, 0.38 )*pow( Y, 0.40 );
   } else if ( name == "Fields Detection System"       ) {
      total = 1646.0*pow( X, 0.31 )*pow( Y, 0.35 );
   } else if ( name == "Particle System"               ) {
      total =  233.0*pow( X, 0.35 )*pow( Y, 0.45 )*pow( Z, 0.49 );
   } else if ( name == "Antenna Subsystem (Active)"    ) {
      total =   23.0*pow( X, 0.41 )*pow( Y, 0.94 );
   } else if ( name == "Antenna Subsystem (Passive)"   ) {
      total =  914.0*pow( X, 0.70 );
   } else if ( name == "Telescope (Visible/UV)"        ) {
      X = in.getConstellation   ().
             getSpacecraft      ()[index].
             getPayload         ()[0].
             getApertureDiameter();
      total =   49.0*pow( X, 1.47 );
      error =    0.23;
   } else if ( name == "Telescope (Infrared)"          ) {
      X = in.getConstellation   ().
             getSpacecraft      ()[index].
             getPayload         ()[0].
             getApertureDiameter();
      total =   95.4*pow( X, 1.47 );
      error =    0.23;
   }
   swTot = 0.13*pow( total, 0.91 ) // software
           /estimateInflationRatioForFY( 2015 );
   total /= estimateInflationRatioForFY( 2015 );
   error /= estimateInflationRatioForFY( 2015 );
   SubsystemDetails payload = // Payload
            derived_i.initializeSubsystem( "payload" );
   hardwareCost.setRDTE( total, error, 0.6 ); // [FY 2000 k$]
   hardwareCost.setTFU ( total, error, 0.4 );
   softwareCost.setRDTE( swTot,     0, 0.6 ); // [FY 2000 k$]
   softwareCost.setTFU ( swTot,     0, 0.4 );
   cost.applySummation ( hardwareCost.getRDTE(),
                         softwareCost.getRDTE(), rdte); // Sum
   cost.applySummation ( hardwareCost.getTFU (),
                         softwareCost.getTFU (), tfu ); // Sum
   cost.setRDTE( rdte.getEstimate(), rdte.getStandardError() );
   cost.setTFU ( tfu .getEstimate(), tfu .getStandardError() );
   payload.setCost( cost ); // apply...
   spacecraftCost_i.addSubsystemDetails( payload );
}

// ConstellationDetails private utility
void ConstellationDetails::unpackContext( const MasterInput &in ) {
   designLife = in.getConstellation().getDesignLife(); // store

   const // Ref to the DSMcontext info from input cost request...
   Context  &context         =      in.getContext(); // unpack:
   string    missionDirector = context.getMissionDirector();
        if ( missionDirector == "Commercial" )
      contextFactor = 0.8;
   else if ( missionDirector == "Government" )
      contextFactor = 1.0;
   else if ( missionDirector == "Academic"   )
      contextFactor = 1.0;
   else
      contextFactor = 1.; // Default for meaningless missionDirector

   fiscalYear      = context.getFiscalYear(); // FY and factor:
   inflationFactor = estimateInflationRatioForFY( fiscalYear );

   // Extract and store remaining context info:
   launch        = context.getLaunch        ();
   groundStation = context.getGroundStations();
}

// ConstellationDetails private utility: Estimate inflationFactor at FY
double ConstellationDetails::
       estimateInflationRatioForFY( int fiscalYear ) const {

   // Given FY, return inflationFactor...
   // Years other than 2000, factor is ratio w/ FY 2000 factor
   if ( fiscalYear < 1972 ) { // FY of produced cost estimates
      // Avoid extrapolating to very early years; negative!

      // In this case, revert to looked-up values...
      return lookUpInflationRatioForFY( fiscalYear );
   }
   if ( fiscalYear < 1980 ) { // FY of produced cost estimates
      double m = 0.054, b = 0.456; // Linear extension before: 1980

      return m*(fiscalYear-1980) + b; // Extrapolate backward
   }
   if ( fiscalYear > 2020 ) { // FY of produced cost estimates
      double m = 0.033, b = 1.523; // Linear extension after: 2020

      return m*(fiscalYear-2020) + b; // Extrapolate forward
   }

   // Otherwise, return looked-up values...
   return lookUpInflationRatioForFY( fiscalYear );
}

// ConstellationDetails private utility: Look up inflationFactor by FY
double ConstellationDetails::
       lookUpInflationRatioForFY( int fiscalYear ) const {

   // Given FY, return inflationFactor...
   // Years other than 2000, factor is ratio w/ FY 2000 factor
   switch ( fiscalYear ) { // FY of produced cost estimates
      case 1980: return 0.456;
      case 1981: return 0.510;
      case 1982: return 0.559;
      case 1983: return 0.610;
      case 1984: return 0.658;
      case 1985: return 0.681;
      case 1986: return 0.700;
      case 1987: return 0.719;
      case 1988: return 0.740;
      case 1989: return 0.771;
      case 1990: return 0.802;
      case 1991: return 0.837;
      case 1992: return 0.860;
      case 1993: return 0.883;
      case 1994: return 0.901;
      case 1995: return 0.918;
      case 1996: return 0.937;
      case 1997: return 0.958;
      case 1998: return 0.970;
      case 1999: return 0.984;
      case 2000: return 1.000;
      case 2001: return 1.017;
      case 2002: return 1.034;
      case 2003: return 1.052;
      case 2004: return 1.075;
      case 2005: return 1.099;
      case 2006: return 1.123;
      case 2007: return 1.148;
      case 2008: return 1.173;
      case 2009: return 1.199;
      case 2010: return 1.225;
      case 2011: return 1.252;
      case 2012: return 1.279;
      case 2013: return 1.308;
      case 2014: return 1.336;
      case 2015: return 1.366;
      case 2016: return 1.396;
      case 2017: return 1.427;
      case 2018: return 1.458;
      case 2019: return 1.490;
      case 2020: return 1.523;

      default: return 1.; // Default for any other FY
   }
}

// ConstellationDetails singleton accessors
int ConstellationDetails::getDesigns() const {
   return designs;
}
int ConstellationDetails::getPlanes () const {
   return planes ;
}

// ConstellationDetails SpacecraftCost accessor
const vector<SpacecraftCost> &ConstellationDetails::
                              getSpacecraftCosts() const {
   return spacecraftCost;
}

// ConstellationDetails DesignLife/FY accessors
double ConstellationDetails::getDesignLife() const {
   return designLife;
}
int    ConstellationDetails::getFiscalYear() const {
   return fiscalYear;
}

// ConstellationDetails Factor accessors
double ConstellationDetails::getContextFactor  () const {
   return   contextFactor;
}
double ConstellationDetails::getInflationFactor() const {
   return inflationFactor;
}

// ConstellationDetails Launch accessor
const Launch &ConstellationDetails::getLaunch() const {
   return launch;
}

// ConstellationDetails GroundStation accessor
const vector<GroundStation> &ConstellationDetails::
                             getGroundStations() const {
   return groundStation;
}

// ConstellationDetails utilities: finding numbers in s/c details
int ConstellationDetails::nSpacecraftToBeBuilt  (       ) const {
   return spacecraftCost.size(); // N s/c costed
}
int ConstellationDetails::nSpacecraftWithDesign ( int d ) const {
   int nWd = 0, size = spacecraftCost.size(), sc = 0; // init

   for ( ; sc < size; sc++ ) // search s/c for design:
      if ( spacecraftCost[sc].getSpacecraftDetails()
                             .getDesign() == d ) nWd++;

   return nWd; // # w/ design == d
}
int ConstellationDetails::aSpacecraftWithDesign ( int d ) const {
   int bad = 0, size = spacecraftCost.size(), sc = 0; // init

   for ( ; sc < size; sc++ ) // search s/c for design:
      if ( spacecraftCost[sc].getSpacecraftDetails()
                             .getDesign() == d )
         return sc; // Return found s/c immediately

   return bad; // Shouldn't happen; selects 1st s/c
}
int ConstellationDetails::nSpacecraftFlyingPlane( int p ) const {
   int nFp = 0, size = spacecraftCost.size(), sc = 0; // init

   for ( ; sc < size; sc++ ) // search s/c for plane :
      if ( spacecraftCost[sc].getSpacecraftDetails()
                             .getPlane () == p ) nFp++;

   return nFp; // # w/ plane  == p
}

// ConstellationDetails utility: estimate launch costs
double ConstellationDetails:: // optional launchVeh name
         estimateLaunchCost( const string &vehicleName_in ) const {
   double launchVehicleCost = 0; // FY 2000 [k$] (Default)
   string vehicleName = "";

   // No name given...
   if ( vehicleName_in == "" ) // use:
        vehicleName = launch.getVehicle(); // Cost request master
   else vehicleName = vehicleName_in; // Otherwise, utility input

   // Estimate launch vehicle cost by name...
        if ( vehicleName == "Atlas II"         )
       launchVehicleCost =  90.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Atlas II A"       )
       launchVehicleCost =  95.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Atlas II AS"      )
       launchVehicleCost = 110.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Atlas V"          )
       launchVehicleCost = 132.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Athena I"         )
       launchVehicleCost =  18.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Athena II"        )
       launchVehicleCost =  26.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Delta II"         )
       launchVehicleCost =  55.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Pegasus XL"       )
       launchVehicleCost =  13.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Minotaur I"       )
       launchVehicleCost =  28.8e3; // FY 2000 [k$]
   else if ( vehicleName == "Minotaur IV"      )
       launchVehicleCost =  50.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Ariane IV (AR40)" )
       launchVehicleCost =  65.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Ariane IV (AR42P)")
       launchVehicleCost =  80.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Ariane IV (AR44L)")
       launchVehicleCost = 120.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Ariane V (550 km)")
       launchVehicleCost = 130.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Proton SL-13"     )
       launchVehicleCost =  75.0e3; // FY 2000 [k$]
   else if ( vehicleName == "Falcon 9"         )
       launchVehicleCost =  62.0e3; // FY 2000 [k$]
   else
       launchVehicleCost = 0.; // FY 2000 [k$] (Default)

   return launchVehicleCost*launch.getTotalNumber(); // FY 2000 [k$]
}

