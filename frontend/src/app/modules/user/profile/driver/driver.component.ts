import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { Driver } from 'src/app/shared/models/driver.model';

@Component({
  selector: 'app-driver',
  templateUrl: './driver.component.html',
})
export class DriverPageComponent implements OnInit {
  faChevronLeft = faChevronLeft;
  driver!: Driver;
  notFound = false;
  
  constructor(private driverService: DriverService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    const username: string | null = this.route.snapshot.paramMap.get('username');
    if (username)
      this.driverService
        .getDriverByUsername(username)
        .then((response) => {
          this.driver = response.data;
        })
        .catch((err) => {
          this.notFound = true;
        });
  }

}
