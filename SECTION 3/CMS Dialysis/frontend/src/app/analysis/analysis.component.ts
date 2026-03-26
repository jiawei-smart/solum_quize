import { state } from '@angular/animations';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MortalityService } from '../services/mortality.service';
import { Chart, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-analysis',
  templateUrl: './analysis.component.html',
  styleUrls: ['./analysis.component.scss']
})
export class AnalysisComponent implements OnInit {
  @ViewChild('pieCanvas') pieCanvas!: ElementRef<HTMLCanvasElement>;
  chart?: Chart;

  public lineChartData?: any


  measureIdList: string[] = [];
  measureId: string = '';

  facilityIdList: string[] = [];
  facilityId: string = '';

  stateList: string[] = [];
  state1: string = '';
  state2: string = '';

  zipCodeList: string[] = [];
  zipCode1: string = '';
  zipCode2: string = '';

  public statesPieChartData: ChartData<'pie', number[], string>;
  public statesPieChartType: ChartType = 'pie';

  public zipCodePieChartData: ChartData<'pie', number[], string>;
  public zipCodePieChartType: ChartType = 'pie';


  constructor(private mortalityService: MortalityService) { }

  ngOnInit() {
    this.mortalityService.getMeasureIdList().subscribe(result => {
      this.measureIdList = result as string[];
    });

    this.mortalityService.getFacilities().subscribe(result => {
      this.facilityIdList = result as string[];
    });

    this.mortalityService.getStates().subscribe(result => {
      this.stateList = result as string[];
    });

    this.mortalityService.getZipCodes().subscribe(result => {
      this.zipCodeList = result as string[];
    });



  }

  searchStates() {
    this.mortalityService.getAnalysisData(this.measureId, '', this.state1, '').subscribe(result => {
      var data1 = result as any;
      this.mortalityService.getAnalysisData(this.measureId, '', this.state2, '').subscribe(result => {
        var data2 = result as any;
        var state1Score = this.calculateAverageScore(data1);
        var state2Score = this.calculateAverageScore(data2);
        this.showStateScorePieChart(this.state1, this.state2, state1Score, state2Score);
      });
    });

  }

  searchZipCodes() {
    this.mortalityService.getAnalysisData(this.measureId, '','', this.zipCode1).subscribe(result => {
      var data1 = result as any;
      this.mortalityService.getAnalysisData(this.measureId, '', '', this.zipCode2).subscribe(result => {
        var data2 = result as any;
        var zipCode1Score = this.calculateAverageScore(data1);
        var zipCode2Score = this.calculateAverageScore(data2);
        this.showZipCodeScorePieChart(this.zipCode1, this.zipCode2, zipCode1Score, zipCode2Score);
      });
    });

  }
  showZipCodeScorePieChart(zipCode1: string, zipCode2: string, zipCode1Score: number, zipCode2Score: number) {
    this.zipCodePieChartData = {
      labels: [zipCode1, zipCode2],
      datasets: [
        {
          data: [zipCode1Score, zipCode2Score],
          backgroundColor: ['#3f51b5', '#ff4081']
        }
      ]
    };
  }

  calculateAverageScore(data: any) {
    if (!data || data.length === 0) return 0;

    const totalEstimatedDeaths = data.reduce((sum, item) => {
      return sum + (item.score * item.denominator);
    }, 0);

    const totalDenominator = data.reduce((sum, item) => {
      return sum + item.denominator;
    }, 0);

    if (totalDenominator === 0) return 0;
    const weightedAvg = (totalEstimatedDeaths / totalDenominator);

    return Math.round(weightedAvg * 100) / 100;
  }

  showStateScorePieChart(state1: string, state2: string, state1Score: number, state2Score: number): void {


    this.statesPieChartData = {
      labels: [state1, state2],
      datasets: [
        {
          data: [state1Score, state2Score],
          backgroundColor: ['#3f51b5', '#ff4081']
        }
      ]
    };

  }

}



