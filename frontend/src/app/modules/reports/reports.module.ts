import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportsRoutingModule } from './reports-routing.module';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RideReportsComponent } from './ride-reports/ride-reports.component';



@NgModule({
  declarations: [RideReportsComponent],
  imports: [
    CommonModule,
    ReportsRoutingModule,
    NgxChartsModule,   
    ReactiveFormsModule,
    FormsModule,
  ]
})
export class ReportsModule { }
