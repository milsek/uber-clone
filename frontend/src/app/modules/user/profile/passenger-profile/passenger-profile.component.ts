import { Component, Input } from '@angular/core';
import { IconDefinition, faBars } from '@fortawesome/free-solid-svg-icons';
import { Passenger } from 'src/app/shared/models/passenger.model';

enum PassengerProfileView {
  Details = "details",
  Edit = 'edit'
}

@Component({
  selector: 'app-passenger-profile',
  templateUrl: './passenger-profile.component.html'
})
export class PassengerProfileComponent {
  @Input() passenger!: Passenger;

  _selectedView: PassengerProfileView = PassengerProfileView.Details; 
  faBars: IconDefinition = faBars;
  showDropdown: boolean = false;

  constructor() { }

  set selectedView(value: string) {
    this._selectedView = value as PassengerProfileView;
  }

  get selectedView() {
    return this._selectedView;
  }

}
