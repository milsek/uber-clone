import { Component, OnInit } from '@angular/core';
import {
  IconDefinition,
  faChevronLeft,
  faStar,
  faChevronCircleDown,
  faUser,
  faTaxi,
} from '@fortawesome/free-solid-svg-icons';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import * as GeoSearch from 'leaflet-geosearch';
import { PassengerRide } from 'src/app/shared/models/ride.model';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import * as moment from 'moment';
import { Driver } from 'src/app/shared/models/driver.model';
import { PhotoService } from 'src/app/core/http/user/photo.service';
import { Router } from '@angular/router';

const service_url = 'https://nominatim.openstreetmap.org/reverse?format=json';
const API_KEY = null;

@Component({
  selector: 'app-ride-history',
  templateUrl: './ride-history.component.html',
})
export class RideHistoryComponent implements OnInit {
  faChevronLeft: IconDefinition = faChevronLeft;
  faStar: IconDefinition = faStar;
  faUser: IconDefinition = faUser;
  faTaxi: IconDefinition = faTaxi;

  private map: any;
  private control: any;
  private provider!: GeoSearch.OpenStreetMapProvider;
  faChevronCircleDown: IconDefinition = faChevronCircleDown;
  sortBy: string = 'ride.startTime';
  startElem: number = 0;
  numOfElements: number = 0;
  page: number = 0;
  selectedRide: PassengerRide | null = null;
  selectedIsFavourite: boolean = false;
  rides: Array<PassengerRide> = [];
  driver: Driver | null = null;

  showReviewModal: boolean = false;

  constructor(
    private passengerService: PassengerService,
    private photoService: PhotoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    setTimeout(() => {
      this.initMap();
      this.getRides();
    }, 10);
  }

  private initMap(): void {
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: './assets/icons/alternative-marker.png',
      iconSize: [30, 45],
      iconAnchor: [15, 45],
    });

    this.map = L.map('map', {
      center: [45.254326, 19.827178],
      zoom: 13,
    });

    const tiles = L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 19,
        minZoom: 3,
        attribution:
          '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }
    );
    tiles.addTo(this.map);

    this.provider = new GeoSearch.OpenStreetMapProvider();

    this.control = L.Routing.control({
      lineOptions: {
        styles: [{ color: '#ff7035', weight: 4 }],
        extendToWaypoints: true,
        missingRouteTolerance: 0.1,
      },
      showAlternatives: false,
      addWaypoints: false,
      waypoints: [],
      autoRoute: true,
      routeWhileDragging: true,
      useZoomParameter: true,
    }).addTo(this.map);
  }

  selectRide(id: number): void {
    const newRide = this.rides.find((ride) => ride.id === id);
    if (newRide === this.selectedRide) return;
    this.selectedRide! = this.rides.find((ride) => ride.id === id)!;
    this.map.setView(this.selectedRide.route.coordinates[0], 8);
    this.control.setWaypoints(this.selectedRide!.route.waypoints);
    this.checkIsFavourite();
  }

  sortRides(event: Event): void {
    const sortBy = (event.target as HTMLInputElement).value;
    this.sortBy = sortBy;
    this.getRides();
  }

  getRides(): void {
    this.passengerService
      .getRides(this.page, 4, this.sortBy, '')
      .then((res) => {
        this.startElem = this.page * 4;
        this.numOfElements = res.data.totalElements;
        this.rides = res.data.content;
        this.selectedRide = this.rides[0];
        this.control.setWaypoints([
          this.selectedRide.route.waypoints[0],
          this.selectedRide.route.waypoints[1],
        ]);
        this.checkIsFavourite();
        this.passengerService
          .getRideDetails(this.selectedRide.id)
          .then((res) => {
            this.driver = res.data.driver;
            this.getImage(this.driver!.profilePicture);
          });
      });
  }

  prev(): void {
    this.page--;
    this.getRides();
  }

  next(): void {
    this.page++;
    this.getRides();
  }

  changeFavouriteStatus(): void {
    if (!this.selectedIsFavourite)
      this.passengerService
        .markFavouriteRoute(this.selectedRide!.id)
        .then(() => {
          this.checkIsFavourite();
        });
    else
      this.passengerService
        .unmarkFavouriteRoute(this.selectedRide!.id)
        .then(() => {
          this.checkIsFavourite();
        });
  }

  checkIsFavourite(): void {
    this.passengerService
      .isFavouriteRoute(this.selectedRide!.id)
      .then((res) => {
        this.selectedIsFavourite = res.data;
      });
  }

  orderRouteAgain(): void {
    if (this.selectedRide) {
      this.passengerService.setTemporaryRoute(this.selectedRide.route);
      window.location.href = '/';
    }
  }

  openReviewModal(): void {
    this.showReviewModal = true;
  }

  onReviewSent(): void {
    this.showReviewModal = false;
    this.getRides();
  }

  canUserRateRide(): boolean {
    if (!this.selectedRide) return false;
    if (this.selectedRide.driverRating || this.selectedRide.vehicleRating)
      return false;
    if (moment().diff(moment(this.selectedRide.endTime), 'hours') > 72)
      return false;
    return true;
  }

  getDateTime(date: Date): string {
    let tempDate = new Date(date);
    return tempDate.toLocaleString('en-GB');
  }

  getRideStatus(ride: PassengerRide): string {
    return ride.status.replace(/_/g, ' ');
  }

  setArrayFromNumber(i: number) {
    return new Array(i);
  }

  getImage(profilePicture: string): void {
    this.photoService.loadImage(profilePicture).then((response) => {
      this.driver!.userImage = response.data;
    });
  }

  openDriver(): void {
    this.router.navigate(['driver/' + this.driver!.username]);
  }

  goHome(): void {
    window.location.href = '/';
  }
}
