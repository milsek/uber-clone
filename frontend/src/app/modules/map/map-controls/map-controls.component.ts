import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { faBars, faArrowRightToBracket } from '@fortawesome/free-solid-svg-icons';

interface RideSummary {
  totalDistance: number,
  totalTime: number,
}

@Component({
  selector: 'app-map-controls',
  templateUrl: './map-controls.component.html',
  styleUrls: ['./map-controls.component.css']
})
export class MapControlsComponent implements OnInit {
  @Input() summary!: RideSummary;
  @Output() clear: EventEmitter<any> = new EventEmitter();

  faBars = faBars;
  faArrowRightToBracket = faArrowRightToBracket;
  
  constructor() { }

  ngOnInit(): void {
  }

  clearMarkers = (): void => {
    this.clear.emit();
  }

}
