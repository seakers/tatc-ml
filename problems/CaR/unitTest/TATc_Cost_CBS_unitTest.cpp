
//
// TATc_Cost_CBS_unitTest.cpp
//
// Call: <path/to/>TATc_Cost_CBS_unitTest [option]
// Test selected by option; writes to console or file
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 30
//

#include <cstdlib>
#include <iostream>
#include "TATc_Cost_CBS.hpp"

using std::atoi;
using std::cout;
using std::endl;
using Json::Value;
using TATc::CaR::outs::CostEstimate;
using TATc::Cost::CBS::CostBreakdown;

void generateCostBreakdown( int );
void testUtilCostBreakdown( int );


int main( int argc, char *argv[] ) { // Unit-testing

   int option = 0; // Default option
   if ( argc > 1 ) // User provided?
      option = atoi( argv[1] );

   // Generate CostBreakdown selected by option
   cout << "Unit-test of TATc_Cost_CBS" << endl;
   generateCostBreakdown( option );
   testUtilCostBreakdown( option );

   return 0;
}


void generateCostBreakdown( int option ) {

   // Generate FY 2017 CostBreakdown object
   cout << " Generating 2017 breakdown" << endl;
   CostBreakdown costBreakdown( 2017 );

   // Set values...
   switch ( option ) { // By option

      default: // Unrecognized or
      case 0:  // Zero-missing option
         costBreakdown.setRDTE( 1, 2 );
         costBreakdown.setTFU ( 3, 4 );
         break;

      case 1:  // One-missing option
         costBreakdown.setRDTE( 1, 2 );
         costBreakdown.setTFU ( 3    );
         break;

      case 2:  // Two-missing option
         costBreakdown.setRDTE( 1, 2 );
         costBreakdown.setTFU (      );
         break;

      case 3:  // Three-param option
         costBreakdown.setRDTE( 1, 2, 0.4 );
         costBreakdown.setTFU ( 1, 2, 0.6 );
         break;
   }

   // Get results...
   CostEstimate rdte  = costBreakdown.getRDTE ();
   CostEstimate tfu   = costBreakdown.getTFU  ();
   CostEstimate total = costBreakdown.getTotal();

   // Writing FY 2017 CostBreakdown to JSON file
   cout << " Note: No file is written." << endl;
   Value v; // breakdown Value
   rdte .buildJSON( v["rdte" ] );
   tfu  .buildJSON( v["tfu"  ] );
   total.buildJSON( v["total"] );
   cout << v << "\n"; // Easy test
}


void testUtilCostBreakdown( int option ) { // ignored, for now

   // Test public utilities of CostBreakdown:
   cout << "* Testing utilities: CostBreakdown" << endl;
   CostBreakdown c0( 2018 ); // FY 2018 costing object
   CostEstimate summation, c0Total; // Default

   cout << "Summation Test: ";
   c0.setRDTE( 1, 1 ); // 1 + 1, sqrt( 1^2 + 1^2 )
   c0.setTFU ( 1, 1 );
   c0.applySummation( c0.getRDTE(),
                      c0.getTFU (),
                      summation    );
   c0Total = c0.getTotal(); // 2, sqrt( 2 )
   if ( summation.getEstimate     () == c0Total.getEstimate     () &&
        summation.getStandardError() == c0Total.getStandardError()   )
        cout << "PASS\n";
   else cout << "FAIL\n";

   cout << "Choose CER Table (3-case) [total, error]:" << endl;
   double total = 0, error = 0;

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   0.0, 0,
                                    0.1, 1,
                               1,   1.0, 1,
                                    1.1, 2,
                               2,   2.0, 2, // Highest total, in range
                                    2.1, 3, // Intented result
                           total, error    );
   cout << " All Params in range: [" << total << ", " << error << "]"
        << " ( [2, 2.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   0.0, 0,
                                    0.1, 1,
                               1,   1.0, 1, // Intended result
                                    1.1, 2,
                               2,   2.0, 0, // Highest total, but out of range
                                    2.1, 1,
                           total, error    );
   cout << " 0,1 Params in range: [" << total << ", " << error << "]"
        << " ( [1, 1.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   0.0, 0,
                                    0.1, 1,
                               1,   3.0, 2, // Highest total, but out of range
                                    3.1, 3,
                               2,   2.0, 2, // High    total, in range
                                    2.1, 3, // Intented result
                           total, error    );
   cout << " 0,2 Params in range: [" << total << ", " << error << "]"
        << " ( [2, 2.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   3.0, 2, // Highest total, but out of range
                                    3.1, 3,
                               1,   0.0, 1,
                                    0.1, 2,
                               2,   1.0, 2, // High    total, in range
                                    1.1, 3, // Intended result
                           total, error    );
   cout << " 1,2 Params in range: [" << total << ", " << error << "]"
        << " ( [1, 1.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   0.0, 0, // Intended result
                                    0.1, 1,
                               1,   1.0, 2, // High    total, but out of range
                                    1.1, 3,
                               2,   2.0, 0, // Highest total, but out of range
                                    2.1, 1,
                           total, error    );
   cout << "   0 Param  in range: [" << total << ", " << error << "]"
        << " ( [0, 0.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   3.0, 2, // Highest total, but out of range
                                    3.1, 3,
                               1,   1.0, 1, // Intended result
                                    1.1, 2,
                               2,   2.0, 0, // High    total, but out of range
                                    2.1, 1,
                           total, error    );
   cout << "   1 Param  in range: [" << total << ", " << error << "]"
        << " ( [1, 1.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   3.0, 2, // Highest total, but out of range
                                    3.1, 3,
                               1,   1.0, 2,
                                    1.1, 3,
                               2,   2.0, 2, // High    total, in range
                                    2.1, 3, // Intended result
                           total, error    );
   cout << "   2 Param  in range: [" << total << ", " << error << "]"
        << " ( [2, 2.1] is PASS )\n";

   //                     Param: tot/err range
   c0.chooseCER_threeCase(     0,   0.0, 2, //                    out of range
                                    0.1, 3, // Intended result (default)
                               1,   4.0, 4, // Highest total, but out of range
                                    4.1, 5,
                               2,   3.0, 0, // High    total, but out of range
                                    3.1, 1,
                           total, error    );
   cout << " No  Param  in range: [" << total << ", " << error << "]"
        << " ( [0, 0.1] is PASS )\n";

   cout << "Choose CER Table (2-param) [rdte, rdteErr]:" << endl;
   double rdte = 0, rdteErr = 0;

   //                     RDTE  RDTEerr
   c0.chooseCER_twoParam(    1,     1.1,
                             2,     2.1, // Intended result
                          rdte, rdteErr );
   cout << " RDTEcase #1 greater: [" << rdte << ", " << rdteErr << "]"
        << " ( [2, 2.1] is PASS )\n";

   //                     RDTE  RDTEerr
   c0.chooseCER_twoParam(    1,     1.1, // Intended result (default)
                             0,     0.1,
                          rdte, rdteErr );
   cout << " RDTEcase #0 greater: [" << rdte << ", " << rdteErr << "]"
        << " ( [1, 1.1] is PASS )\n";
}

