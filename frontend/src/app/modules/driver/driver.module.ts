import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RideHistoryComponent } from './ride-history/ride-history.component';
import { DriverRoutingModule } from './driver-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';

@NgModule({
  declarations: [RideHistoryComponent],
  imports: [
    CommonModule,
    DriverRoutingModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    SharedModule,
    FormsModule,
  ],
})
export class DriverModule {}
