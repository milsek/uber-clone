export interface MemberRegistrationData {
  username: string | null | undefined;
  email: string | null | undefined
  password: string | null | undefined
  name: string | null | undefined
  surname: string | null | undefined
  phoneNumber: string | null | undefined
  city: string | null | undefined
}

export interface DriverRegistrationData extends MemberRegistrationData {
  vehicleType: string | null | undefined
  babySeat: boolean;
  petsAllowed: boolean;
  make: string | null | undefined
  model: string | null | undefined
  colour: string | null | undefined
  licensePlateNumber: string | null | undefined
}
