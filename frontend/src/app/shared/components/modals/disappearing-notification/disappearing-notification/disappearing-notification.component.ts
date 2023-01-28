import { Component, Input } from '@angular/core';
import { IconDefinition, faBell } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-disappearing-notification',
  templateUrl: './disappearing-notification.component.html',
  styleUrls: ['./disappearing-notification.component.css']
})
export class DisappearingNotificationComponent {
  @Input() title!: string;
  @Input() description!: string;
  faBell: IconDefinition = faBell;

  constructor() { }

}
