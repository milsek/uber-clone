export interface Passenger {
  username: string;
  name: string;
  surname: string;
  phoneNumber: string;
  city: string;
  profilePicture: string;
  distanceTravelled: number;
  ridesCompleted: number;
  tokenBalance: number;
  email?: string;
  accountType?: string;
  accountStatus?: string;
}

export interface PassengerSimple {
  username: string;
  name: string;
  surname: string;
}
