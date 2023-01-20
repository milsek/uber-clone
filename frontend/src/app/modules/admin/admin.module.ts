import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRoutingModule } from './admin-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ReactiveFormsModule } from '@angular/forms';
import { RegisterDriverComponent } from './register-driver/register-driver.component';
import { UpdateRequestsComponent } from './update-requests/update-requests.component';

@NgModule({
  declarations: [RegisterDriverComponent, UpdateRequestsComponent],
  imports: [
    CommonModule,
    AdminRoutingModule,
    FontAwesomeModule,
    ReactiveFormsModule,
  ],
})
export class AdminModule {}
