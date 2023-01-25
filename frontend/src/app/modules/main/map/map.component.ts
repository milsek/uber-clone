import { Component, AfterViewInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import * as GeoSearch from 'leaflet-geosearch';
import axios from 'axios';
import { DriverInfo } from 'src/app/shared/models/data-transfer-interfaces/driver-info.model';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { SimulatorService } from 'src/app/core/http/simulator/simulator.service';
import * as moment from 'moment';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { DriverRide, RideSimple } from 'src/app/shared/models/ride.model';
import { DriverService } from 'src/app/core/http/user/driver.service';

const service_url = "https://nominatim.openstreetmap.org/reverse?format=json";
const API_KEY = null;

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit {
  private map: any;
  private control: any;
  public chosenRoute: any;
  public alternativeRoute: any;
  public waypoints: any[] = [];
  private accontType: string = this.authenticationService.getAccountType();
  private vehiclePositions: any[] = [];
  @Input() driverInfo!: DriverInfo;
  private provider!: GeoSearch.OpenStreetMapProvider;
  private vehicleMarkers: any = {};
  private vehicleRoutes: any = {};

  @Input() set isMainLoaded(value: boolean) {
    this.handlePassengerRideInProgress();
    this.handleDriverRideInProgress();
  }

  handlePassengerRideInProgress(): void {
    const passengerRide: RideSimple | null = this.passengerService.getCurrentRide();
    this.handleRideInProgress(passengerRide);
  }

  handleDriverRideInProgress(): void {
    const driverRide: DriverRide | undefined = this.driverService.getCurrentRides()?.currentRide;
    this.handleRideInProgress(driverRide);
  }

  handleRideInProgress(ride: RideSimple | DriverRide | null | undefined): void {
    if (!ride) return;
    this.clearMarkers();
    this.fillWaypoints(ride);
    this.setCursorStyle();
    this.drawRideRoute(ride)
  }

  occupiedTaxiIcon = L.icon({
    iconUrl: '/assets/icons/occupied-taxi.png',
    iconSize: [20, 20]
  })

  unoccupiedTaxiIcon = L.icon({
    iconUrl: '/assets/icons/unoccupied-taxi.png',
    iconSize: [20, 20]
  })

  constructor(
    private authenticationService: AuthenticationService,
    private simulatorService: SimulatorService,
    private passengerService: PassengerService,
    private driverService: DriverService
    ) { }

  ngAfterViewInit(): void {
    this.initMap();
    this.getVehiclePositions();
    setInterval(() => {
      this.getVehiclePositions();
    }, 10000);
  }

  private initMap(): void {
    L.Marker.prototype.options.icon = L.icon({
      iconUrl: './assets/icons/alternative-marker.png',
      iconSize:     [30, 45],
      iconAnchor:   [15, 45],
    });
  
    this.map = L.map('map', {
      center: [ 45.254326, 19.827178 ],
      zoom: 15
    });

    const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    });
    tiles.addTo(this.map);

    this.provider = new GeoSearch.OpenStreetMapProvider();

    this.map.on('click', this.addMarker); 

    const that = this;
    this.control = L.Routing.control({
      lineOptions: {styles: [{color: '#ff7035', weight: 4}], extendToWaypoints: true, missingRouteTolerance: 0.1},
      altLineOptions: {styles: [{color: '#afafaf', weight: 7}], extendToWaypoints: true, missingRouteTolerance: 0.1},
      showAlternatives: true,
      addWaypoints: this.canUserAlterWaypoints() ? true : false,
      waypoints: [],
      autoRoute: true,
      routeWhileDragging: true,
      plan: L.Routing.plan([], {
        createMarker: function(i, wp) {
          return L.marker(wp.latLng, {draggable: true}).on('contextmenu', function(e: any) { 
            that.removeMarker(wp);
          });
        }
      })
    })
    .on('routesfound', function(e) {
      const newRoute = e.routes[0];
      // a waypoint was dragged
      if (that.chosenRoute?.waypoints.length === newRoute.waypoints.length)
        that.updateWaypoints(that.chosenRoute.waypoints, newRoute.waypoints);
      that.chosenRoute = newRoute;
      that.alternativeRoute = e.routes.length > 1 ? e.routes[1] : null;
    })
    .on('routeselected', function(e) {
      const previousRoute = that.chosenRoute;
      if (previousRoute !== e.route) {
        that.chosenRoute = e.route;
        that.alternativeRoute = previousRoute;
      }
    })
    .addTo(this.map);

    this.setCursorStyle();
  }

  addMarker = async (e: any) => {
    if (this.canUserAlterWaypoints()) {
      if (this.accontType === 'anonymous' && this.waypoints.length > 1) return;
      this.control.setWaypoints([...this.control.getPlan().getWaypoints().filter((x: L.Routing.Waypoint) => x.latLng), e.latlng]);
      await this.reverseSearchLocation(e.latlng.lat, e.latlng.lng)
      .then((res) => {
        this.waypoints.push(res);
      });
    }
  }

  removeMarker(wp: any): void {
    if (this.canUserAlterWaypoints()) {
      const i = this.control.getWaypoints().findIndex((x: any) => x === wp);
      this.control.setWaypoints([...this.control.getPlan().getWaypoints().filter((x: L.Routing.Waypoint) => x !== wp)]);
      this.waypoints.splice(i, 1);
      if (this.waypoints.length < 2) {
        this.chosenRoute = null;
        this.alternativeRoute = null;
      }
    }
  }

  clearMarkers(): void {
    this.control.setWaypoints([]);
    this.chosenRoute = null;
    this.waypoints = [];
  }

  drawRideRoute(ride: RideSimple | DriverRide): void {
    // maybe no need for 'deep' copy after testing
    const r: any = {
      name: 'Route',
      summary: { totalDistance: ride.distance * 1000, totalTime: ride.expectedTime },
      coordinates: ride.route.coordinates?.map(latlng => { return {...latlng}}),
      waypoints: ride.route.waypoints?.map(latlng => { return {...latlng}}),
      inputWaypoints: ride.route.waypoints?.map(latlng => { return {...latlng}}),
      waypointIndices: [0, ride.route.coordinates!.length - 1],
      routesIndex: 0,
      properties: { isSimplified: true },
      instructions: [],
    }
    L.Routing.line(r, {styles: [{color: '#ff7035', weight: 4}], extendToWaypoints: false, missingRouteTolerance: 0.1}).addTo(this.map);
    ride.route.waypoints?.forEach((e : any) => {
      L.marker({lat: e.lat, lng: e.lng}).addTo(this.map);
    });
    this.map.setView([ride.route.waypoints[0].lat, ride.route.waypoints[0].lng], 15);
  }
  
  getVehiclePositions = () => {
    this.simulatorService.getVehiclePositions()
      .then((res) => {
        this.vehiclePositions = res.data;
        this.drawVehiclePositions();
      })
      .catch((err) => {
        // oopsie
      });
  }

  drawVehiclePositions = () => {
    this.vehiclePositions.forEach(vehicle => {
      // if (this.areSameCoordinates(vehicle.currentCoordinates, vehicle.nextCoordinates)) {
      if (!vehicle.rideActive) {
        if (this.vehicleMarkers[vehicle.id]) this.vehicleMarkers[vehicle.id].removeFrom(this.map);
        const marker = L.marker(vehicle.currentCoordinates, { icon: this.unoccupiedTaxiIcon }).addTo(this.map);
        this.vehicleMarkers[vehicle.id] = marker;
        if (this.vehicleRoutes[vehicle.id]) delete this.vehicleRoutes[vehicle.id];
      } else {
        this.getRouteForCoordinates(vehicle);
        if (!this.vehicleMarkers[vehicle.id]) {
          const marker = L.marker(vehicle.currentCoordinates, { icon: this.occupiedTaxiIcon }).addTo(this.map);
          this.vehicleMarkers[vehicle.id] = marker;
        }
        if (vehicle.rideActive) {
          this.vehicleMarkers[vehicle.id].setIcon(this.occupiedTaxiIcon);
        }
      }
    })
  }

  getRouteForCoordinates = (vehicle: any) => {
    const that = this;
    if (!this.vehicleRoutes[vehicle.id]) {
      const waypoint1 = new L.Routing.Waypoint(L.latLng(vehicle.currentCoordinates), '', {});
      const waypoint2 = new L.Routing.Waypoint(L.latLng(vehicle.nextCoordinates), '', {});
      L.Routing.control({
        show: false,
        waypoints: [waypoint1, waypoint2],
        autoRoute: false,
        routeWhileDragging: false,
        addWaypoints: false,
      }).on('routesfound', function (e) {
        const route = e.routes[0];
        that.vehicleRoutes[vehicle.id] = route;
        that.simulateMovement(vehicle);
        // console.clear();
      }).route();
    }
  }
  
  simulateMovement = (vehicle: any): void => {
    if (!vehicle.rideActive) return;

    let startingMoment = moment(vehicle.coordinatesChangedAt);
    let currentMoment = moment();
    const difference = currentMoment.diff(startingMoment, 'seconds');
    const remainingTime = vehicle.expectedTripTime - difference;
    if (remainingTime <= 0) return;
    
    const finishedPartOfRide = difference / vehicle.expectedTripTime;
    const startingCoordinate = ~~(this.vehicleRoutes[vehicle.id].coordinates.length * finishedPartOfRide) + 1;

    const coordinatesToDraw =  this.vehicleRoutes[vehicle.id].coordinates.slice(startingCoordinate);
    coordinatesToDraw.forEach((coord: any, index: number) => {
      setTimeout(() => {
        this.vehicleMarkers[vehicle.id].setLatLng([coord.lat, coord.lng]);
      }, (remainingTime / coordinatesToDraw.length) * 1000 * index)
    })
  }

  canUserAlterWaypoints(): boolean {
    const accountType: string = this.authenticationService.getAccountType();
    if (accountType === 'anonymous') return true;
    return accountType === 'passenger' && !this.passengerService.getCurrentRide();
  }

  async updateWaypoints(oldRouteWaypoints: any[], newRouteWaypoints: any[]): Promise<void> {
    for (let i = 0; i < oldRouteWaypoints.length; i++) {
      if (!this.areSameCoordinates(oldRouteWaypoints[i].latLng, newRouteWaypoints[i].latLng)) {
        await this.reverseSearchLocation(newRouteWaypoints[i].latLng.lat, newRouteWaypoints[i].latLng.lng)
        .then((res) => {
          this.waypoints[i] = res;
        });
      }
    }
  }

  private areSameCoordinates(x: any, y: any): boolean {
    return x.lat === y.lat && x.lng === y.lng;
  }

  async addNewWaypoint(event: string) {
    const results = await this.searchLocation(event);
    if (results.length > 0)
      this.addMarker({ latlng: { lat: results[0].y, lng: results[0].x } });
  }

  removeWaypoint(event: any) {
    this.removeMarker(this.control.getWaypoints()[event])
  }

  searchLocation = async (query: string) => {
    return await this.provider.search({ query: query }).then((res) => {
      return res;
    });
  }

  reverseSearchLocation = async ( latitude: number, longitude: number, zoom: number = 18 ): Promise<any> => {
    let url = `${service_url}&lat=${latitude}&lon=${longitude}&zoom=${zoom}`;
    url = API_KEY ? `${url}&key=${API_KEY}` : url;
    try {
        const response = await axios.get(url);
        return {
            x: response.data.lon,
            y: response.data.lat,
            label: response.data.display_name,
            bounds: response.data.boundingbox,
            raw: response.data
        }
    } catch (error) {
        console.error(error);
    }
  }

  fillWaypoints = async (ride: RideSimple | DriverRide) => {
    if (!ride) return;
    for (const latLng of ride.route.waypoints) {
      await this.reverseSearchLocation(latLng.lat, latLng.lng)
      .then((res) => {
        this.waypoints.push(res);
      });
    }
  }

  setCursorStyle = () => {
    if (this.canUserAlterWaypoints())
      document.getElementById('map')!.style.cursor = 'crosshair';
    else
      document.getElementById('map')!.style.cursor = 'grab';
  }

}
