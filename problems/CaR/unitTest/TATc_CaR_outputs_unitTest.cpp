
//
// TATc_CaR_outputs_unitTest.cpp
//
// Call: <path/to/>TATc_CaR_outputs_unitTest [option]
// Test selected by option; writes to console or file
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 02
//

#include <cstdlib>
#include <fstream>
#include <iostream>
#include "TATc_CaR_outputs.hpp"

using std::atoi;
using std::cout;
using std::endl;
using Json::Value;
using std::vector;
using std::ofstream;
using TATc::CaR::outs::ResultOutput  ;
using TATc::CaR::outs::CostEstimate  ;
using TATc::CaR::outs::SpacecraftRank;

void generateResultOutput( int );


int main( int argc, char *argv[] ) { // Unit-testing

   int option = 0; // Default option
   if ( argc > 1 ) // User provided?
      option = atoi( argv[1] );

   // Generate ResultOutput selected by option
   cout << "Unit-test of TATc_CaR_outputs" << endl;
   generateResultOutput( option );

   return 0;
}


void generateResultOutput( int option ) {

   // Generate Default ResultOutput object, modify...
   cout << " Generating non-default result" << endl;
   ResultOutput resultOutput; // Default
   CostEstimate cost; // Default

   // Set to non-default costs...
   cost.setEstimate( 1 ); // Lifecycle
   resultOutput.setLifecycleCost( cost );
   cost.setEstimate( 2 ); // Hardware
   resultOutput.setHardwareCost( cost );
   cost.setEstimate( 3 ); // Iat
   resultOutput.setIatCost( cost );
   cost.setEstimate( 4 ); // Program
   resultOutput.setProgramCost( cost );
   cost.setEstimate( 5 ); // Ground
   resultOutput.setGroundCost( cost );
   cost.setEstimate( 6 ); // Launch
   resultOutput.setLaunchCost( cost );
   cost.setEstimate( 7 ); // Operations
   resultOutput.setOperationsCost( cost );
   cost.setEstimate( 8 ); // NonRecurring
   resultOutput.setNonRecurringCost( cost );
   cost.setEstimate( 9 ); // Recurring
   resultOutput.setRecurringCost( cost );

   // Add variable number of default ranks:
   for ( int i = 0; i < option; i++ ) { // option: # loops
      SpacecraftRank  spacecraftRank; // Add default
      resultOutput.addSpacecraftRank( spacecraftRank );
   }

   // Writing modified Result to JSON file
   cout << " Note: No file is written." << endl;
   Value v; //  result Value
   resultOutput.buildJSON( v );
   cout << v << "\n"; // Easy test
}

