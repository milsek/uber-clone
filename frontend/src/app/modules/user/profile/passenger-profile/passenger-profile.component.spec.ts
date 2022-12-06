import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerProfileComponent } from './passenger-profile.component';

describe('MemberComponent', () => {
  let component: PassengerProfileComponent;
  let fixture: ComponentFixture<PassengerProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PassengerProfileComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
