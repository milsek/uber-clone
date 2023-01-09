import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from './map/map.component';
import { MapControlsComponent } from './map-controls/map-controls.component';
import { TopControlsComponent } from './map-controls/top-controls/top-controls.component';
import { MainComponent } from './main.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AppRoutingModule } from 'src/app/app-routing.module';
import { OrderMenuComponent } from './order-menu/order-menu.component';
import { FormsModule } from '@angular/forms';
import { PassengerRideDetailsComponent } from './passenger-ride-details/passenger-ride-details.component';



@NgModule({
  declarations: [
    MapComponent,
    MapControlsComponent,
    TopControlsComponent,
    MainComponent,
    OrderMenuComponent,
    PassengerRideDetailsComponent
  ],
  imports: [
    CommonModule,
    FontAwesomeModule,
    AppRoutingModule,
    FormsModule
  ]
})
export class MainModule { }