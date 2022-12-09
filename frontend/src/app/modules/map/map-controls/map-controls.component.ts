import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { DriverService } from 'src/app/core/http/user/driver.service';

interface RideSummary {
  totalDistance: number,
  totalTime: number,
}

@Component({
  selector: 'app-map-controls',
  templateUrl: './map-controls.component.html',
})
export class MapControlsComponent implements OnInit {
  @Input() summary!: RideSummary;
  @Output() clear: EventEmitter<any> = new EventEmitter();

  // All
  accountType: string = this.authenticationService.getAccountType();

  // Driver
  isActive: boolean | null = null;
  
  constructor(private driverService: DriverService, private authenticationService: AuthenticationService) { }

  async ngOnInit(): Promise<void> {
    if (this.accountType === 'driver')
      this.isActive = await this.driverService.getDriverActivity();
  }

  clearMarkers = (): void => {
    this.clear.emit();
  }
}
