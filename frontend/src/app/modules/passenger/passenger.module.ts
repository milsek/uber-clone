import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PassengerRoutingModule } from './passenger-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';
import { RideHistoryComponent } from './ride-history/ride-history.component';
import { FavouriteRoutesComponent } from './favourite-routes/favourite-routes.component';

@NgModule({
  declarations: [RideHistoryComponent, FavouriteRoutesComponent],
  imports: [
    CommonModule,
    PassengerRoutingModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    SharedModule,
    FormsModule,
  ],
})
export class PassengerModule {}
