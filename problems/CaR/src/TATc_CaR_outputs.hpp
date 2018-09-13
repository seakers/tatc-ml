
//
// TATc_CaR_outputs.hpp
//
// C++ header per TAT-C SICD Section 7.2
//
// Describes structures resulting from TAT-C Cost & Risk analysis
// for a proposed Earth Science Spacecraft Constellation mission.
// Intent is that structures are built for writing to JSON file.
//
// Author: Matt Holland (NASA/GSFC Code 587)
// Modified: 2016 Dec 31
//

#ifndef TATC_CAR_OUTPUTS_HPP
#define TATC_CAR_OUTPUTS_HPP

#include <json/json.h>
#include <vector>

using Json::Value;
using std::vector;

namespace TATc { // Tradespace Analysis Tool for Constellations
namespace CaR  { // Cost & Risk (C&R) Module
namespace outs { // Output-interface structures


class CostEstimate { // Details for some aspect of mission architecture
public:
   CostEstimate(); // Default constructor

   // Mutators
   void setEstimate     ( double );
   void setStandardError( double );
   void setFiscalYear   ( int    );

   // Accessors
   double getEstimate     () const;
   double getStandardError() const;
   double getFiscalYear   () const;
   // Note: access required for internal Cost calculations

   // JSON Value builder
   void buildJSON( Value & ) const; // build on Value reference

private:
   double estimate     ; // Expected cost [FYk$] of some mission aspect
   double standardError; // Standard Error [FYk$] in given cost estimate
   int    fiscalYear   ; // associated with the produced estimate
};


class SpacecraftRank { // Rank, by cost, within proposed constellation
public:
   SpacecraftRank(); // Default constructor

   // Mutators
   void setSpacecraftIndex( int    );
   void setTotalCost      ( double );
   void setFiscalYear     ( int    );
   void setInflationFactor( double ); // for sorting; NOT JSON
   void setRank           ( int    );

   // Operators
   bool operator<( const SpacecraftRank & ) const;
   // Note: supplies basis for sorting ranks

   // JSON Value builder
   void buildJSON( Value & ) const; // build on Value reference

private:
   int    spacecraftIndex; // 0 is first, per input array
   double totalCost      ; // total [FYk$] for ranked s/c
   int    fiscalYear     ; // used to produce the estimate
   double inflationFactor; // FY's ratio w/ FY 2000 factor
   int    rank           ; // 1 is highest, by total cost
};


class ResultOutput { // C&R executable output
public:
   ResultOutput(); // Default constructor

   // CostEstimate mutators
   void setLifecycleCost   ( const CostEstimate & );
   void setHardwareCost    ( const CostEstimate & );
   void setIatCost         ( const CostEstimate & );
   void setProgramCost     ( const CostEstimate & );
   void setGroundCost      ( const CostEstimate & );
   void setLaunchCost      ( const CostEstimate & );
   void setOperationsCost  ( const CostEstimate & );
   void setNonRecurringCost( const CostEstimate & );
   void setRecurringCost   ( const CostEstimate & );

   // SpacecraftRank accumulator
   void addSpacecraftRank( const SpacecraftRank & );
   // Note: user may add defined ranks in sorted order; no further work needed

   // SpacecraftRank sorter
   void setSpacecraftRankings();
   // Note: assumes rank-data defined; uses these to sort, then sets rankings

   // JSON Value builder
   void buildJSON( Value & ) const; // build on Value reference

private:
   CostEstimate    lifecycleCost; // total for proposed mission architecture
   CostEstimate     hardwareCost; // total to produce all physical satellites
   CostEstimate          iatCost; // total integrating, assembling and testing
   CostEstimate      programCost; // total systems engineering, program-level
   CostEstimate       groundCost; // total ground system and ground operations
   CostEstimate       launchCost; // total launch vehicle & launch operations
   CostEstimate   operationsCost; // total mission operations
   CostEstimate nonRecurringCost; // total non-recurring,
   CostEstimate    recurringCost; //       recurring cost

   vector<SpacecraftRank> spacecraftRank; // array, by estimated cost
};


} // namespace outs
} // namespace CaR
} // namespace TATc

#endif

