
//
// TATc_Cost_Obs_unitTest.cpp
//
// Call: <path/to/>TATc_Cost_Obs_unitTest [option]
// Test selected by option; writes to console or file
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 16
//

#include <cstdlib>
#include <iostream>
#include "TATc_Cost_Obs.hpp"

using std::atoi;
using std::cout;
using std::endl;
using Json::Value;
using std::vector;
using std::string;
using TATc::CaR::outs::CostEstimate;
using TATc::Cost::CBS::CostBreakdown;
using TATc::Cost::Obs::SpacecraftCost   ;
using TATc::Cost::Obs::SpacecraftDetails;
using TATc::Cost::Obs::SubsystemDetails ;

void generateSpacecraftCost   (       int                 );
void     showSpacecraftCost   ( const SpacecraftCost    & );
void     showSpacecraftDetails( const SpacecraftDetails & );
void     showSubsystemDetails ( const SubsystemDetails  & );
void     showCostBreakdown    ( const CostBreakdown     & );
void testUtilSpacecraftDetails(       int                 );


int main( int argc, char *argv[] ) { // Unit-testing

   int option = 0; // Default option
   if ( argc > 1 ) // User provided?
      option = atoi( argv[1] );

   // Generate SpacecraftCost selected by option, etc
   cout << "Unit-test of TATc_Cost_Obs" << endl;
   generateSpacecraftCost   ( option );
   testUtilSpacecraftDetails( option );

   return 0;
}


void generateSpacecraftCost( int option ) {

   // Generate SpacecraftCost object from SpacecraftDetails:
   cout << "* Generating non-default Observatory Cost" << endl;
   SpacecraftDetails spacecraftDetails; // Default, modify...
   spacecraftDetails.setDesign   ( 1   );
   spacecraftDetails.setPlane    ( 2   );
   spacecraftDetails.setDryMass  ( 3.1 );
   spacecraftDetails.setBusMass  ( 3.1 ); // No propellant
   spacecraftDetails.setTotalMass( 4.2 );

   // Add s/c factor details...
   spacecraftDetails.setHeritageFactor   ( 0.9 );
   spacecraftDetails.setReliabilityFactor( 1.1 );

   // Add FY 2017 costing details...
   CostBreakdown costBreakdown( 2017 ); // outer, for s/c
   SpacecraftCost    spacecraftCost( spacecraftDetails   );
   spacecraftCost.setSpacecraftPlane    ( 5              );
   spacecraftCost.setSpacecraftTotalCost(  costBreakdown );

   // Add variable number of non-default subsystem details:
   for ( int i = 0; i < option; i++ ) { // option: # loops
      SubsystemDetails subsystemDetails;
      subsystemDetails.setName( "test" );
      subsystemDetails.setMass( 0.3 + i );

      // Add FY 2018 + i costing details...
      CostBreakdown costBreakdown( 2018 + i );   // inner, for
      subsystemDetails.setCost( costBreakdown ); // subsystem
      spacecraftCost.addSubsystemDetails( subsystemDetails );
   }

   // Show generated SpacecraftCost
   showSpacecraftCost( spacecraftCost );
}


void showSpacecraftCost( const SpacecraftCost
                              &spacecraftCost ) {

   // Extract SpacecraftCost fields
   SpacecraftDetails spacecraftDetails =
   spacecraftCost.getSpacecraftDetails();
   vector<SubsystemDetails> subsystemDetails =
          spacecraftCost.getSubsystemDetails();

   // Show SpacecraftCost fields
   cout << "a SpacecraftCost..." << endl;
   showSpacecraftDetails( spacecraftDetails );
   int sd = 0, size = subsystemDetails.size();
   for ( ; sd < size; sd++ ) // each subsystem...
      showSubsystemDetails( subsystemDetails[sd] );
}


void showSpacecraftDetails( const SpacecraftDetails
                                 &spacecraftDetails ) {

   // Extract SpacecraftDetails fields
   int           design    = spacecraftDetails.getDesign   ();
   int           plane     = spacecraftDetails.getPlane    ();
   double          dryMass = spacecraftDetails.getDryMass  ();
   double          busMass = spacecraftDetails.getBusMass  ();
   double        totalMass = spacecraftDetails.getTotalMass();
   CostBreakdown totalCost = spacecraftDetails.getTotalCost();

   // Extract SpacecraftDetails factors
   double    heritageFactor = spacecraftDetails.getHeritageFactor   ();
   double reliabilityFactor = spacecraftDetails.getReliabilityFactor();

   // Show SpacecraftDetails fields
   cout << "+ SpacecraftDetails:"
        <<  "\n  design   :[" << design
        << "]\n  plane    :[" << plane
        << "]\n    dryMass:[" <<   dryMass
        << "]\n    busMass:[" <<   busMass
        << "]\n  totalMass:[" << totalMass
        << "]\n     heritageFactor:[" << heritageFactor
        << "]\n  reliabilityFactor:[" << reliabilityFactor
        << "]\n";
   showCostBreakdown( totalCost );
}


void showSubsystemDetails( const SubsystemDetails
                                &subsystemDetails ) {

   // Extract SubsystemDetails fields
   string        name = subsystemDetails.getName();
   double        mass = subsystemDetails.getMass();
   CostBreakdown cost = subsystemDetails.getCost();

   // Show SubsystemDetails fields
   cout << "+SubsystemDetails:"
        <<  "\n name:[" << name
        << "]\n mass:[" << mass
        << "]\n";
   showCostBreakdown( cost );
}


void showCostBreakdown( const CostBreakdown
                             &costBreakdown ) {

   // Extract CostBreakdown fields
   CostEstimate rdte  = costBreakdown.getRDTE ();
   CostEstimate tfu   = costBreakdown.getTFU  ();
   CostEstimate total = costBreakdown.getTotal();

   // Writing given CostBreakdown to JSON "file"
   cout << " JSON-style CostBreakdown:" << endl;
   Value v; // breakdown Value
   rdte .buildJSON( v["rdte" ] );
   tfu  .buildJSON( v["tfu"  ] );
   total.buildJSON( v["total"] );
   cout << v << "\n"; // Easy out
}


void testUtilSpacecraftDetails( int option ) { // ignored, for now

   // Test public utilities of SpacecraftDetails:
   cout << "* Testing utilities: SpacecraftDetails" << endl; 
   SpacecraftDetails spacecraftDetails; // Default

   cout << "Heritage Factor Table [TRL, heritageFactor]:" << endl;
   for ( int TRL = -1; TRL < 12; TRL++ ) { // range of TRLs...
      double heritageFactor = // estimate heritageFactor:
      spacecraftDetails.estimateHeritageFactorFromTRL( TRL );
      cout << " [" << TRL << ", " << heritageFactor << "]\n";
   }

   cout << "Reliability Factor Table [totalMass, reliabilityFactor]:" << endl;
   for ( double totalMass = -100.; totalMass < 1200.; totalMass += 100. ) {
      double reliabilityFactor = // estimateReliabilityFactor over mass
      spacecraftDetails.estimateReliabilityFactorFromMass( totalMass );
      cout << " [" << totalMass << ", " << reliabilityFactor << "]\n";
   }
}

