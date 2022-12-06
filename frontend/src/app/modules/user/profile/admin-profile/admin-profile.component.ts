import { Component, Input, OnInit } from '@angular/core';
import { faBars, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { Admin } from 'src/app/shared/models/admin.model';

enum AdminProfileView {
  Details = "details",
  Edit = 'edit'
}

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
})
export class AdminProfileComponent implements OnInit {
  @Input() admin!: Admin;

  _selectedView: AdminProfileView = AdminProfileView.Details; 
  faBars: IconDefinition = faBars;
  showDropdown: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

  set selectedView(value: string) {
    this._selectedView = value as AdminProfileView;
  }

  get selectedView() {
    return this._selectedView;
  }

}
