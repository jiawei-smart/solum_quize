import { state } from '@angular/animations';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MortalityService {
  private baseUrl = 'http://localhost:8080/api/cms';

  constructor(private http: HttpClient) {}

  getMeasureIdList(): Observable<Object> {
    return this.http.get(`${this.baseUrl}/measureIds`);
  }

  getFacilities(): Observable<Object> {
    return this.http.get(`${this.baseUrl}/facilities`);
  }

  getStates(): Observable<Object> {
    return this.http.get(`${this.baseUrl}/states`);
  }

  getZipCodes(): Observable<Object> {
    return this.http.get(`${this.baseUrl}/zipCodes`);
  }

  getFacilityRankings(): Observable<Object> {
    return this.http.get(`${this.baseUrl}/facilityRankings`);
  }

  getAnalysisData(measureId:string, facilityId:string, state:string, zipCode:string): Observable<Object> {
    var paramters= {
        measureId:measureId,
        facilityId:facilityId,
        state:state,
        zipCode:zipCode
    }
    return this.http.get(`${this.baseUrl}/analysis`, { params: paramters });
  }

  getSummary(measureId:string, year: string, month: string, state: string, zipCode: string, facilityName: string, page: number, pageSize: number): Observable<Object>{
    var paramters= {
        measureId:measureId,
        year:year,
        month:month,
        state:state,
        zipCode:zipCode,
        facilityName:facilityName,
        page:page,
        pageSize:pageSize
    }

    return this.http.get(`${this.baseUrl}/summary`, { params: paramters });
  }
}