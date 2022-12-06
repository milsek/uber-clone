import { Component, Input, OnInit } from '@angular/core';
import { Passenger } from 'src/app/shared/models/passenger.model';

@Component({
  selector: 'app-passenger-edit',
  templateUrl: './passenger-edit.component.html',
})
export class PassengerEditComponent implements OnInit {
  @Input() passenger!: Passenger;

  constructor() { }

  ngOnInit(): void {
  }

}
