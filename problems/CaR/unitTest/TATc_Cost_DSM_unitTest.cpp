
//
// TATc_Cost_DSM_unitTest.cpp
//
// Call: <path/to/>TATc_Cost_DSM_unitTest [option]
// Test selected by option; writes to console or file
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 02
//

#include <cstdlib>
#include <fstream>
#include <iostream>
#include "TATc_Cost_DSM.hpp"

using std::atoi;
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
using TATc::CaR::outs::ResultOutput  ;
using TATc::CaR::outs::CostEstimate  ;
using TATc::CaR::outs::SpacecraftRank;
using TATc::Cost::CBS::CostBreakdown;
using TATc::Cost::Obs::SpacecraftCost   ;
using TATc::Cost::Obs::SpacecraftDetails;
using TATc::Cost::Obs::SubsystemDetails ;
using TATc::Cost::DSM::ConstellationCost   ;
using TATc::Cost::DSM::ConstellationDetails;

void generateConstellationCost   ( int,  const  MasterInput   & );
void     showConstellationDetails( const ConstellationDetails & );
void     showLaunch              ( const Launch               & );
void     showGroundStation       ( const GroundStation        & );
void     showSpacecraftCost      ( const SpacecraftCost       & );
void     showSpacecraftDetails   ( const SpacecraftDetails    & );
void     showSubsystemDetails    ( const SubsystemDetails     & );
void     showCostBreakdown       ( const CostBreakdown        & );
void testUtilConstellationDetails( int,  Value & ); // modified


int main( int argc, char *argv[] ) { // Unit-testing

   int option = 0; // Default option
   if ( argc > 1 ) // User provided?
      option = atoi( argv[1] );

   Value v; // Build from read Value
   ifstream fin( "bin/CostRisk.json" );
   fin >> v; // Read entire file
   MasterInput masterInput( v );

   // Generate ConstellationCost per option, etc
   cout << "Unit-test of TATc_Cost_DSM" << endl;
   generateConstellationCost   ( option, masterInput );
   testUtilConstellationDetails( option, v ); // modified

   return 0;
}


void generateConstellationCost(       int          option     ,
                                const MasterInput &masterInput ) {

   // Generate ConstellationCost object from MasterInput:
   cout << "* Generating non-default Mission Cost" << endl;
   ConstellationCost constellationCost( masterInput );

   // Add FY 2017 costing details...
   CostBreakdown costBreakdown( 2017 );

   // Set to non-default costs...
   costBreakdown    .setTFU      ( 1, 2          ); // Hardware
   constellationCost.setHardware ( costBreakdown );
   costBreakdown    .setTFU      ( 3, 4          ); // IAT
   constellationCost.setIAT      ( costBreakdown );
   costBreakdown    .setTFU      ( 5, 6          ); // Program
   constellationCost.setProgram  ( costBreakdown );
   costBreakdown    .setTFU      ( 7, 8          ); // Ground
   constellationCost.setGround   ( costBreakdown );
   costBreakdown    .setTFU      ( 9, 0          ); // LaunchOps
   constellationCost.setLaunchOps( costBreakdown );
   constellationCost.setOperating(); // Operating

   // Display derived ConstellationDetails from costing object
   cout << "a ConstellationCost..." << endl; // display header
   showConstellationDetails( constellationCost.getDetails() );

   // Extract modified ResultOutput from constellation costing
   cout << "& ResultOutput..." << endl; // & show display header
   ResultOutput resultOutput = constellationCost.getResultOutput();

   // Writing modified Result to JSON file
   cout << " Note: No file is written." << endl;
   Value v; //  result Value
   resultOutput.buildJSON( v );
   cout << v << "\n"; // Easy test
}


