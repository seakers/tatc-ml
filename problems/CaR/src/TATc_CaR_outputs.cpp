
//
// TATc_CaR_outputs.cpp
//
// C++ implementation of TAT-C SICD Section 7.2
//
// Builds TAT-C C&R output structures for writing to JSON file.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 31
//

#include <algorithm>
#include "TATc_CaR_outputs.hpp"

using std::sort;
using Json::Value;
using std::vector;
using TATc::CaR::outs::ResultOutput  ;
using TATc::CaR::outs::CostEstimate  ;
using TATc::CaR::outs::SpacecraftRank;


// ResultOutput constructor
ResultOutput::ResultOutput() { // Default
   // Default constructors do their work.
}

// ResultOutput CostEstimate mutators
void ResultOutput::setLifecycleCost   ( const CostEstimate &cost ) {
      lifecycleCost = cost;
}
void ResultOutput::setHardwareCost    ( const CostEstimate &cost ) {
       hardwareCost = cost;
}
void ResultOutput::setIatCost         ( const CostEstimate &cost ) {
            iatCost = cost;
}
void ResultOutput::setProgramCost     ( const CostEstimate &cost ) {
        programCost = cost;
}
void ResultOutput::setGroundCost      ( const CostEstimate &cost ) {
         groundCost = cost;
}
void ResultOutput::setLaunchCost      ( const CostEstimate &cost ) {
         launchCost = cost;
}
void ResultOutput::setOperationsCost  ( const CostEstimate &cost ) {
     operationsCost = cost;
}
void ResultOutput::setNonRecurringCost( const CostEstimate &cost ) {
   nonRecurringCost = cost;
}
void ResultOutput::setRecurringCost   ( const CostEstimate &cost ) {
      recurringCost = cost;
}

// ResultOutput SpacecraftRank accumulator
void ResultOutput::addSpacecraftRank( const SpacecraftRank &rank ) {
   spacecraftRank.push_back( rank );
}

// ResultOutput SpacecraftRank sorter
void ResultOutput::setSpacecraftRankings() {
   sort( spacecraftRank.begin(),
         spacecraftRank.end  () );
   int r = 0,  size = spacecraftRank.size();
   for ( ; r < size; r++ ) spacecraftRank[r].setRank( r + 1 );
}

// ResultOutput JSON builder
void ResultOutput::buildJSON( Value &out ) const {
     lifecycleCost.buildJSON( out[   "lifecycleCost"] );
      hardwareCost.buildJSON( out[    "hardwareCost"] );
           iatCost.buildJSON( out[         "iatCost"] );
       programCost.buildJSON( out[     "programCost"] );
        groundCost.buildJSON( out[      "groundCost"] );
        launchCost.buildJSON( out[      "launchCost"] );
    operationsCost.buildJSON( out[  "operationsCost"] );
  nonRecurringCost.buildJSON( out["nonRecurringCost"] );
     recurringCost.buildJSON( out[   "recurringCost"] );

   Value &out_r = out["spacecraftRank"];
   int r = 0, size = spacecraftRank.size();
   for ( ; r < size; r++ ) // Only if size > 0...
      spacecraftRank[r].buildJSON( out_r[r] );
}


// CostEstimate constructor
CostEstimate::CostEstimate() { // Default
   setEstimate     (    0 );
   setStandardError(    0 );
   setFiscalYear   ( 2017 );
}

// CostEstimate mutators
void CostEstimate::setEstimate     ( double est ) {
   estimate      = est;
}
void CostEstimate::setStandardError( double err ) {
   standardError = err;
}
void CostEstimate::setFiscalYear   ( int    FY  ) {
   fiscalYear    = FY ;
}

// CostEstimate accessors
double CostEstimate::getEstimate     () const {
   return estimate     ;
}
double CostEstimate::getStandardError() const {
   return standardError;
}
double CostEstimate::getFiscalYear   () const {
   return fiscalYear   ;
}

// CostEstimate JSON builder
void CostEstimate::buildJSON( Value &out ) const {
   out["estimate"     ] = estimate     ;
   out["standardError"] = standardError;
   out["fiscalYear"   ] = fiscalYear   ;
}


// SpacecraftRank constructor
SpacecraftRank::SpacecraftRank() { // Default
   setSpacecraftIndex(    0     );
   setTotalCost      (    0     );
   setFiscalYear     ( 2017     );
   setInflationFactor(    1.427 ); // FY 2017
   setRank           (    1     );
}

// SpacecraftRank mutators
void SpacecraftRank::setSpacecraftIndex( int    index ) {
   spacecraftIndex = index;
}
void SpacecraftRank::setTotalCost      ( double cost  ) {
   totalCost       = cost ;
}
void SpacecraftRank::setFiscalYear     ( int    FY    ) {
   fiscalYear      = FY   ;
}
void SpacecraftRank::setInflationFactor( double iFctr ) {
   inflationFactor = iFctr;
}
void SpacecraftRank::setRank           ( int    r     ) {
   rank            = r    ;
}

// SpacecraftRank operators
bool SpacecraftRank::operator<( const SpacecraftRank &rhs ) const {
   return     totalCost/    inflationFactor < // compare FY
          rhs.totalCost/rhs.inflationFactor ; // 2000 costs
}

// SpacecraftRank JSON builder
void SpacecraftRank::buildJSON( Value &out ) const {
   out["spacecraftIndex"] = spacecraftIndex;
   out["totalCost"      ] = totalCost      ;
   out["fiscalYear"     ] = fiscalYear     ;
   out["rank"           ] = rank           ;
}

