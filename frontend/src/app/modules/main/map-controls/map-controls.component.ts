import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DriverInfo } from 'src/app/shared/models/data-transfer-interfaces/driver-info.model';
import { RideSummary } from 'src/app/shared/models/data-transfer-interfaces/ride-summary.model';

@Component({
  selector: 'app-map-controls',
  templateUrl: './map-controls.component.html',
})
export class MapControlsComponent implements OnInit {
  @Input() summary!: RideSummary;
  @Input() driverInfo!: DriverInfo;
  @Output() clear: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }
  clearMarkers = (): void => {
    this.clear.emit();
  }
}
