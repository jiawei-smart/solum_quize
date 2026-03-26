import { Component, ViewChild, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MortalityService } from '../services/mortality.service';
import { DataElement } from '../mortality.model';
import { MatPaginator } from '@angular/material/paginator';
@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss']
})
export class SummaryComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  year: string = '';
  month: string = '';
  state: string = '';
  zipCode: string = '';
  facilityName: string = '';

  measureId: string = '';
  measureIdList: string[];

  averageRate!: string;
  max!: string;
  min!: string;
  top10Highest!: [];
  top10Lowest!: [];

  tableData!: any;
  dataSource!: MatTableDataSource<any>;

  yearList: string[] = [];
  monthList: string[] = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];

  page: number = 1;
  pageSize: number = 10;
  pageSizeOptions: number[] = [5, 10, 20, 50];
  totalElements = 0;
  displayedColumns: string[] = ['facilityId', 'facilityName', 'state', 'zipCode', 'address', 'denominator', 'measureId', 'score',  'lowerEstimate', 'higherEstimate', 'endDate', 'footnote', 'cityTown',  'telephoneNumber'];


  constructor(private mortalityService: MortalityService) { }

  ngOnInit() {
    this.filterData();
    for (let i = 2023; i <= 2026; i++) {
      this.yearList.push(i.toString());
    }
    this.mortalityService.getMeasureIdList().subscribe(result => {
      this.measureIdList = result as string[];
    });
  }

  filterData() {
    if (this.measureId == '') {
      this.mortalityService.getMeasureIdList().subscribe(result => {
        this.measureIdList = result as string[];
        this.measureId = this.measureIdList[0];
        this.excuteFilter();
      });
    }else {
      this.excuteFilter();
    }
    
  }
  excuteFilter() {
    this.mortalityService.getSummary(this.measureId, this.year, this.month, this.state, this.zipCode, this.facilityName, this.page, this.pageSize).subscribe(result => {
      this.averageRate = result["averageRate"];
      this.max = result["max"];
      this.min = result["min"];
      this.top10Highest = result["top10Highest"];
      this.top10Lowest = result["top10Lowest"];
      this.tableData = result["tableData"];
      this.totalElements = result["total"];
      this.dataSource = new MatTableDataSource<DataElement>(this.tableData);
    });
  }

  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.page = event.pageIndex + 1;
    this.filterData();
  }

  search() {
    this.page = 1;
    this.filterData();
  }

  clearFilter() {
    this.year = '';
    this.month = '';
    this.state = '';
    this.zipCode = '';
    this.facilityName = '';
    this.page = 1;
    this.filterData();

  }
}