package  com.nhom15.app_dat_xe.common.domain

/**
 * Số tiền VND, lưu bằng Long (không dùng Double để tránh sai số).
 *
 * Lưu ý: trong entity JPA hãy khai báo cột là `Long` (amount) và bọc `Money(amount)`
 * khi dùng trong code. Value class không map trực tiếp thành cột một cách ổn định.
 */
@JvmInline
value class Money(val amount: Long) : Comparable<Money> {

    operator fun plus(other: Money) = Money(amount + other.amount)
    operator fun minus(other: Money) = Money(amount - other.amount)
    operator fun times(factor: Int) = Money(amount * factor)

    /** Lấy p% của số tiền, làm tròn xuống. Ví dụ hoa hồng: fare.percent(20) */
    fun percent(p: Int) = Money(amount * p / 100)

    fun isZero() = amount == 0L
    fun isPositive() = amount > 0L

    override fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    override fun toString(): String = "${amount}đ"

    companion object {
        val ZERO = Money(0)
    }
}
