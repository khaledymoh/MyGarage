package com.almudier.mygarage.core.data.repository

internal val DEFAULT_SERVICES: List<String> = listOf(
    // Scheduled maintenance
    "Oil change", "Minor service", "Major service", "Oil service", "Annual inspection",
    "Seasonal maintenance", "Summer readiness check", "Winter readiness check",
    "Recall inspection", "Warranty inspection", "Service interval reset", "Maintenance reminder reset",

    // Engine
    "Engine oil replacement", "Oil filter replacement", "Air filter replacement",
    "Cabin air filter replacement", "Fuel filter replacement", "Spark plug replacement",
    "Ignition coil replacement", "Engine coolant replacement", "Coolant flush", "Coolant leak repair",
    "Thermostat replacement", "Water pump replacement", "Engine belt replacement",
    "Serpentine belt replacement", "Timing belt replacement", "Timing chain inspection",
    "Timing chain replacement", "Belt tensioner replacement", "Valve cover gasket replacement",
    "Oil pan gasket replacement", "Head gasket replacement", "Engine mount replacement",
    "Engine compression test", "Engine carbon cleaning", "Fuel injector cleaning",
    "Fuel injector replacement", "Fuel pump replacement", "Throttle body cleaning",
    "PCV valve replacement", "Engine oil leak repair", "Engine overhaul", "Engine replacement",

    // Transmission and drivetrain
    "Transmission oil replacement", "Transmission filter replacement",
    "Transmission oil pan gasket replacement", "Transmission fluid flush",
    "Transmission adaptation reset", "Transmission software update", "Transmission repair",
    "Transmission replacement", "Differential oil replacement", "Differential inspection",
    "Differential seal replacement", "Driveshaft inspection", "Driveshaft repair",
    "Driveshaft replacement", "CV joint replacement", "CV boot replacement",
    "Axle shaft replacement", "Transfer case oil replacement", "Clutch replacement",
    "Flywheel replacement",

    // Hybrid and electric
    "High-voltage battery health check", "High-voltage battery diagnostic",
    "High-voltage battery replacement", "High-voltage battery module repair",
    "Hybrid battery cooling system service", "Hybrid battery coolant replacement",
    "Hybrid battery fan cleaning", "Electric motor diagnostic", "Electric motor repair",
    "Inverter diagnostic", "Inverter coolant replacement", "DC-DC converter diagnostic",
    "High-voltage cable inspection", "High-voltage contactor inspection",
    "Charging port inspection", "Charging port repair", "Onboard charger diagnostic",
    "Onboard charger repair", "Charging cable inspection", "Charging cable replacement",
    "Charging system diagnostic", "Hybrid system software update", "Hybrid cooling fluid service",

    // 12V battery and electrical
    "12V battery replacement", "12V battery registration", "12V battery charging",
    "12V battery health test", "Battery terminal cleaning", "Battery diagnostic",
    "Alternator diagnostic", "Alternator replacement", "Starter motor replacement",
    "Fuse replacement", "Relay replacement", "Wiring repair", "Ground connection repair",
    "ECU diagnostic", "ECU programming", "ECU coding", "Fault code scanning", "Fault code clearing",
    "Parking sensor replacement", "Rear camera repair", "360-degree camera calibration",
    "Window regulator replacement", "Power window repair", "Central locking repair",
    "Key programming", "Remote key battery replacement", "Horn repair",

    // Lighting
    "Headlight bulb replacement", "Headlight repair", "Headlight replacement", "Headlight alignment",
    "Headlight restoration", "Adaptive headlight calibration", "Taillight replacement",
    "Interior lighting repair", "Ambient lighting installation",

    // Cooling and heating
    "Radiator replacement", "Radiator cleaning", "Radiator leak repair", "Coolant hose replacement",
    "Coolant pipe replacement", "Coolant reservoir replacement", "Coolant cap replacement",
    "Cooling fan replacement", "Cooling fan diagnostic", "Heater core cleaning",
    "Heater core replacement", "Coolant bleeding", "Cooling system pressure test",

    // Air conditioning
    "AC system diagnostic", "AC refrigerant recharge", "AC leak detection",
    "AC compressor replacement", "AC condenser replacement", "AC evaporator cleaning",
    "AC evaporator replacement", "AC pressure test", "AC hose replacement",
    "AC expansion valve replacement", "HVAC system inspection", "HVAC blower motor replacement",
    "Blower resistor replacement", "Climate control panel repair", "Heater repair",
    "AC system disinfection", "Cabin odor removal",

    // Intake, turbo and exhaust
    "Engine air intake cleaning", "Intake pipe replacement", "Charge pipe replacement",
    "Intercooler inspection", "Intercooler replacement", "Turbocharger inspection",
    "Turbocharger repair", "Turbocharger replacement", "Turbo oil line replacement",
    "Boost leak test", "Vacuum hose replacement", "Wastegate inspection",
    "Diverter valve replacement", "Exhaust leak repair", "Exhaust manifold gasket replacement",
    "Downpipe replacement", "Catalytic converter replacement", "Exhaust resonator replacement",
    "Center exhaust section replacement", "Muffler replacement",
    "Exhaust valve actuator replacement", "Exhaust pipe welding", "Exhaust system replacement",
    "Oxygen sensor replacement", "Exhaust gas sensor replacement", "Emissions system diagnostic",
    "DPF cleaning", "EGR valve cleaning", "EGR valve replacement",

    // Brakes
    "Brake pad replacement", "Brake disc replacement", "Brake disc resurfacing",
    "Brake fluid replacement", "Brake fluid bleeding", "Brake caliper service",
    "Brake caliper replacement", "Brake hose replacement", "Brake line repair",
    "Brake master cylinder replacement", "Brake booster inspection", "ABS diagnostic",
    "ABS sensor replacement", "Wheel speed sensor replacement", "Electronic parking brake service",
    "Parking brake adjustment", "Handbrake cable replacement", "Brake system inspection",
    "Brake noise diagnosis", "Brake vibration diagnosis",

    // Suspension and steering
    "Shock absorber replacement", "Strut replacement", "Spring replacement", "Coilover installation",
    "Suspension bush replacement", "Control arm replacement", "Ball joint replacement",
    "Stabilizer link replacement", "Stabilizer bar bushing replacement", "Strut mount replacement",
    "Wheel bearing replacement", "Steering rack inspection", "Steering rack replacement",
    "Tie rod replacement", "Tie rod end replacement", "Power steering system service",
    "Power steering fluid replacement", "Electric power steering diagnostic",
    "Steering angle sensor calibration", "Suspension inspection", "Adaptive suspension diagnostic",
    "Adaptive damper replacement", "Ride-height adjustment",

    // Tires and wheels
    "Tire replacement", "Tire rotation", "Tire repair", "Tire pressure adjustment",
    "Nitrogen tire fill", "TPMS service", "TPMS sensor replacement", "Wheel balancing",
    "Wheel alignment", "Four-wheel alignment", "Rim inspection", "Rim repair", "Rim straightening",
    "Tire mounting", "Tire dismounting", "Run-flat tire inspection", "Tire tread inspection",
    "Tire sidewall inspection", "Spare tire inspection", "Wheel bolt replacement",
    "Wheel and tire cleaning",

    // Bodywork and exterior
    "Body panel repair", "Dent removal", "Paint repair", "Full vehicle repaint", "Scratch removal",
    "Paint correction", "Machine polishing", "Ceramic coating",
    "Paint protection film (PPF) installation", "PPF removal", "Window tint installation",
    "Windshield replacement", "Windshield chip repair", "Side window replacement",
    "Rear window replacement", "Wiper blade replacement", "Wiper motor replacement",
    "Washer fluid refill", "Washer pump replacement", "Washer nozzle cleaning",
    "Side mirror replacement", "Mirror motor repair", "Door handle repair", "Door lock replacement",
    "Bumper repair", "Bumper replacement", "Grille replacement", "Hood repair", "Trunk lid repair",
    "Sunroof inspection", "Sunroof drain cleaning", "Sunroof motor repair",
    "Weatherstrip replacement", "Undercarriage inspection", "Rust treatment", "Underbody coating",

    // Interior and comfort
    "Interior deep cleaning", "Upholstery cleaning", "Leather cleaning and conditioning",
    "Seat repair", "Seat upholstery replacement", "Seat heating repair", "Lumbar support repair",
    "Electric seat motor repair", "Seat memory system repair", "Steering wheel repair",
    "Steering wheel replacement", "Heated steering wheel repair", "Interior trim replacement",
    "Dashboard repair", "Headliner replacement", "Carpet cleaning", "Floor mat replacement",
    "Trunk lining repair", "Interior odor removal", "Door trim repair", "Seat belt replacement",
    "Seat belt mechanism repair",

    // Diagnostics, inspection and calibration
    "General inspection", "General vehicle inspection", "Pre-purchase inspection",
    "Periodic maintenance inspection", "Full vehicle diagnostic scan", "Engine diagnostic",
    "Transmission diagnostic", "Hybrid system diagnostic", "Battery health diagnostic",
    "Emissions test", "Live data inspection", "Road test", "Noise diagnosis", "Vibration diagnosis",
    "Fluid leak inspection", "Fluid level inspection", "ECU adaptation reset",
    "Steering angle calibration", "ADAS calibration", "Radar calibration", "Camera calibration",
    "Tire pressure monitoring calibration", "Vehicle software programming", "Vehicle software update",

    // Fluids and lubrication
    "Transmission fluid replacement", "Coolant replacement", "AC refrigerant refill",
    "Lubrication of hinges and locks",

    // Performance and modifications
    "ECU tuning", "Transmission tuning", "Dyno testing", "Intake system upgrade",
    "Air filter upgrade", "Charge pipe upgrade", "Intercooler upgrade", "Turbocharger upgrade",
    "Downpipe installation", "Exhaust system upgrade", "Exhaust valve controller installation",
    "Suspension upgrade", "Brake upgrade", "Wheel upgrade", "Performance tire installation",
    "Lowering spring installation", "Strut brace installation", "Steering wheel upgrade",
    "Body kit installation", "Spoiler installation", "Diffuser installation",
    "Front lip installation", "Paddle shifter installation", "Ambient lighting upgrade",
    "Screen upgrade", "Camera system upgrade", "Retrofit installation", "Vehicle coding",
    "Vehicle feature activation", "Dash cam installation", "Alarm system installation",
    "GPS tracker installation", "Audio system installation",

    // Cleaning and detailing
    "Exterior car wash", "Interior car wash", "Full detailing", "Engine bay cleaning",
    "Undercarriage washing", "Paint decontamination", "Clay bar treatment", "Paint polishing",
    "Wax application", "Glass polishing", "Glass water-repellent treatment", "Leather conditioning",
    "Interior sanitization", "Pet hair removal", "Fabric seat shampooing",

    // Roadside and emergency
    "Battery jump-start", "Battery replacement", "Flat tire assistance", "Tire change",
    "Emergency tire repair", "Vehicle towing", "Breakdown recovery", "Fuel delivery",
    "Lockout assistance", "Emergency diagnostics", "Overheating assistance",
    "Emergency electrical repair", "Accident recovery",

    // Administrative and other
    "Vehicle registration inspection", "Roadworthiness inspection", "Insurance inspection",
    "Accident damage assessment", "Warranty claim inspection", "Recall service",
    "Parts installation", "Labor-only service", "Vehicle pickup and delivery",
).distinctBy { it.lowercase() }
