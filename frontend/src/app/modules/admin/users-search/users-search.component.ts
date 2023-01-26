import { Component, HostListener, Input, OnInit } from '@angular/core';
import {
  IconDefinition,
  faChevronLeft,
  faTrashCan,
} from '@fortawesome/free-solid-svg-icons';
import { Icon } from 'leaflet';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { AdminService } from 'src/app/core/http/user/admin.service';
import { DriverService } from 'src/app/core/http/user/driver.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { Driver } from 'src/app/shared/models/driver.model';
import { Note } from 'src/app/shared/models/note.model';

@Component({
  selector: 'app-users-search',
  templateUrl: './users-search.component.html',
})
export class UsersSearchComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faTrashCan: IconDefinition = faTrashCan;
  public name: string = '';
  public surname: string = '';
  public username: string = '';
  type: string = 'DRIVER';
  users: Array<Driver> = [];
  numOfElements: number = 0;
  page: number = 0;
  startElem: number = 0;

  showUserNotesModal: boolean = false;
  showUserBanModal: boolean = false;
  clickedInsideModal: boolean = false;
  userNotes: Array<Note> = [];
  selectedUser: string = '';
  banStatus: string = '';
  newNote: string = '';

  constructor(
    private photoService: PhotoService,
    private driverService: DriverService,
    private passengerService: PassengerService,
    private authenticationService: AuthenticationService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {}

  onItemChange(event: any) {
    this.type = event.target.value;
  }

  search(): void {
    this.page = 0;
    this.startElem = 0;
    this.getUsers();
  }

  getUsers(): void {
    if (this.type === 'DRIVER') {
      this.driverService
        .getDrivers(this.name, this.surname, this.username, this.page)
        .then((res) => {
          this.users = res.data.drivers;
          this.numOfElements = res.data.numberOfDrivers;
        });
    } else {
      this.passengerService
        .getPassengers(this.name, this.surname, this.username, this.page)
        .then((res) => {
          this.users = res.data.passengers;
          this.numOfElements = res.data.numberOfPassengers;
        });
    }
  }

  getImage(profilePicture: string): void {
    for (let user of this.users) {
      if (user.profilePicture === profilePicture) {
        user.userImage = '';
      }
    }
    this.photoService.loadImage(profilePicture).then((response) => {
      for (let user of this.users) {
        if (user.profilePicture === profilePicture) {
          user.userImage = response.data;
        }
      }
    });
  }

  prev(): void {
    if (this.page !== 0) {
      this.page--;
      this.startElem -= 7;
      this.getUsers();
    }
  }

  next(): void {
    if (this.startElem + 7 <= this.numOfElements) {
      this.page++;
      this.startElem += 7;
      this.getUsers();
    }
  }

  ban(): void {
    this.adminService.banUser(this.selectedUser).then(() => {
      this.getUsers();
    });
    this.showUserBanModal = false;
  }

  unban(): void {
    this.adminService.unbanUser(this.selectedUser).then(() => {
      this.getUsers();
    });
    this.showUserBanModal = false;
  }

  showBanModal(username: string): void {
    this.showUserBanModal = true;
    this.clickedInsideModal = true;
    for (let user of this.users) {
      if (user.username === username) {
        if (user.accountStatus === 'ACTIVE') {
          this.banStatus = 'NOT BANNED';
        } else {
          this.banStatus = 'BANNED';
        }
      }
    }
    this.selectedUser = username;
  }

  closeBanModal(): void {
    this.showUserBanModal = false;
  }

  showUserNotes(username: string): void {
    for (let user of this.users) {
      if (user.username === username) {
        this.userNotes = user.notes;
      }
    }
    this.selectedUser = username;
    this.showUserNotesModal = true;
    this.clickedInsideModal = true;
  }

  closeUserNotesModal(): void {
    this.showUserNotesModal = false;
  }

  addNote(): void {
    this.adminService.addNote(this.newNote, this.selectedUser);
    if (this.newNote.trim() !== '') {
      this.userNotes.push({
        content: this.newNote,
        admin: { username: this.authenticationService.getSession()!.username },
      });
      this.newNote = '';
    }
  }

  removeNote(note: Note): void {
    this.adminService.removeNote(note, this.selectedUser);
    this.userNotes = this.userNotes.filter((n) => n !== note);
  }

  @HostListener('document:click')
  clickout() {
    if (this.showUserBanModal && !this.clickedInsideModal) {
      this.showUserBanModal = false;
    } else if (this.showUserNotesModal && !this.clickedInsideModal) {
      this.showUserNotesModal = false;
    }
    this.clickedInsideModal = false;
  }

  get accountType(): string {
    return this.authenticationService.getAccountType();
  }
}
