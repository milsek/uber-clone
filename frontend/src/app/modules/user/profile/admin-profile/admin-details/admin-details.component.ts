import { Component, Input, OnInit } from '@angular/core';
import { faEnvelope, faMobileRetro, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { Admin } from 'src/app/shared/models/admin.model';

@Component({
  selector: 'app-admin-details',
  templateUrl: './admin-details.component.html',
})
export class AdminDetailsComponent implements OnInit {
  @Input() admin!: Admin;

  faMobileRetro: IconDefinition = faMobileRetro;
  faEnvelope: IconDefinition = faEnvelope;

  constructor() { }

  ngOnInit(): void {
  }

}
