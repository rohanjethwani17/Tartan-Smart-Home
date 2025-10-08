> Use blackbox testing techniques to create better test cases and corresponding oracles (i.e., the concrete input values and expected output in your tests). You are advised to carefully read Tartan's specification and decide on whether there are additional scenarios you need to test for (e.g., now is the time to think about whether the rules interact and if there are edge cases you need to think of). When selecting concrete oracles, some of your options include boundary value testing, random testing, or strong/weak equivalence class testing. You may think of other strategies, too.

## UC01
- The IoT Controller shall require the user to login to the house control panel using a
  username and password. The password has the following requirements:
  - Minimum Length: 8 characters
  - At least one upper case character
  - At least one number
  - At least one symbol
- Testing Strategy (partition)
  - length: 7, 8, 9
  - upper case chars: 0, 1, 2
  - numbers: 0, 1, 2
  - symbols: 0, 1, 2


## UC02
- The IoT Controller shall not allow the user to attempt to log in after three failed
  attempts
- Testing strategy (BVA?)
- - 0 failed logins -> 1 successful login should work
  - 2 failed logins -> 1 successful login should work
    - this might vacuously fill the first test
  - 3 failed logins -> not allowed to try again

## UC03
- The IoT Controller shall provide a service that allows users to determine the
  temperature/humidity, turn on and off lights, open and close the door, enable the
  alarm, and determine if anyone is home.
- Testing Strategy
  - ??

## UC04
- The IoT Controller should store sensor values, log all actions in the house, and allow
  the user to review the log at any time.
- Testing Strategy
    - ??

## UC05
- The IoT Controller shall allow the user to clear the log at any time.
- Testing Strategy
    - ??

## UC06
- The IoT Controller shall activate the alarm; the door is manually
  opened while the alarm is enabled or the house is suddenly occupied while alarm is
  enabled.
- Testing Strategy (equivalence classes)
    - with alarm enabled -> open door manually, ensure alarm activated
    - with alarm enabled -> set proximity, ensure alarm activated

## UC07
- The IoT Controller shall detect when the house has been vacant for more than a
  user-specified amount of time. Once this time period has passed the system shall
  close the door, turn off the light, and enable the alarm.
- Testing Strategy (BVA)
  - set user defined time to BVA values (min, min+1, mid, max-1, max)
  - ensure all effects happen after simulating time passing

## UC08
- The alarm must be disabled by the user in person by entering a user-defined
  passcode.
- Testing Strategy (equivalence classes)
  - attempt disabling alarm with incorrect password, doesn't work
  - attempt disabling alarm with correct password, does work
  - does this interact with HS05?

## UC09
- The IoT Controller shall automatically turn on the light when the house becomes
  occupied.
- Testing Strategy (equivalence classes)
  - house unoccupied -> unoccupied, light stays off
  - house unoccupied -> occupied, light must turn on

## UC10
- The IoT Controller shall allow the user to set the house temperature. The minimum
  temperature allowed is 50 degrees Fahrenheit (10 degrees Celsius). The maximum
  temperature allowed is 80 degrees Fahrenheit (27 degrees Celsius).
- Testing Strategy (BVA)
  - Success BVA Set to 50, 51, 65 (mid), 79, 80
  - Too low BVA; should fail when set to 49, 48, minimum value, min + 1, some middle ground
  - Too high BVA; should fail when set to 81, 82, INT_MAX, INT_MAX -1, some middle ground

## UC11
- The IoT Controller shall allow new users to be added to the system. New users must
  provide passwords that adhere to the requirements in UC01
- Testing Strategy
  - password covered by UC01
  - testing should verify that a new user has the password that was set for them at least
  - ??

## UC12
- The IoT Controller shall decide whether the house should heat or cool the house.
  The system shall prevent the heater and air conditioner from running at the same
  time.
- Testing Strategy (equivalence class)
  - Set temp hotter than it currently is, ensure heater turns on, AC is off
  - Set temp colder than it currently is, ensure AC turns on, heater is off
  - Attempt different ways of manually setting AC and heater on simultaneously, make sure they don't work

## UC13
- The IoT Controller shall allow the user to turn on and turn off the dehumidifier. The
  dehumidifier can only be activated with the air conditioner.
- Testing Strategy (equivalence class?)
  - AC off, DH off is valid
  - AC on, DH off is valid
  - AC on, DH on is valid
  - AC off, DH on is invalid


## HS01
- The Tartan Smart Home Platform shall require the user to login to the house control
  panel using a username and password. The password has the following
  requirements are still under consideration.
- Testing Strategy (Equivalence class)
  - Log in with proper password is successful
  - Log in with improper password is unsuccessful

## HS02
- The Tartan Smart Home Platform shall allow a user to fetch the state and update
  the state remotely.
- Testing Strategy
    - interacts with many other requirements
    - ??

## HS03
- The Tartan Smart Home Platform shall support multiple houses.
- Testing Strategy
  - Maybe with BVH: try 0 houses, 1 house, limit to some large amount of houses?

## HS04
- The Tartan Smart Home Platform shall support data logging at configurable
  intervals. The data logged shall reflect the state of the house.
- Testing Strategy
  - BVH with different intervals

## HS05
- The Tartan Smart Home Platform shall fully integrate with the IoT Controller. That
  is, the platform will fully support the functionality of the IoT Controller
- Testing Strategy
  - ??
