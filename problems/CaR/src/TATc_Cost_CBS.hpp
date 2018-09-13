
//
// TATc_Cost_CBS.hpp
//
// C++ header per TAT-C Cost Module Requirements Document
// [RD Author: Veronica Foreman (MIT)]
//
// Describes structures used internally by TAT-C Cost analyses.
// Intent is that structures are used for in-memory calculations.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2017 Jan 02
//

#ifndef TATC_COST_CBS_HPP
#define TATC_COST_CBS_HPP

#include "TATc_CaR_outputs.hpp"

using TATc::CaR::outs::CostEstimate;

namespace TATc { // Tradespace Analysis Tool for Constellations
namespace Cost { // Cost & Risk (C&R) Module Costing 
namespace CBS  { // Cost-breakdown structures


class CostBreakdown { // Breakdown for some aspect of mission architecture
public:
   CostBreakdown( int = 2017 ); // construct from common Fiscal Year value

   // Mutators, w/
   // Optional estimate and standardError, weights:
   void setRDTE( double = 0, double = 0, double = 1 );
   void setTFU ( double = 0, double = 0, double = 1 );
   // Total is managed automatically

   // Accessors
   const CostEstimate &getRDTE () const; // Ref
   const CostEstimate &getTFU  () const; // Ref
   const CostEstimate &getTotal() const; // Ref

   // Utility: apply summation of two inputs (FY not changed)
   void applySummation( const CostEstimate &,
                        const CostEstimate &,
                              CostEstimate & ) const; // Ref

   // Utility: apply best of 3 CER choices (default to 0)
   void chooseCER_threeCase( double, double  , double  ,
                                     double  , double  ,
                             double, double  , double  ,
                                     double  , double  ,
                             double, double  , double  ,
                                     double  , double  ,
                                     double &, double & ) const; // Ref

   // Utility: apply best 2-param CER choice (default: 0)
   void chooseCER_twoParam( double  , double  ,
                            double  , double  ,
                            double &, double & ) const; // Ref

private:
   void updateTotal(); // via RDTE and TFU mutators

   CostEstimate rdte ; // Research, Development, Testing & Evaluation
   CostEstimate tfu  ; // Theoretical First Unit
   CostEstimate total; // summation
};


} // namespace CBS
} // namespace Cost
} // namespace TATc

#endif

