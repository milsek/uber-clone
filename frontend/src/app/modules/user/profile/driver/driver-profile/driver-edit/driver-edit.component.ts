import { Component, Input, OnInit } from '@angular/core';
import { Driver } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-driver-edit',
  templateUrl: './driver-edit.component.html',
})
export class DriverEditComponent implements OnInit {
  @Input() driver!: Driver;

  constructor() { }

  ngOnInit(): void {
  }

}
