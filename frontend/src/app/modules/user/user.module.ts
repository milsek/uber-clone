import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountComponent } from './profile/account.component';
import { UserRoutingModule } from './user-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { DriverProfileComponent } from './profile/driver/driver-profile/driver-profile.component';
import { PassengerProfileComponent } from './profile/passenger-profile/passenger-profile.component';
import { DriverPageComponent } from './profile/driver/driver.component';
import { DriverDetailsComponent } from './profile/driver/driver-profile/driver-details/driver-details.component';
import { DriverReviewsComponent } from './profile/driver/driver-profile/driver-reviews/driver-reviews.component';
import { DriverEditComponent } from './profile/driver/driver-profile/driver-edit/driver-edit.component';
import { PassengerDetailsComponent } from './profile/passenger-profile/passenger-details/passenger-details.component';
import { PassengerEditComponent } from './profile/passenger-profile/passenger-edit/passenger-edit.component';
import { AdminProfileComponent } from './profile/admin-profile/admin-profile.component';
import { AdminDetailsComponent } from './profile/admin-profile/admin-details/admin-details.component';
import { AdminEditComponent } from './profile/admin-profile/admin-edit/admin-edit.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';

@NgModule({
  declarations: [
    AccountComponent,
    DriverProfileComponent,
    PassengerProfileComponent,
    DriverPageComponent,
    DriverDetailsComponent,
    DriverReviewsComponent,
    DriverEditComponent,
    PassengerDetailsComponent,
    PassengerEditComponent,
    AdminProfileComponent,
    AdminDetailsComponent,
    AdminEditComponent,
  ],
  imports: [
    CommonModule,
    UserRoutingModule,
    FontAwesomeModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
  ],
})
export class UserModule {}
