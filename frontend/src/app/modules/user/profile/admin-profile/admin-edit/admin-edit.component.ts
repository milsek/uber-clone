import { Component, Input, OnInit } from '@angular/core';
import { Admin } from 'src/app/shared/models/admin.model';

@Component({
  selector: 'app-admin-edit',
  templateUrl: './admin-edit.component.html',
})
export class AdminEditComponent implements OnInit {
  @Input() admin!: Admin;

  constructor() { }

  ngOnInit(): void {
  }

}
