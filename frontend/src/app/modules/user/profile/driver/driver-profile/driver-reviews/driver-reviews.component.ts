import { Component, Input, OnInit } from '@angular/core';
import { Driver } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-driver-reviews',
  templateUrl: './driver-reviews.component.html',
})
export class DriverReviewsComponent implements OnInit {
  @Input() driver!: Driver;

  constructor() { }

  ngOnInit(): void {
  }

}
