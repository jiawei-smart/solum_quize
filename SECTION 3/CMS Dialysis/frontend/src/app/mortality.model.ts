export interface MortalitySummary {
    total: number;
    avgMortality: number;
    minMortality: number;
    maxMortality: number;
    top10Highest: any[];
    top10Lowest: any[];
  }
  
  export interface AnalysisData {
    monthlyTrend: { month: string, avg: number }[];
    byState: { State: string, avg: number }[];
  }

  export interface DataElement {
    facilityId?: string;
    facilityName?: string;
    address?: string;
    cityTown?: string;
    state?: string;
    zipCode?: string;
    county?: string;
    telephoneNumber?: string;
    measureId?: string;
    measureName?: string;
    comparedToNational?: string;
    denominator?: number;
    score?: number;
    lowerEstimate?: number;
    higherEstimate?: number;
    footnote?: string;
    startDate?: string;
    endDate?: string;
  }