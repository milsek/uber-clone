import { Component, OnInit } from '@angular/core';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;

  constructor() { }

  ngOnInit(): void {
  }

}
