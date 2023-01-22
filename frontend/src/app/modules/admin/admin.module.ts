import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRoutingModule } from './admin-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ReactiveFormsModule } from '@angular/forms';
import { RegisterDriverComponent } from './register-driver/register-driver.component';
import { UpdateRequestsComponent } from './update-requests/update-requests.component';
import { RideRejectionRequestsComponent } from './ride-rejection-requests/ride-rejection-requests.component';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';

@NgModule({
  declarations: [RegisterDriverComponent, UpdateRequestsComponent, RideRejectionRequestsComponent],
  imports: [
    CommonModule,
    AdminRoutingModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    SharedModule
  ],
})
export class AdminModule {}
