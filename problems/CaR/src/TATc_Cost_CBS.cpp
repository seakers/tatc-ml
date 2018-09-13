
//
// TATc_Cost_CBS.cpp
//
// C++ implementation per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Manages TAT-C Cost structures for internal calculation.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 29
//

#include <cmath>
#include "TATc_Cost_CBS.hpp"

using std::pow ;
using std::sqrt;
using TATc::CaR::outs::CostEstimate;
using TATc::Cost::CBS::CostBreakdown;


// CostBreakdown constructor (from common FY)
CostBreakdown::CostBreakdown( int fiscalYear ) {
   rdte .setFiscalYear( fiscalYear );
   tfu  .setFiscalYear( fiscalYear );
   total.setFiscalYear( fiscalYear );
}

// CostBreakdown mutators -- optional params:
// Note: weight applied such that target total
//       estimate and standardError are specified,
//       along with weights to apply to components,
//       NOT per usual rules for r.v. coefficients.
void CostBreakdown::setRDTE( double estimate     ,
                             double standardError,
                             double weight        ) {
   rdte .setEstimate     ( estimate     *      weight   );
   rdte .setStandardError( standardError*sqrt( weight ) );
   updateTotal();
}
void CostBreakdown::setTFU ( double estimate     ,
                             double standardError,
                             double weight        ) {
   tfu  .setEstimate     ( estimate     *      weight   );
   tfu  .setStandardError( standardError*sqrt( weight ) );
   updateTotal();
}
void CostBreakdown::updateTotal() { // private method
   total.setEstimate     (           rdte.getEstimate     ()      +
                                     tfu .getEstimate     ()       );
   total.setStandardError( sqrt(pow( rdte.getStandardError(), 2 ) +
                                pow( tfu .getStandardError(), 2 )) );
}

// CostBreakdown accessors
const CostEstimate &CostBreakdown::getRDTE () const {
   return rdte ;
}
const CostEstimate &CostBreakdown::getTFU  () const {
   return tfu  ;
}
const CostEstimate &CostBreakdown::getTotal() const {
   return total;
}

// CostBreakdown utility: X + Y costs stored in summation
void CostBreakdown::applySummation( const CostEstimate &X        ,
                                    const CostEstimate &Y        ,
                                          CostEstimate &summation ) const {
   summation.setEstimate     (           X.getEstimate     ()      +
                                         Y.getEstimate     ()       );
   summation.setStandardError( sqrt(pow( X.getStandardError(), 2 ) +
                                    pow( Y.getStandardError(), 2 )) );
}

// CostBreakdown utility: apply best of 3 CER choices (default to 0)
void CostBreakdown::
     chooseCER_threeCase( double      X0, double  total0, double rngBeg0,
                                          double  error0, double rngEnd0,
                          double      X1, double  total1, double rngBeg1, 
                                          double  error1, double rngEnd1,
                          double      X2, double  total2, double rngBeg2,
                                          double  error2, double rngEnd2,
                          double  &total, double &error ) const {
   int    isOutV = 0, n_inRange = 3, maxTotal_ind =      0,
          choice = 0; // boolean vectors, counts & indices
   double buffer = 0.25,             maxTotal_val = total0;
   double range0 = buffer*(rngEnd0 - rngBeg0), // buffered
          range1 = buffer*(rngEnd1 - rngBeg1),
          range2 = buffer*(rngEnd2 - rngBeg2);

   rngBeg0 -= range0; rngEnd0 += range0; // expand ranges
   rngBeg1 -= range1; rngEnd1 += range1;
   rngBeg2 -= range2; rngEnd2 += range2;

   // Search for in-range choice (track out-range w/ isOutV += 2^i):
   if ( X0 < rngBeg0 || X0 > rngEnd0 ) { isOutV += 1; n_inRange--; }
   if ( X1 < rngBeg1 || X1 > rngEnd1 ) { isOutV += 2; n_inRange--; }
   if ( X2 < rngBeg2 || X2 > rngEnd2 ) { isOutV += 4; n_inRange--; }

   if ( n_inRange <= 1 ) { // No or One choice in range...
      switch ( isOutV ) {  // choose in-range case:
        default: // shouldn't happen; choice stays 0
         case 7: choice = 0; // All out (isOutV == 2^0 + 2^1 + 2^2)
                 break;
         case 6: choice = 0; // 1,2 out (isOutV ==       2^1 + 2^2)
                 break;
         case 5: choice = 1; // 0,2 out (isOutV == 2^0 +       2^2)
                 break;
         case 3: choice = 2; // 0,1 out (isOutV == 2^0 + 2^1      )
      }
   } else { // Otherwise, break ties w/ max total...
      if (  isOutV   % 2 == 1 || // either case#0 out, or
           (isOutV/2 % 2 == 0 && // case#1 in and
            total1 > maxTotal_val) ) { // new max: 1
         maxTotal_val = total1;
         maxTotal_ind =      1;
      }
      if ( (isOutV/4 % 2 == 0 && // case#2 in and
            total2 > maxTotal_val) ) { // new max: 2
         maxTotal_ind =      2;
      }
      choice = maxTotal_ind; // choose max in tie
   }

   switch ( choice ) { // apply best:
     default: //  impossible; choice stays 0
      case 0: total = total0; error = error0;
              break;
      case 1: total = total1; error = error1;
              break;
      case 2: total = total2; error = error2;
   }
}

// CostBreakdown utility: apply best 2-param CER choice (default: 0)
void CostBreakdown::
     chooseCER_twoParam( double  rdte0, double  rdteErr0,
                         double  rdte1, double  rdteErr1,
                         double &rdte , double &rdteErr  ) const {

   if ( rdte1 > rdte0 ) { // case#1 gives higher RDTE...
      rdte = rdte1; rdteErr = rdteErr1; // Choose case#1
   } else { // Otherwise...
      rdte = rdte0; rdteErr = rdteErr0; // Default to #0
   }
}

