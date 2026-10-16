package com.almudier.mygarage.core.domain.model

data class Vehicle(
    val id: String,
    val manufacturer: String,
    val model: String,
    val year: Int,
    val trim: String,
    val mileage: Int,
    val plate: String,
    val vin: String = "",
    val color: String,
    val photoUri: String? = null,
)

val EmptyVehicle = Vehicle(
    id = "",
    manufacturer = "",
    model = "",
    year = 0,
    trim = "",
    mileage = 0,
    plate = "",
    color = "",
    photoUri = null,
)

data class MaintenanceItem(
    val service: String,
    val cost: Double = 0.0,
    val laborCost: Double = 0.0,
    val notes: String = "",
)

data class MaintenanceRecord(
    val id: String,
    val vehicleId: String,
    val date: String,
    val mileage: Int,
    val workshop: String,
    val totalCost: Double? = null,
    val notes: String = "",
    val items: List<MaintenanceItem>,
    val type: RecordType = RecordType.Maintenance,
    /** A receipt photo exists; load it with MaintenanceRepository.loadReceipt. */
    val hasReceipt: Boolean = false,
    /**
     * Receipt image when loaded or being edited: a `data:` URI (stored) or a content/file URI
     * (picked, not saved yet). Null in lists; set to null with [hasReceipt] false to remove it.
     */
    val receipt: String? = null,
)

/** Maintenance visits track mileage; upgrades and detailing don't. */
enum class RecordType {
    Maintenance, Upgrade, Detailing;

    val tracksMileage: Boolean get() = this == Maintenance

    companion object {
        fun fromName(name: String?): RecordType? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

val EmptyMaintenanceRecord = MaintenanceRecord(
    id = "",
    vehicleId = "",
    date = "",
    mileage = 0,
    workshop = "",
    items = emptyList(),
)

/** The odometer can't read lower than the mileage logged at the latest service. */
fun Vehicle.withServiceMileage(records: List<MaintenanceRecord>): Vehicle {
    val latestService = records.maxOfOrNull { it.mileage } ?: return this
    return if (latestService > mileage) copy(mileage = latestService) else this
}
