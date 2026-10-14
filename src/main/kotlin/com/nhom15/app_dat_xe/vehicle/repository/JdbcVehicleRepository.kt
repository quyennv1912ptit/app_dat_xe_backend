package com.nhom15.app_dat_xe.vehicle.repository

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.enums.VehicleType
import com.nhom15.app_dat_xe.vehicle.dto.CreateVehicleRequest
import com.nhom15.app_dat_xe.vehicle.dto.VehicleResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class JdbcVehicleRepository(private val jdbcTemplate: JdbcTemplate) {

    fun findAllByOwnerId(ownerId: Long): List<VehicleResponse> = jdbcTemplate.query(
        "SELECT * FROM vehicles WHERE user_id = ? ORDER BY id",
        { row, _ ->
            val labels = (1..row.metaData.columnCount).associate {
                row.metaData.getColumnLabel(it).lowercase() to it
            }
            fun value(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
                labels[name]?.let(row::getString)
            }
            VehicleResponse(
                id = row.getLong("id"),
                plateNumber = value("plate_number"),
                transmissionType = value("transmission_type"),
                vehicleType = value("vehicle_type") ?: "CAR",
                brand = value("brand", "make", "manufacturer"),
                model = value("model"),
                color = value("color", "colour"),
            )
        },
        ownerId,
    )

    fun create(ownerId: Long, request: CreateVehicleRequest): VehicleResponse {
        val columns = vehicleColumns()
        val selected = linkedMapOf<String, Any>(
            "user_id" to ownerId,
            "plate_number" to request.plateNumber.trim().uppercase(),
            "transmission_type" to request.transmissionType.name,
        )
        if ("vehicle_type" in columns) {
            selected["vehicle_type"] = request.vehicleType.name
        } else if (request.vehicleType != VehicleType.CAR) {
            throw BadRequestException(message = "Cơ sở dữ liệu hiện chỉ hỗ trợ loại xe CAR")
        }
        addOptional(selected, columns, listOf("brand", "make", "manufacturer"), request.brand, "hãng xe")
        addOptional(selected, columns, listOf("model"), request.model, "mẫu xe")
        addOptional(selected, columns, listOf("color", "colour"), request.color, "màu xe")

        val missingRequired = listOf("user_id", "plate_number", "transmission_type")
            .filterNot(columns::contains)
        if (missingRequired.isNotEmpty()) {
            throw BadRequestException(message = "Bảng vehicles thiếu cột bắt buộc: ${missingRequired.joinToString()}")
        }

        val keyHolder = GeneratedKeyHolder()
        val names = selected.keys.toList()
        val sql = "INSERT INTO vehicles (${names.joinToString()}) VALUES (${names.joinToString { "?" }})"
        try {
            jdbcTemplate.update({ connection ->
                connection.prepareStatement(sql, arrayOf("id")).apply {
                    names.forEachIndexed { index, name -> setObject(index + 1, selected.getValue(name)) }
                }
            }, keyHolder)
        } catch (e: DataIntegrityViolationException) {
            throw BadRequestException(message = "Biển số xe đã được đăng ký hoặc dữ liệu xe không hợp lệ")
        }
        val id = keyHolder.key?.toLong()
            ?: throw IllegalStateException("Không lấy được id xe mới")
        return findByIdAndOwnerId(id, ownerId) ?: throw IllegalStateException("Không đọc lại được xe vừa tạo")
    }

    fun deleteByIdAndOwnerId(id: Long, ownerId: Long) {
        if (id <= 0) throw BadRequestException(message = "vehicleId phải lớn hơn 0")
        val deleted = try {
            jdbcTemplate.update("DELETE FROM vehicles WHERE id = ? AND user_id = ?", id, ownerId)
        } catch (_: DataIntegrityViolationException) {
            throw ConflictException(message = "Xe đã được sử dụng trong chuyến đi nên không thể xóa")
        }
        if (deleted == 0) throw NotFoundException(ErrorCode.VEHICLE_NOT_FOUND)
    }

    private fun findByIdAndOwnerId(id: Long, ownerId: Long): VehicleResponse? = jdbcTemplate.query(
        "SELECT * FROM vehicles WHERE id = ? AND user_id = ?",
        { row, _ ->
            val labels = (1..row.metaData.columnCount).associate {
                row.metaData.getColumnLabel(it).lowercase() to it
            }
            fun value(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
                labels[name]?.let(row::getString)
            }
            VehicleResponse(
                id = row.getLong("id"), plateNumber = value("plate_number"),
                transmissionType = value("transmission_type"), vehicleType = value("vehicle_type") ?: "CAR",
                brand = value("brand", "make", "manufacturer"), model = value("model"),
                color = value("color", "colour"),
            )
        },
        id,
        ownerId,
    ).firstOrNull()

    private fun vehicleColumns(): Set<String> = jdbcTemplate.execute(ConnectionCallback { connection ->
        val columns = mutableSetOf<String>()
        connection.metaData.getColumns(connection.catalog, null, "vehicles", null).use { rows ->
            while (rows.next()) columns += rows.getString("COLUMN_NAME").lowercase()
        }
        columns
    }) ?: emptySet()

    private fun addOptional(
        selected: MutableMap<String, Any>,
        columns: Set<String>,
        aliases: List<String>,
        value: String?,
        label: String,
    ) {
        val normalized = value?.trim()?.takeIf(String::isNotEmpty) ?: return
        val column = aliases.firstOrNull(columns::contains)
        if (column == null) {
            throw BadRequestException(message = "Cơ sở dữ liệu chưa hỗ trợ lưu $label")
        }
        selected[column] = normalized
    }
}