void showConstellationDetails( const ConstellationDetails
                                    &constellationDetails ) {

   // Extract ConstellationDetails singleton fields
   int    designs         = constellationDetails.getDesigns        ();
   int    planes          = constellationDetails.getPlanes         ();
   int    fiscalYear      = constellationDetails.getFiscalYear     ();
   double designLife      = constellationDetails.getDesignLife     ();
   double   contextFactor = constellationDetails.getContextFactor  ();
   double inflationFactor = constellationDetails.getInflationFactor();
   Launch launch          = constellationDetails.getLaunch         ();

   // #s/c, search for design/plane=0, launch cost w/ default args:
   int    nObsBuilt  = constellationDetails.nSpacecraftToBeBuilt();
   int    nWdZero    = constellationDetails.nSpacecraftWithDesign ( 0 );
   int    nFpZero    = constellationDetails.nSpacecraftFlyingPlane( 0 );
   double launchCost = constellationDetails.estimateLaunchCost();

   // Extract GroundStation models
   vector<GroundStation>   groundStation =
   constellationDetails.getGroundStations();
   int gs = 0, gsSz = groundStation.size();

   // Extract SpacecraftCost models
   vector<SpacecraftCost>  spacecraftCost =
   constellationDetails.getSpacecraftCosts();
   int sc = 0, scSz = spacecraftCost.size();

   // Show ConstellationDetails fields
   cout << "+ ConstellationDetails:"
        <<  "\n  designs   :[" << designs
        << "]\n  planes    :[" << planes
        << "]\n  fiscalYear:[" << fiscalYear
        << "]\n  designLife:[" << designLife
        << "]\n  nObsBuilt :[" << nObsBuilt
        << "]\n  nWdZero   :[" << nWdZero
        << "]\n  nFpZero   :[" << nFpZero
        << "]\n  launchCost:[" << launchCost
        << "]\n    contextFactor:[" <<   contextFactor
        << "]\n  inflationFactor:[" << inflationFactor
        << "]\n";
   showLaunch( launch );
   for ( ; gs < gsSz; gs++ ) // each gndStation:
      showGroundStation (  groundStation[gs] );
   for ( ; sc < scSz; sc++ ) // each spacecraft:
      showSpacecraftCost( spacecraftCost[sc] );
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


void testUtilConstellationDetails( int option, Value &in ) {

   // Test private utilities of ConstellationDetails:
   cout << "* Testing utilities: ConstellationDetails" << endl; 

   cout << "Inflation Factor Table [FY, inflationFactor]:" << endl;
   int  entryFiscalYear = in["context"]["fiscalYear"].asInt();
   for ( int fiscalYear = 1970; fiscalYear < 2031; fiscalYear++ ) {
      in["context"]["fiscalYear"] = fiscalYear; // update
      MasterInput masterInput( in ); // unpack inputs...
      ConstellationCost constellationCost( masterInput );
      double inflationFactor = // Extract inflationFactor:
      constellationCost.getDetails().getInflationFactor();
      cout << " [" << fiscalYear << ", " << inflationFactor << "]\n";
   }

   cout << "Context Factor Table [missionDirector, contextFactor]:" << endl;
   string missionDirector[] = { "Government", "Military",
                                "Commercial", "Academic" },
     entryMissionDirector = // save
   in["context"]["missionDirector"].asString();
   in["context"]["fiscalYear"] = entryFiscalYear; // restore
   for ( int md = 0; md < 4; md++ ) { // all mission directors:
      in["context"]["missionDirector"] = missionDirector[md];
      MasterInput masterInput( in ); // unpack inputs...
      ConstellationCost constellationCost( masterInput );
      double contextFactor = // Extract contextFactor...
      constellationCost.getDetails().getContextFactor();
      cout << " [" << missionDirector[md] << ", " << contextFactor << "]\n";
   }

   // Test public utilities of ConstellationDetails:
   cout << "Launch Cost Table [vehicleName, launchCost]:" << endl;
   string launchVehicle[] = {"Atlas II", "Atlas II A", "Atlas II AS",
                             "Atlas V" , "Athena I"  , "Athena II"  ,
                             "Delta II", "Pegasus XL",
                             "Minotaur I", "Minotaur IV",
                             "Ariane IV (AR40)" , "Ariane IV (AR42P)",
                             "Ariane IV (AR44L)", "Ariane V (550 km)",
                             "Proton SL-13", "Falcon 9", "Bogus1", "" };
   in["context"]["missionDirector"] = entryMissionDirector; // restore
   MasterInput masterInput( in ); // unpack inputs...
   ConstellationCost constellationCost( masterInput );
   ConstellationDetails constellationDetails =
   constellationCost.getDetails(); // details object
   for ( int lv = 0; lv < 18; lv++ ) { // all launch vehicles (& bogus):
      double launchCost = // estimateLaunchCost for given vehicle...
      constellationDetails.estimateLaunchCost( launchVehicle[lv] );
      cout << " [" << launchVehicle[lv] << ", " << launchCost << "]\n";
   }
}

