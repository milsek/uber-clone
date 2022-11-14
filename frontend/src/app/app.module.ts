import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AppComponent } from './app.component';
import { MapComponent } from './modules/map/map.component';
import { MapControlsComponent } from './modules/map/map-controls/map-controls.component';

@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    MapControlsComponent
  ],
  imports: [
    BrowserModule, AppRoutingModule, FontAwesomeModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
