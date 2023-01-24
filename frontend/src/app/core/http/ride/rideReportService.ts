import { Injectable } from '@angular/core';
import axios from 'axios';
import { Coordinates } from 'src/app/shared/models/coordinates.model';
import { AuthenticationService } from '../../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class RideReportService {

  constructor(private authenticationService: AuthenticationService) { }

  getReport(startDate: string, endDate: string, reportParameter: string, userType: string, adminDiagramType : string): Promise<any> {
    var params : string;
    params = "startDate=" + startDate + "&endDate=" +endDate;
    if(userType == 'admin'){
      params += "&type=" + adminDiagramType;
    }
    params += "&reportParameter=" + reportParameter;
    return axios.get(`http://localhost:8080/api/rides/generate-report-`+userType+`?`+params, {
        headers: {
          Authorization: `Bearer ${this.authenticationService.getToken()}`,
        },
      }).then((res => {
      return res.data;
    }));
  }

}
