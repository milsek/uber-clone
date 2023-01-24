import { Component, OnInit } from '@angular/core';
import { RideReportService } from 'src/app/core/http/ride/rideReportService';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';

@Component({
  selector: 'app-ride-reports',
  templateUrl: './ride-reports.component.html',
})
export class RideReportsComponent implements OnInit {
  graphData : { name: string; value: number; }[] = [];
  graphLoaded = true;
  xAxisName : string = '';
  yAxisName : string = '';
  activeSelect : string = '';
  activeAdminSelect : string = '';
  accountType: string = this.authenticationService.getAccountType();
  
  rideReportForm = new FormGroup({
    startDate: new FormControl('', [
      Validators.required,]),
    endDate: new FormControl('', [
      Validators.required,]),
      type: new FormControl('', [
        Validators.required,]),
        adminGraphType: new FormControl('', [
          Validators.required,]),
  });

  constructor(private rideReportService: RideReportService, private authenticationService: AuthenticationService) {
    
   }

   public async getReport() : Promise<void>{
    if(this.startDate?.invalid || this.endDate?.invalid || this.type?.invalid){
      return;
    }
    if(this.accountType === 'admin' && this.adminGraphType?.invalid){
      return;
    }
    var adminType : string = this.accountType === 'admin' ? this.adminGraphType?.value! : '';  
    var data = await this.rideReportService.getReport(this.startDate?.value!,this.endDate?.value!,this.type?.value!,this.accountType,adminType);
    var i = 0;
    this.graphData = [];
    for(i = 0; i < data.xaxisValues.length; i++){
      this.graphData.push({ name: data.xaxisValues[i], value: data.yaxisValues[i] });
    }
    this.xAxisName = data.xaxisName;
    this.graphData = [...this.graphData];
    this.yAxisName = data.yaxisName;
    this.graphLoaded = false;
    this.graphLoaded = true;
   }

  async ngOnInit(): Promise<void> {
  }

  public changeActiveSelect() : void{
    this.activeSelect = this.type?.value!;
  }

  public changeAdminActiveSelect() : void{
    this.activeAdminSelect = this.adminGraphType?.value!;
  }
  
  get startDate() {
    return this.rideReportForm.get('startDate');
  }
  get endDate() {
    return this.rideReportForm.get('endDate');
  }

  get type() {
    return this.rideReportForm.get('type');
  }
  get adminGraphType() {
    return this.rideReportForm.get('adminGraphType');
  }


}
